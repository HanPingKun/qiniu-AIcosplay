package com.echo.verse.app.config;

import com.echo.verse.app.repository.RedisChatMemoryStore; // 导入我们自己的类
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author hpk
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiConfig {

    // 关键修正: 现在注入的是我们自己编写的、实现了 ChatMemoryStore 接口的 RedisChatMemoryStore 类
    private final RedisChatMemoryStore redisChatMemoryStore;

    @Value("${minimax.api-key}")
    private String minimaxApiKey;
    @Value("${minimax.group-id}")
    private String minimaxGroupId;
    @Value("${minimax.tts-url}")
    private String minimaxTtsUrl;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                // 这里传入我们自己实现的 redisChatMemoryStore Bean
                .chatMemoryStore(redisChatMemoryStore)
                .build();
    }

    @Bean("miniMaxWebClient")
    public WebClient miniMaxWebClient(WebClient.Builder builder) {
        String url = minimaxTtsUrl + "?GroupId=" + minimaxGroupId;
        log.info("Initializing MiniMax WebClient with base URL: {}", url);
        return builder
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + minimaxApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
