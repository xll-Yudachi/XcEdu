package com.xuecheng.order.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @ClassName: AsyncTaskConfig   
 * @Description: Spring Task的并行任务调度
 * @author: Yudachi
 * @date: 2019年8月13日 下午6:54:55
 * @version V1.0
 */
@Configuration
@EnableScheduling
public class AsyncTaskConfig implements SchedulingConfigurer, AsyncConfigurer{

	//线程池线程数量
	private int corePoolSize = 5;
	
	@Bean
	public ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		//初始化线程池
		scheduler.initialize();
		//线程池容量
		scheduler.setPoolSize(corePoolSize);
		return scheduler;
	}

	@Override
	public Executor getAsyncExecutor() {
		Executor executor = taskScheduler();
		return executor;
	}
	
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return null;
	}
	
	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
		scheduledTaskRegistrar.setTaskScheduler(taskScheduler());
	}
}
