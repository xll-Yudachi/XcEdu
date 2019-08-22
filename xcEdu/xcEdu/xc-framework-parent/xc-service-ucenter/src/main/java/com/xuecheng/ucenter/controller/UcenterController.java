package com.xuecheng.ucenter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xuecheng.api.ucenter.UcenterControllerApi;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.service.UserService;

@RestController
@RequestMapping("/ucenter")
public class UcenterController implements UcenterControllerApi{

	@Autowired
	private UserService userService;
	
	@Override
	@GetMapping("/getuserext")
	public XcUserExt getUserext(String username) {
		return userService.getUserExt(username);
	}
}
