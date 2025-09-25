package com.echo.verse.app.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echo.verse.app.dao.entity.MessageDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hpk
 */
@Mapper
public interface MessageMapper extends BaseMapper<MessageDO> {}