package com.echo.verse.app.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * @author hpk
 */
@AiService(chatMemoryProvider = "chatMemoryProvider")
public interface ConversationalAgent {

    /**
     * 修正: @SystemMessage 注解应用于方法，并使用 @V 注入动态内容
     */
    @SystemMessage("{{profile}}")
    Flux<String> chat(@MemoryId String memoryId,
                      @V("profile") String characterProfile,
                      @UserMessage String userMessage);
}