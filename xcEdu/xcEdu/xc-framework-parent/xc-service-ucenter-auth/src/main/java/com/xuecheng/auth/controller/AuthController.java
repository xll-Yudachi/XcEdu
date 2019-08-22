package com.xuecheng.auth.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;

@RestController
public class AuthController implements AuthControllerApi{
	
	@Value("${auth.clientId}")
	private String clientId;
	@Value("${auth.clientSecret}")
	private String clientSecret;

	@Autowired
	private AuthService authService;
	
	@Override
	@PostMapping("/userlogin")
	public LoginResult login(LoginRequest loginRequest) {
		if(loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())) {
			ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
		}
		if(loginRequest == null || StringUtils.isEmpty(loginRequest.getPassword())) {
			ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
		}
		
		//账号
		String username = loginRequest.getUsername();
		//密码
		String password = loginRequest.getPassword();
		
		//申请令牌
		AuthToken authToken = authService.login(username, password, clientId, clientSecret);
		
		//用户身份令牌
		String access_token = authToken.getAccess_token();
		//将令牌存储到cookie
		authService.saveCookie(access_token);
		
		return new LoginResult(CommonCode.SUCCESS, access_token);
	}
	
	@Override
	@PostMapping("/userlogout")
	public ResponseResult logout() {
		//取出cookie中的用户身份令牌
		String uid = authService.getTokenFormCookie();
		//删除redis中的token
		boolean result = authService.delToken(uid);
		if(!result) {
			return new ResponseResult(CommonCode.FAIL);
		}
		//清除cookie
		authService.clearCookie(uid);
		return new ResponseResult(CommonCode.SUCCESS);
	}

	@Override
	@GetMapping("/userjwt")
	public JwtResult userjwt() {
		//取出cookie中的用户身份令牌
		String uid = authService.getTokenFormCookie();
		if(uid == null) {
			return new JwtResult(CommonCode.FAIL, null);
		}
		
		//拿身份令牌从redis中查询Jwt令牌
		AuthToken userToken = authService.getUserToken(uid);
		if(userToken != null) {
			//将Jwt令牌返回给用户
			String jwt_token = userToken.getJwt_token();
			return new JwtResult(CommonCode.SUCCESS,  jwt_token);
		} 
	
		return null;
	}
	
	
}
