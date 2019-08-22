package com.xuecheng.manage_cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @ClassName: ManageCmsApplication   
 * @Description: CMS管理入口类
 * @author: Yudachi
 * @date: 2019年6月8日 上午11:28:38
 */
@SpringBootApplication
@EntityScan("com.xuecheng.framewokr.domain.cms") //扫描实体类
@ComponentScan(basePackages = {"com.xuecheng.api"})	//扫描接口
@ComponentScan(basePackages = {"com.xuecheng.manage_cms"}) 
@ComponentScan(basePackages = {"com.xuecheng.framework"})
@EnableDiscoveryClient
@FeignClient
public class ManageCmsApplication {
	public static void main(String[] args) {
		SpringApplication.run(ManageCmsApplication.class, args);
	}
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
	}
}
