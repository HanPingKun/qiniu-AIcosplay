package com.echo.verse.app.service;

import cn.hutool.core.util.HexUtil;
import com.echo.verse.app.agent.CharacterGeneratorAgent;
import com.echo.verse.app.agent.ConversationalAgent;
import com.echo.verse.app.agent.VoiceDirectorAgent;
import com.echo.verse.app.dto.minimax.MiniMaxTtsReqDTO;
import com.echo.verse.app.dto.minimax.MiniMaxTtsRespDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author hpk
 */
@Slf4j
@Service
public class AiCoreService {

    private final CharacterGeneratorAgent characterGeneratorAgent;
    private final VoiceDirectorAgent voiceDirectorAgent;
    private final ConversationalAgent conversationalAgent;
    private final ObjectMapper objectMapper;
    private final WebClient miniMaxWebClient;

    /**
     * 手动编写构造函数以解决 @Qualifier 和 Lombok 的冲突。
     * 将 @Qualifier 注解直接放在构造函数的参数上，这是最标准、最清晰的做法。
     */
    public AiCoreService(CharacterGeneratorAgent characterGeneratorAgent,
                         VoiceDirectorAgent voiceDirectorAgent,
                         ConversationalAgent conversationalAgent,
                         ObjectMapper objectMapper,
                         @Qualifier("miniMaxWebClient") WebClient miniMaxWebClient) {
        this.characterGeneratorAgent = characterGeneratorAgent;
        this.voiceDirectorAgent = voiceDirectorAgent;
        this.conversationalAgent = conversationalAgent;
        this.objectMapper = objectMapper;
        this.miniMaxWebClient = miniMaxWebClient;
    }

    public Mono<GeneratedCharacter> generateCharacter(String characterRequest) {
        return Mono.fromCallable(() -> {
                    log.info("Generating character profile for request: {}", characterRequest);
                    CharacterGeneratorAgent.CharacterProfile profile = characterGeneratorAgent.generateProfile(characterRequest);
                    log.info("Generated profile: {}", profile);

                    String voiceDescriptionJson = objectMapper.writeValueAsString(profile.voiceDescription());

                    // 调用Agent获取原始voice_id
                    String rawVoiceId = voiceDirectorAgent.getVoiceId(voiceDescriptionJson);
                    log.info("Raw voice_id from LLM: '{}'", rawVoiceId);

                    // 关键修正：对LLM的输出进行清理，移除所有空白字符
                    String cleanedVoiceId = rawVoiceId.strip();
                    log.info("Cleaned MiniMax voice_id: '{}'", cleanedVoiceId);

                    return new GeneratedCharacter(profile.characterName(), profile.characterDescription(), cleanedVoiceId);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<String> streamResponse(String conversationId, String characterProfile, String userMessage) {
        return conversationalAgent.chat(conversationId, characterProfile, userMessage)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<byte[]> synthesizeSpeech(String voiceId, String text) {
        // 1. 构建请求体
        MiniMaxTtsReqDTO requestBody = MiniMaxTtsReqDTO.builder()
                .model("speech-01-hd")
                .text(text)
                .stream(false)
                // 关键修正：output_format 应该设置为 'hex'，因为我们需要直接获取音频的十六进制数据
                .outputFormat("hex")
                .voiceSetting(MiniMaxTtsReqDTO.VoiceSetting.builder()
                        .voiceId(voiceId)
                        .speed(1.0)
                        .vol(1.0)
                        .pitch(0)
                        .build())
                .audioSetting(MiniMaxTtsReqDTO.AudioSetting.builder()
                        // audio_setting.format 才是用来控制音频文件类型的参数
                        .format("mp3")
                        .bitrate(128000)
                        .sampleRate(24000)
                        .channel(1)
                        .build())
                .build();

        // 2. 发起异步HTTP请求
        return miniMaxWebClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(MiniMaxTtsRespDTO.class)
                .flatMap(response -> {
                    if (response.getBaseResp() != null && response.getBaseResp().getStatusCode() != 0) {
                        String errorMessage = response.getBaseResp().getStatusMsg();
                        log.error("MiniMax API Error: {}", errorMessage);
                        return Mono.error(new RuntimeException("MiniMax API Error: " + errorMessage));
                    }
                    if (response.getData() == null || response.getData().getAudio() == null) {
                        return Mono.error(new RuntimeException("MiniMax API returned no audio data."));
                    }
                    // 3. 解码Hex字符串为字节数组
                    byte[] audioBytes = HexUtil.decodeHex(response.getData().getAudio());
                    log.info("MiniMax TTS synthesis successful, audio size: {} bytes", audioBytes.length);
                    return Mono.just(audioBytes);
                })
                .doOnError(e -> log.error("MiniMax TTS synthesis failed", e));
    }

    public record GeneratedCharacter(String name, String descriptionPrompt, String voiceId) {}
}
