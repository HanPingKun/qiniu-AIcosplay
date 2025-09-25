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
@TableName("t_user")
public class UserDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}