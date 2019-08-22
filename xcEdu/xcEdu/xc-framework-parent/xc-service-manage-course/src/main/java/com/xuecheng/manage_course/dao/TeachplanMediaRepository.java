package com.xuecheng.manage_course.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xuecheng.framework.domain.course.TeachplanMedia;

public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia, String>{

	//根据课程id查询列表
	List<TeachplanMedia> findByCourseId(String courseId);
}
