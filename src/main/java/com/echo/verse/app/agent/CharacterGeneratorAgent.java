package com.echo.verse.app.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * @author hpk
 */
@AiService(tools = "voiceTool")
public interface CharacterGeneratorAgent {
    record CharacterProfile(String characterName, String characterDescription, VoiceDescription voiceDescription) {}
    record VoiceDescription(String gender, String age, String accent, String speakingStyle) {}

    @SystemMessage(fromResource = "/prompts/character-generator.txt")
    CharacterProfile generateProfile(String characterRequest);
}