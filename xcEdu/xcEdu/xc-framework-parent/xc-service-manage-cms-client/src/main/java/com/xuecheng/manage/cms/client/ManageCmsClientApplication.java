package com.xuecheng.manage.cms.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("com.xuecheng.framework.domain.cms")	//扫描实体类
@ComponentScan(basePackages = {"com.xuecheng.framework"})
@ComponentScan(basePackages = {"com.xuecheng.manage.cms.client"})
@EnableDiscoveryClient
public class ManageCmsClientApplication {
	public static void main(String[] args) {
		SpringApplication.run(ManageCmsClientApplication.class, args);
	}
}
