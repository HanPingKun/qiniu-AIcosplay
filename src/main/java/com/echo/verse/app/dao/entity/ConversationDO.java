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
@TableName("t_conversation")
public class ConversationDO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private Long userId;
    private String characterName;
    private String characterDescriptionPrompt;
    private String characterVoiceId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
