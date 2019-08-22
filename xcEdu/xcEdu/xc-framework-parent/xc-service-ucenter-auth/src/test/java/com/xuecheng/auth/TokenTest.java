package com.xuecheng.auth;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.xuecheng.framework.client.XcServiceList;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TokenTest {

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private LoadBalancerClient loadBalancerClient;
	
	@Test
	public void testToken() {
		//从eureka中获取认证服务的地址(因为Spring Security在认证服务中)
		//从eureka中获取认证服务的一个实例的地址
		ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
		//此地址就是http://ip:port
		URI uri = serviceInstance.getUri();
		//令牌的申请地址是 http://localhost:40400/auth/oauth/token
		String authUrl = uri + "/auth/oauth/token";
		
		//定义header
		LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		String httpBasic = getHttpBasic("XcWebApp", "XcWebApp");
		header.add("Authorization", httpBasic);
		
		//定义body
		LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "password");
		body.add("username", "itcast");
		body.add("password", "123");
		
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body,header);
		
		//设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			public void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
				if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401) {
					super.handleError(response, statusCode);
				}
			}
		});
		
		ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
		
		//申请令牌信息
		Map bodyMap = exchange.getBody();
		System.out.println(bodyMap);
	}
	
	//获取httpbasic的串
	private String getHttpBasic(String clientId, String clientSecret) {
		String string = clientId + ":" + clientSecret;
		//将串进行Base64编码
		byte[] encode = Base64Utils.encode(string.getBytes());
		return "Basic " + new String(encode);
	}
}
