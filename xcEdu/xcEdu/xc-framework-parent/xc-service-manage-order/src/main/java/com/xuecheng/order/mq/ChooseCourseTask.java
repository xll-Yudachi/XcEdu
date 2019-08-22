package com.xuecheng.order.mq;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;

@Component
public class ChooseCourseTask {

	@Autowired
	public TaskService taskService;
	
	@Scheduled(cron="0/3 * * * * *")
	//定时发送添加选课任务
	public void sendChooseCourseTask() {
		//得到1分钟之前的时间
		Date date = new Date();
		long time = date.getTime() - 60000;
		Date preDate = new Date(time);
		List<XcTask> xcTaskList = taskService.findXcTaskList(preDate, 100);
		System.out.println(xcTaskList);
		//调用service发布消息，将添加选课的任务发送给mq
		for(XcTask xcTask : xcTaskList) {
			if(taskService.getTask(xcTask.getId(), xcTask.getVersion()) > 0) {
				String ex = xcTask.getMqExchange();
				String routingKey = xcTask.getMqRoutingkey();
				taskService.publish(xcTask, ex, routingKey);
			}
		}
	}
	
	@RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
	public void receiveFinishChoosecourseTask(XcTask xcTask) {
		if(xcTask != null && StringUtils.isNotEmpty(xcTask.getId())) {
			taskService.finishTask(xcTask.getId());
		}
	}
}
