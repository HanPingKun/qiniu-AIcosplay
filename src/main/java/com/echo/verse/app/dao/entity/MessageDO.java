package com.echo.verse.app.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author hpk
 */
@Data
@TableName("t_message")
public class MessageDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String conversationId;
    private Sender sender;
    private String contentText;
    private Boolean audioGenerated;
    private LocalDateTime timestamp;
}