package com.xuecheng.manage.cms.client.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

	//队列bean名称
	public static final String QUEUE_CMS_POSTPAGE = "queue_cms_postpage";
	//交换机的名称
	public static final String EX_ROUTING_CMS_POSTPAGE = "ex_routing_cms_postpage";
	//队列名称
	@Value("${xuecheng.mq.queue}")
	public String queue_cms_postpage_name;
	//routingKey即站点Id
	@Value("${xuecheng.mq.routingKey}")
	public String routingKey;
	
	//采用(direct)订阅发布形式的交换机
	@Bean(EX_ROUTING_CMS_POSTPAGE)
	public Exchange EXCHANGE_DIRECT_INFORM() {
		return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
	}
	
	//声明队列
	@Bean(QUEUE_CMS_POSTPAGE)
	public Queue QUEUE_CMS_POSTPAGE() {
		return new Queue(queue_cms_postpage_name);
	}
	
	//绑定到交换机
	@Bean
	public Binding BINDING_QUEUE_INFORM_SMS(@Qualifier(QUEUE_CMS_POSTPAGE) Queue queue,@Qualifier(EX_ROUTING_CMS_POSTPAGE) Exchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
	}
}
