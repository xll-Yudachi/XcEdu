package com.xuecheng.manage_course.dao;

import org.apache.ibatis.annotations.Mapper;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;

@Mapper
public interface TeachplanMapper {
	//课程计划查询
	public TeachplanNode selectList(String courseId);
}
