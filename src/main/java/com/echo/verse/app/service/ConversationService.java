package com.echo.verse.app.service;
import com.echo.verse.app.dto.req.CreateConversationReqDTO;
import com.echo.verse.app.dto.req.SendMessageReqDTO;
import com.echo.verse.app.dto.resp.ConversationInfoRespDTO;
import com.echo.verse.app.dto.resp.ConversationRespDTO;
import com.echo.verse.app.dto.resp.MessageRespDTO;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
/**
 * @author hpk
 */
public interface ConversationService {
    Mono<ConversationRespDTO> startNewConversation(Long userId, CreateConversationReqDTO reqDTO);
    Flux<ServerSentEvent<String>> streamChatMessage(Long userId, String conversationId, SendMessageReqDTO reqDTO);
    Flux<ConversationInfoRespDTO> getConversationHistory(Long userId);
    Flux<MessageRespDTO> getMessageHistory(Long userId, String conversationId);
    Mono<byte[]> synthesizeMessageAudio(Long userId, String conversationId, Long messageId);
}
