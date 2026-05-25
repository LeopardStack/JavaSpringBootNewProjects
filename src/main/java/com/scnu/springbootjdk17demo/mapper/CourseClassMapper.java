package com.scnu.springbootjdk17demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.springbootjdk17demo.entity.CourseClass;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseClassMapper extends BaseMapper<CourseClass> {}