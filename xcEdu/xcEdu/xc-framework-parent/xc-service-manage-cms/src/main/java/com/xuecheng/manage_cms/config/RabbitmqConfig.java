package com.xuecheng.manage_cms.config;

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

	//交换机的名称
	public static final String EX_ROUTING_CMS_POSTPAGE = "ex_routing_cms_postpage";
	
	//采用(direct)订阅发布形式的交换机
	@Bean(EX_ROUTING_CMS_POSTPAGE)
	public Exchange EXCHANGE_DIRECT_INFORM() {
		return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
	}
}
