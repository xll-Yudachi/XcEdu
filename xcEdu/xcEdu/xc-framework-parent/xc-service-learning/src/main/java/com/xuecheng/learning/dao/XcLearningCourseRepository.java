package com.xuecheng.learning.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xuecheng.framework.domain.learning.XcLearningCourse;

public interface XcLearningCourseRepository extends JpaRepository<XcLearningCourse, String>{

	//根据用户id和课程id查询
	XcLearningCourse findByUserIdAndCourseId(String userId,String courseId);
	
}
