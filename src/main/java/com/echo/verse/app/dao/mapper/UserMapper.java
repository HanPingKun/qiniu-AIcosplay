package com.echo.verse.app.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.echo.verse.app.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hpk
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {}