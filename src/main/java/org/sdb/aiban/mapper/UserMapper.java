package org.sdb.aiban.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.sdb.aiban.entity.SysUser;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
}