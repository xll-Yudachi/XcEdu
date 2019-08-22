package com.xuecheng.learning.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;

@FeignClient(value="xc-service-search")
public interface CourseSearchClient {
	@GetMapping("/search/course/getmedia/{teachplanId}")
	public TeachplanMediaPub getmedia(@PathVariable("teachplanId") String teachplanId);
}
