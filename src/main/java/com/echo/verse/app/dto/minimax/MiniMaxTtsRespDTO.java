package com.echo.verse.app.dto.minimax;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author hpk
 */
@Data
public class MiniMaxTtsRespDTO {
    private DataPayload data;
    @JsonProperty("base_resp")
    private BaseResp baseResp;
    @JsonProperty("trace_id")
    private String traceId;

    @Data
    public static class DataPayload {
        private String audio; // hex编码的音频
    }

    @Data
    public static class BaseResp {
        @JsonProperty("status_code")
        private int statusCode;
        @JsonProperty("status_msg")
        private String statusMsg;
    }
}
