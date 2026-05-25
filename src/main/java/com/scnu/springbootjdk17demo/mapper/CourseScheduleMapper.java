package com.scnu.springbootjdk17demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.springbootjdk17demo.entity.CourseSchedule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseScheduleMapper extends BaseMapper<CourseSchedule> {}