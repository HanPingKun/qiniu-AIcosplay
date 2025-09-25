package com.echo.verse.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echo.verse.app.dao.entity.ConversationDO;
import com.echo.verse.app.dao.entity.MessageDO;
import com.echo.verse.app.dao.entity.Sender;
import com.echo.verse.app.dao.mapper.ConversationMapper;
import com.echo.verse.app.dao.mapper.MessageMapper;
import com.echo.verse.app.dto.req.CreateConversationReqDTO;
import com.echo.verse.app.dto.req.SendMessageReqDTO;
import com.echo.verse.app.dto.resp.ConversationInfoRespDTO;
import com.echo.verse.app.dto.resp.ConversationRespDTO;
import com.echo.verse.app.dto.resp.MessageRespDTO;
import com.echo.verse.app.exception.ResourceNotFoundException;
import com.echo.verse.app.service.AiCoreService;
import com.echo.verse.app.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author hpk
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final AiCoreService aiCoreService;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @Override
    public Mono<ConversationRespDTO> startNewConversation(Long userId, CreateConversationReqDTO reqDTO) {
        // 关键修正：在调用AI服务后立即使用 .cache()
        // 这会创建一个新的Mono，它会缓存第一次执行的结果。
        // 后续的所有 .flatMap 操作都将使用这个缓存的结果，而不会重新触发对大模型的API调用。
        Mono<AiCoreService.GeneratedCharacter> cachedGeneratedCharacterMono =
                aiCoreService.generateCharacter(reqDTO.getCharacterRequest()).cache();

        return cachedGeneratedCharacterMono
                .flatMap(generatedCharacter -> {
                    ConversationDO conversation = new ConversationDO();
                    conversation.setUserId(userId);
                    conversation.setCharacterName(generatedCharacter.name());
                    conversation.setTitle("New Conversation");
                    // 如果你修改了数据库字段，请确保这里是 setCharacterVoiceId
                    conversation.setCharacterVoiceId(generatedCharacter.voiceId());
                    conversation.setCharacterDescriptionPrompt(generatedCharacter.descriptionPrompt());
                    conversation.setCreatedAt(LocalDateTime.now());
                    conversation.setUpdatedAt(LocalDateTime.now());

                    return Mono.fromCallable(() -> conversationMapper.insert(conversation))
                            .subscribeOn(Schedulers.boundedElastic())
                            .thenReturn(conversation);
                })
                .flatMap(savedConversation -> {
                    String welcomePrompt = "Introduce yourself briefly in one sentence and greet the user to start the conversation.";

                    // 这里我们再次使用缓存的 Mono 来获取角色描述，而不会触发新的API调用
                    return cachedGeneratedCharacterMono.flatMap(generatedCharacter ->
                            aiCoreService.streamResponse(
                                            savedConversation.getId(),
                                            generatedCharacter.descriptionPrompt(), // 从缓存的结果中获取
                                            welcomePrompt
                                    )
                                    .collectList()
                                    .map(parts -> String.join("", parts))
                                    .flatMap(welcomeMessage -> {
                                        MessageDO aiMessage = new MessageDO();
                                        aiMessage.setConversationId(savedConversation.getId());
                                        aiMessage.setSender(Sender.AI);
                                        aiMessage.setContentText(welcomeMessage);
                                        aiMessage.setTimestamp(LocalDateTime.now());
                                        aiMessage.setAudioGenerated(true);

                                        return Mono.fromCallable(() -> messageMapper.insert(aiMessage))
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .map(res -> ConversationRespDTO.builder()
                                                        .conversationId(savedConversation.getId())
                                                        .characterName(savedConversation.getCharacterName())
                                                        .initialMessage(MessageRespDTO.builder()
                                                                .id(aiMessage.getId())
                                                                .sender(Sender.AI)
                                                                .content(welcomeMessage)
                                                                .audioAvailable(true)
                                                                .timestamp(aiMessage.getTimestamp())
                                                                .build())
                                                        .createdAt(savedConversation.getCreatedAt())
                                                        .build());
                                    })
                    );
                });
    }

    @Override
    public Flux<ServerSentEvent<String>> streamChatMessage(Long userId, String conversationId, SendMessageReqDTO reqDTO) {
        StringBuilder fullAiResponse = new StringBuilder();
        AtomicLong eventId = new AtomicLong(0);

        Mono<ConversationDO> conversationMono = verifyAndGetConversation(userId, conversationId).cache();

        Mono<Void> saveUserMessageMono = conversationMono.flatMap(conv -> {
            MessageDO userMessage = new MessageDO();
            userMessage.setConversationId(conversationId);
            userMessage.setSender(Sender.USER);
            userMessage.setContentText(reqDTO.getContent());
            userMessage.setTimestamp(LocalDateTime.now());
            return Mono.fromRunnable(() -> messageMapper.insert(userMessage))
                    .subscribeOn(Schedulers.boundedElastic());
        }).then();

        Flux<String> aiResponseStream = conversationMono.flatMapMany(conv ->
                aiCoreService.streamResponse(conv.getId(), conv.getCharacterDescriptionPrompt(), reqDTO.getContent())
        );

        // 关键修正: 使用 concatWith 和 Mono.defer 来确保数据库操作在流结束后执行
        Flux<ServerSentEvent<String>> messageStream = saveUserMessageMono
                .thenMany(aiResponseStream)
                .doOnNext(fullAiResponse::append)
                .map(token -> ServerSentEvent.builder(token)
                        .id(String.valueOf(eventId.getAndIncrement()))
                        .event("message")
                        .build());

        // 创建一个在流结束后执行的 Mono<Void>
        Mono<Void> saveAiMessageAndTitleMono = Mono.defer(() -> {
            if (fullAiResponse.isEmpty()) {
                return Mono.empty();
            }
            MessageDO aiMessage = new MessageDO();
            aiMessage.setConversationId(conversationId);
            aiMessage.setSender(Sender.AI);
            aiMessage.setContentText(fullAiResponse.toString());
            aiMessage.setTimestamp(LocalDateTime.now());
            aiMessage.setAudioGenerated(reqDTO.isRequestAudio());

            // 将保存AI消息和更新标题串联起来
            return Mono.fromRunnable(() -> messageMapper.insert(aiMessage))
                    .subscribeOn(Schedulers.boundedElastic())
                    .then(conversationMono)
                    .flatMap(this::updateConversationTitleIfNeeded);
        }).then(); // 转换为 Mono<Void>

        // 使用 concatWith 将消息流和结束操作连接起来
        // 它会先发送完所有 messageStream 的元素，然后再执行 saveAiMessageAndTitleMono
        return messageStream.concatWith(saveAiMessageAndTitleMono.then(Mono.empty()));
    }

    @Override
    public Flux<ConversationInfoRespDTO> getConversationHistory(Long userId) {
        return Flux.defer(() -> {
                    List<ConversationDO> conversations = conversationMapper.selectList(
                            new LambdaQueryWrapper<ConversationDO>()
                                    .eq(ConversationDO::getUserId, userId)
                                    .orderByDesc(ConversationDO::getUpdatedAt)
                    );
                    return Flux.fromIterable(conversations);
                }).subscribeOn(Schedulers.boundedElastic())
                .map(conv -> ConversationInfoRespDTO.builder()
                        .id(conv.getId())
                        .title(conv.getTitle())
                        .characterName(conv.getCharacterName())
                        .createdAt(conv.getCreatedAt())
                        .build());
    }

    @Override
    public Flux<MessageRespDTO> getMessageHistory(Long userId, String conversationId) {
        return verifyAndGetConversation(userId, conversationId)
                .flatMapMany(conv -> Flux.defer(() -> {
                    List<MessageDO> messages = messageMapper.selectList(
                            new LambdaQueryWrapper<MessageDO>()
                                    .eq(MessageDO::getConversationId, conversationId)
                                    .orderByAsc(MessageDO::getTimestamp)
                    );
                    return Flux.fromIterable(messages);
                }).subscribeOn(Schedulers.boundedElastic()))
                .map(msg -> MessageRespDTO.builder()
                        .id(msg.getId())
                        .sender(msg.getSender())
                        .content(msg.getContentText())
                        .audioAvailable(msg.getAudioGenerated())
                        .timestamp(msg.getTimestamp())
                        .build());
    }

    @Override
    public Mono<byte[]> synthesizeMessageAudio(Long userId, String conversationId, Long messageId) {
        return verifyAndGetConversation(userId, conversationId)
                .flatMap(conv -> Mono.fromCallable(() -> messageMapper.selectById(messageId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Message not found")))
                        .filter(msg -> msg.getConversationId().equals(conversationId) && msg.getSender() == Sender.AI)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid message for audio synthesis")))
                        .flatMap(msg -> aiCoreService.synthesizeSpeech(conv.getCharacterVoiceId(), msg.getContentText()))
                );
    }



    private Mono<ConversationDO> verifyAndGetConversation(Long userId, String conversationId) {
        return Mono.fromCallable(() -> conversationMapper.selectById(conversationId))
                .subscribeOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Conversation not found")))
                .filter(conv -> conv.getUserId().equals(userId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Conversation not found or access denied")));
    }

    private Mono<Void> updateConversationTitleIfNeeded(ConversationDO conversation) {
        if (!"New Conversation".equals(conversation.getTitle())) {
            return Mono.empty();
        }

        return Mono.fromCallable(() -> messageMapper.selectList(
                        new LambdaQueryWrapper<MessageDO>()
                                .eq(MessageDO::getConversationId, conversation.getId())
                                .orderByAsc(MessageDO::getTimestamp).last("LIMIT 4")))
                .subscribeOn(Schedulers.boundedElastic())
                .filter(messages -> messages.size() >= 2)
                .flatMap(messages -> {
                    String context = messages.stream()
                            .map(msg -> msg.getSender() + ": " + msg.getContentText())
                            .reduce("", (a, b) -> a + "\n" + b);
                    String prompt = "Please generate a short, concise title (less than 10 words) for the following conversation. " +
                            "Your output MUST be the title text ONLY. " +
                            "Do NOT include any explanations, markdown, or any characters before or after the title itself. " +
                            "For example, a good title would be '与赫敏聊天'.\n\n" +
                            "Conversation:\n" + context;

                    return aiCoreService.streamResponse("title-generator", "You are a title generator.", prompt)
                            .collectList()
                            .map(parts -> String.join("", parts).trim()) // 收集并初步清理
                            .map(rawTitle -> {
                                // 关键修正：对返回的标题进行严格处理
                                // 1. 移除可能存在的 Markdown 标记，如 **
                                String cleanedTitle = rawTitle.replace("*", "");
                                // 2. 如果包含换行符，只取第一行
                                if (cleanedTitle.contains("\n")) {
                                    cleanedTitle = cleanedTitle.split("\n")[0].trim();
                                }
                                // 3. 确保标题长度不超过数据库字段限制 (e.g., 200)
                                if (cleanedTitle.length() > 200) {
                                    cleanedTitle = cleanedTitle.substring(0, 197) + "...";
                                }
                                return cleanedTitle;
                            });
                })
                .flatMap(title -> {
                    if (title.isBlank()) {
                        return Mono.empty();
                    }
                    conversation.setTitle(title);
                    conversation.setUpdatedAt(LocalDateTime.now());
                    return Mono.fromRunnable(() -> conversationMapper.updateById(conversation))
                            .subscribeOn(Schedulers.boundedElastic());
                }).then();
    }
}
