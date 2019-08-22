package com.xuecheng.manage.cms.client.mqListener;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.xuecheng.manage.cms.client.service.PageService;

@Component
public class ConsumerPostPage {

	@Autowired
	private PageService pageService;
	
	@RabbitListener(queues = {"${xuecheng.mq.queue}"})
	public void postPage(String msg) {
		//解析消息
		Map map = JSON.parseObject(msg,Map.class);
		//得到消息中的页面Id
		String pageId = (String)map.get("pageId");
		//调用service方法将页面从GridFS中下载到服务器
		pageService.savePageToServerPath(pageId);
	}
}
