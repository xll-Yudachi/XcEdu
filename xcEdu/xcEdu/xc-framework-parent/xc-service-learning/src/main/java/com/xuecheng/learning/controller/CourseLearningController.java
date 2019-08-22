package com.xuecheng.learning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xuecheng.api.learning.CourseLearningControllerApi;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.learning.service.CourseLearningService;

@RestController
@RequestMapping("/learning/course")
public class CourseLearningController implements CourseLearningControllerApi{

	@Autowired
	private CourseLearningService courseLearningService;
	
	@Override
	@GetMapping("/getmediaUrl/{courseId}/{teachplanId}")
	public GetMediaResult getMediaUrl(@PathVariable("courseId") String courseId, @PathVariable("teachplanId") String teachplanId) {
		System.err.println(courseId);
		System.err.println(teachplanId);
		//获取课程学习地址
		return courseLearningService.getMediaUrl(courseId,teachplanId);
	}

}
