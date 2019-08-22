package com.xuecheng.manage_course.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.dao.CategoryMapper;

@Service
public class CategoryService {

	@Autowired
	private CategoryMapper categoryMapper;

	//查询分类
	public CategoryNode findCategoryList() {
		return categoryMapper.findCategoryList();
	}

}
