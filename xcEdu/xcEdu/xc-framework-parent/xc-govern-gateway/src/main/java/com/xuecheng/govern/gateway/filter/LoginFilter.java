package com.xuecheng.govern.gateway.filter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.govern.gateway.service.AuthService;

@Component
public class LoginFilter extends ZuulFilter{

	@Autowired
	private AuthService authService;
	
	// 过滤器执行内容
	@Override
	public Object run() throws ZuulException {
		RequestContext requestContext = RequestContext.getCurrentContext();
		//得到request
		HttpServletRequest request = requestContext.getRequest();

		//取cookie中的身份令牌
		String tokenFromCookie = authService.getTokenFromCookie(request);
		if(StringUtils.isEmpty(tokenFromCookie)) {
			//拒绝访问
			authService.access_denied();
			return null;
		}
		
		//取header头信息中的jwt
		String jwtFromHeader = authService.getJwtFromHeader(request);
		if(StringUtils.isEmpty(jwtFromHeader)) {
			//拒绝访问
			authService.access_denied();
			return null;
		}
		
		//从redis取出jwt的过期时间
		long expire = authService.getExpire(tokenFromCookie);
		if(expire < 0) {
			//拒绝访问
			authService.access_denied();
			return null;
		}
		
		return null;
	}

	// 该过滤器是否需要执行
	@Override
	public boolean shouldFilter() {
		return true;
	}

	// int值来定义过滤器的执行顺序，数值越小优先级越高
	@Override
	public int filterOrder() {
		return 0;
	}
	
	/**
	 * pre: 请求在被路由转发之前执行
	 * routing: 在路由请求时调用
	 * post: 在routing和error过滤器之后调用
	 * error: 处理请求时发生错误调用
	 */
	@Override
	public String filterType() {
		return "pre";
	}
	
}
