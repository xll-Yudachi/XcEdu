package com.xuecheng.freemarker.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/freemarker")
public class FreemarkerController {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@RequestMapping("/banner")
	public String index_banner(Map<String, Object> map) {
		//使用RestTemplate获取轮播图模型数据
		ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
		Map body = forEntity.getBody();
		map.putAll(body);
		return "index_banner";
	
	}
	
	@RequestMapping("/course")
	public String course(Map<String, Object> map) {
		//使用RestTemplate获取课程信息模型数据
		ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course/courseview/4028e581617f945f01617f9dabc40000", Map.class);
		Map body = forEntity.getBody();
		map.putAll(body);
		return "course";
	
	}
}
