package com.scnu.springbootjdk17demo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.springbootjdk17demo.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // BaseMapper 已提供 selectOne、updateById 等所有基础方法
    // 复杂 SQL 才需要在这里写
}