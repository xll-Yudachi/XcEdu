package com.xuecheng.learning.mq;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.CourseLearningService;

@Component
public class ChooseCourseTask {

	@Autowired
	private CourseLearningService courseLearningService;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@RabbitListener(queues = RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE)
	public void receiveChoosecourseTask(XcTask xcTask) {
		//取出消息的内容
		String requestBody = xcTask.getRequestBody();
		Map map = JSON.parseObject(requestBody, Map.class);
		String userId = (String) map.get("userId");
		String courseId = (String) map.get("courseId");
		
		ResponseResult addcourse = courseLearningService.addCourse(userId, courseId, null, null, null, xcTask);
		if(addcourse.isSuccess()) {
			rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE,RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY, xcTask);
		}
	}
}
