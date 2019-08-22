package com.xuecheng.govern_center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
@EnableDiscoveryClient
public class GovernCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(GovernCenterApplication.class, args);
	}
}
