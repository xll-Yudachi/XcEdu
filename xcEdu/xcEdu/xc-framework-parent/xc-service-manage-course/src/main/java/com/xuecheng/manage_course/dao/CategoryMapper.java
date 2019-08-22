package com.xuecheng.manage_course.dao;

import org.apache.ibatis.annotations.Mapper;

import com.xuecheng.framework.domain.course.ext.CategoryNode;

@Mapper
public interface CategoryMapper {

	//查询课程分类
	public CategoryNode findCategoryList();
}
