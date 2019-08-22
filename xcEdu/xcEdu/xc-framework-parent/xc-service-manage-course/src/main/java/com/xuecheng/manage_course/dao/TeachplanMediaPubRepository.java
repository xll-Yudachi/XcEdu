package com.xuecheng.manage_course.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;

public interface TeachplanMediaPubRepository extends JpaRepository<TeachplanMediaPub, String>{
	//根据课程Id删除记录
	long deleteByCourseId(String courseId);
}
