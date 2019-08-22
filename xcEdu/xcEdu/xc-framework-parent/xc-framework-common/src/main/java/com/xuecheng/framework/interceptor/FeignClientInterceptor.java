package com.xuecheng.framework.interceptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;

//微服务远程调用间的拦截器
public class FeignClientInterceptor implements RequestInterceptor{

	@Override
	public void apply(RequestTemplate requestTemplate) {

		ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if(requestAttributes == null) {
			HttpServletRequest  request = requestAttributes.getRequest();
			//取出当前请求的header，找到jwt令牌
			Enumeration<String> headers = request.getHeaderNames();
			if(headers != null) {
				while(headers.hasMoreElements()) {
					String headerName = headers.nextElement();
					String headerValue = request.getHeader(headerName);
					//将header向下传递
					requestTemplate.header(headerName, headerValue);
				}
			}
		}
	}

}
