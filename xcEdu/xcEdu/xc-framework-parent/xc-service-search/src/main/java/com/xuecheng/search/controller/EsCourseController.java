package com.xuecheng.search.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xuecheng.api.search.EsCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.EsCourseService;

@RestController
@RequestMapping("/search/course")
public class EsCourseController implements EsCourseControllerApi{

	@Autowired
	private EsCourseService esCourseService;
	
	@Override
	@GetMapping("/list/{page}/{size}")
	public QueryResponseResult<CoursePub> searchList(@PathVariable("page") int page, @PathVariable("size") int size, CourseSearchParam courseSearchParam) {
		return esCourseService.searchList(page, size, courseSearchParam);
	}

	@Override
	@GetMapping("/getall/{id}")
	public Map<String, CoursePub> getall(@PathVariable("id") String id) {
		return esCourseService.getall(id);
	}

	@Override
	@GetMapping("/getmedia/{teachplanId}")
	public TeachplanMediaPub getmedia(@PathVariable("teachplanId") String teachplanId) {
		String[] teachplanIds = new String[]{teachplanId};
		QueryResponseResult<TeachplanMediaPub> queryResponseResult = esCourseService.getmedia(teachplanIds);
		QueryResult<TeachplanMediaPub> queryResult = queryResponseResult.getQueryResult();
		if(queryResult!=null) {
			if(queryResult.getList()!=null && queryResult.getList().size()>0) {
				//返回课程计划对应课程媒资
				return queryResult.getList().get(0);
			}
		}
		return new TeachplanMediaPub();
	}

}
