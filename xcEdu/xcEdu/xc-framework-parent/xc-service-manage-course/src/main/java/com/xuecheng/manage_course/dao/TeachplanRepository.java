package com.xuecheng.manage_course.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xuecheng.framework.domain.course.Teachplan;

public interface TeachplanRepository extends JpaRepository<Teachplan, String>{

	 public List<Teachplan> findByCourseidAndParentid(String courseId,String parentId);
}
