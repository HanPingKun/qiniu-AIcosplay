package com.echo.verse.app.dto.resp;
import com.echo.verse.app.dao.entity.Sender;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * @author hpk
 */
@Data
@Builder
public class MessageRespDTO {
    private Long id;
    private Sender sender;
    private String content;
    private boolean audioAvailable;
    private LocalDateTime timestamp;
}