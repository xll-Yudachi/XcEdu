package com.xuecheng.auth.service;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.utils.CookieUtil;

@Service
public class AuthService {

	@Value("${auth.tokenValiditySeconds}")
	private int tokenValiditySeconds;
	@Value("${auth.cookieDomain}")
	private String cookieDomain;
	@Value("${auth.cookieMaxAge}")
	private int cookieMaxAge;

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private LoadBalancerClient loadBalancerClient;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	// 用户认证申请令牌，将令牌存储到redis
	public AuthToken login(String username, String password, String clientId, String clientSecret) {

		// 请求Spring Security申请令牌
		AuthToken authToken = applyToken(username, password, clientId, clientSecret);
		if (authToken == null) {
			ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
		}
		// 用户身份令牌
		String access_token = authToken.getAccess_token();
		// 存储到redis中的内容
		String jsonString = JSON.toJSONString(authToken);
		// 将令牌存储到redis
		boolean result = saveToken(access_token, jsonString, tokenValiditySeconds);
		if (!result) {
			ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
		}
		return authToken;
	}

	// 申请令牌
	private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
		// 从eureka中获取认证服务的地址(因为Spring Security在认证服务中)
		// 从eureka中获取认证服务的一个实例的地址
		ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
		// 此地址就是http://ip:port
		URI uri = serviceInstance.getUri();
		// 令牌的申请地址是 http://localhost:40400/auth/oauth/token
		String authUrl = uri + "/auth/oauth/token";

		// 定义header
		LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		String httpBasic = getHttpBasic(clientId, clientSecret);
		header.add("Authorization", httpBasic);

		// 定义body
		LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "password");
		body.add("username", username);
		body.add("password", password);

		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);

		// 设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			public void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
				if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
					super.handleError(response, statusCode);
				}
			}
		});

		ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);

		// 申请令牌信息
		Map bodyMap = exchange.getBody();
		if (bodyMap == null || bodyMap.get("access_token") == null || bodyMap.get("refresh_token") == null
				|| bodyMap.get("jti") == null) {

			if (bodyMap != null && bodyMap.get("error_description") != null) {
				String error_description = (String) bodyMap.get("error_description");
				if (error_description.indexOf("UserDetailsService returned null") >= 0) {
					ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
				} else if (error_description.indexOf("坏的凭证") >= 0) {
					ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
				}
			}

			return null;
		}
		AuthToken authToken = new AuthToken();
		authToken.setAccess_token((String) bodyMap.get("jti")); // 用户身份令牌
		authToken.setRefresh_token((String) bodyMap.get("refresh_token")); // 刷新令牌
		authToken.setJwt_token((String) bodyMap.get("access_token")); // jwt令牌
		return authToken;
	}

	// 存储到redis
	private boolean saveToken(String access_token, String content, long ttl) {
		String key = "user_token:" + access_token;
		stringRedisTemplate.boundValueOps(key).set(content, ttl, TimeUnit.SECONDS);
		Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
		return expire > 0;
	}
	
	// 从redis删除token
	public boolean delToken(String access_token) {
		String key = "user_token:" + access_token;
		stringRedisTemplate.delete(key);
		return true;
	}
	
	
	// 从redis查询令牌
	public AuthToken getUserToken(String token) {
		String key = "user_token:" + token;
		// 从redis中取到令牌信息
		String value = stringRedisTemplate.opsForValue().get(key);
		// 转成对象
		try {
			AuthToken authToken = JSON.parseObject(value, AuthToken.class);
			return authToken;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 获取httpbasic的串
	private String getHttpBasic(String clientId, String clientSecret) {
		String string = clientId + ":" + clientSecret;
		// 将串进行Base64编码
		byte[] encode = Base64Utils.encode(string.getBytes());
		return "Basic " + new String(encode);
	}

	// 将令牌存储到cookie
	public void saveCookie(String token) {
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, cookieMaxAge, false);
	}
	
	// 从cookie中删除令牌
	public void clearCookie(String token) {
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
		CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, 0, false);
	}
	
	// 取出cookie中的身份令牌
	public String getTokenFormCookie() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		Map<String, String> map = CookieUtil.readCookie(request, "uid");
		if (map != null || map.get("uid") != null) {
			return map.get("uid");
		}
		return null;
	}
	
}
