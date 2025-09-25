package com.echo.verse.app.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * @author hpk
 */
@AiService
public interface VoiceDirectorAgent {
    @SystemMessage(fromResource = "/prompts/voice-director.txt")
    String getVoiceId(String voiceDescriptionJson);
}
