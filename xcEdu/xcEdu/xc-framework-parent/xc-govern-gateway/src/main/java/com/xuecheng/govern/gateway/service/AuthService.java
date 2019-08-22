package com.xuecheng.govern.gateway.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.context.RequestContext;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;

@Service
public class AuthService {
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	//从cookie中查询Token身份令牌
	public String getTokenFromCookie(HttpServletRequest request) {
		Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
		String access_token = cookieMap.get("uid");
		if(StringUtils.isEmpty(access_token)) {
			return null;
		}
		return access_token;
	}
	
	//从头信息中查询Jwt令牌
	public String getJwtFromHeader(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if(StringUtils.isEmpty(authorization)) {
			//拒绝访问
			return null;
		}
		if(!authorization.startsWith("Bearer ")) {
			//拒绝访问
			return null;
		}
		return authorization;
	}
	
	//查询令牌的有效期
	public long getExpire(String access_token) {
		//token在redis中的Key
		String key = "user_token:" + access_token;
		Long expire = stringRedisTemplate.getExpire(key);
		return expire;
	}
	
	//拒绝访问
	public void access_denied() {
		RequestContext requestContext = RequestContext.getCurrentContext();
		HttpServletResponse response = requestContext.getResponse();
		//拒绝访问
		requestContext.setSendZuulResponse(false);
		//设置响应代码
		requestContext.setResponseStatusCode(200);
		//构建响应的信息
		ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
		//转成json
		String jsonString = JSON.toJSONString(responseResult);
		requestContext.setResponseBody(jsonString);
		//转成json后 设置contentType
		response.setContentType("application/json;charset=utf-8");
	}
}
