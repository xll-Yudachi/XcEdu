package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.ext.CategoryNode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="课程分类管理接口",description = "课程分类管理接口,提供课程分类的增、删、查、改")
public interface CategoryControllerApi {
	
	@ApiOperation("课程分类查询")
	public CategoryNode findCategoryList();

}
