package com.echo.verse.app.dto.minimax;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * @author hpk
 */ // 使用建造者模式，方便构建复杂的请求体
@Data
@Builder
public class MiniMaxTtsReqDTO {
    private String model;
    private String text;
    private boolean stream;
    @JsonProperty("voice_setting")
    private VoiceSetting voiceSetting;
    @JsonProperty("audio_setting")
    private AudioSetting audioSetting;
    @JsonProperty("pronunciation_dict")
    private PronunciationDict pronunciationDict;
    @JsonProperty("output_format")
    private String outputFormat;

    @Data
    @Builder
    public static class VoiceSetting {
        @JsonProperty("voice_id")
        private String voiceId;
        private double speed;
        private double vol;
        private int pitch;
    }

    @Data
    @Builder
    public static class AudioSetting {
        @JsonProperty("sample_rate")
        private int sampleRate;
        private int bitrate;
        private String format;
        private int channel;
    }

    @Data
    @Builder
    public static class PronunciationDict {
        private List<String> tone;
    }
}
