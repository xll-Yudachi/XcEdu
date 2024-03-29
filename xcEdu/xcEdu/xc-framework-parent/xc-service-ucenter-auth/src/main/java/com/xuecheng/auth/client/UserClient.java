package com.xuecheng.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;

@FeignClient(value = XcServiceList.XC_SERVICE_UCENTER)
public interface UserClient {
	// 根据账号查询用户信息
	@GetMapping("/ucenter/getuserext")
	public XcUserExt getUserext(@RequestParam("username") String username);
	
}
