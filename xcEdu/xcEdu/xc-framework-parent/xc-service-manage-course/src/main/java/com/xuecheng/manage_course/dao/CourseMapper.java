package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper {
 
	//查询课程计划
	CourseBase findCourseBaseById(String id);

	//查询课程信息
	Page<CourseInfo> findCourseListPage(CourseListRequest courseListRequest);
	
	//我的课程查询列表
	Page<CourseInfo> findCourseList(CourseListRequest courseListRequest);
		
}
