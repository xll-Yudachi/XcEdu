package com.xuecheng.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

	//添加选课任务交换机
	public static final String EX_LEARNING_ADDCHOOSECOURSE = "ex_learning_addchoosecourse";
	
	//完成添加选课消息队列
    public static final String XC_LEARNING_FINISHADDCHOOSECOURSE = "xc_learning_finishaddchoosecourse";
    
    //添加选课消息队列
    public static final String XC_LEARNING_ADDCHOOSECOURSE = "xc_learning_addchoosecourse";
    
    //添加选课路由key
    public static final String XC_LEARNING_ADDCHOOSECOURSE_KEY = "addchoosecourse";
   
    //完成添加选课路由key
    public static final String XC_LEARNING_FINISHADDCHOOSECOURSE_KEY = "finishaddchoosecourse";

    /**
     * 交换机配置
     */
    @Bean(EX_LEARNING_ADDCHOOSECOURSE)
    public Exchange EX_DECLARE() {
    	return ExchangeBuilder.directExchange(EX_LEARNING_ADDCHOOSECOURSE).durable(true).build();
    }
    
    //声明完成添加选课队列
    @Bean(XC_LEARNING_FINISHADDCHOOSECOURSE)
    public Queue FINISHADD_QUEUE_DECLARE() {
    	Queue queue = new Queue(XC_LEARNING_FINISHADDCHOOSECOURSE, true, false, true);
    	return queue;
    }
    
    //声明添加选课队列
    @Bean(XC_LEARNING_ADDCHOOSECOURSE)
    public Queue ADD_QUEUE_DECLARE() {
    	Queue queue = new Queue(XC_LEARNING_ADDCHOOSECOURSE, true, false, true);
    	return queue;
    }
    
    //绑定完成添加选课队列到交换机
    @Bean
    public Binding binding_finishedadd_queue(@Qualifier("xc_learning_finishaddchoosecourse") Queue queue,@Qualifier("ex_learning_addchoosecourse") Exchange exchange) {
    	return BindingBuilder.bind(queue).to(exchange).with(XC_LEARNING_FINISHADDCHOOSECOURSE_KEY).noargs();
    }
    
    //绑定添加选课队列到交换机
    @Bean
    public Binding binding_add_queue(@Qualifier("xc_learning_addchoosecourse") Queue queue, @Qualifier("ex_learning_addchoosecourse") Exchange exchange) {
    	return BindingBuilder.bind(queue).to(exchange).with(XC_LEARNING_ADDCHOOSECOURSE_KEY).noargs();
    }
}
