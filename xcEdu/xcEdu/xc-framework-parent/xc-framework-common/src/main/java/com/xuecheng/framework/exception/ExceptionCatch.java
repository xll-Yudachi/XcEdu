package com.xuecheng.framework.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;

/**
 * @ClassName: ExceptionCatch   
 * @Description: 统一异常捕获类
 * @author: Yudachi
 * @date: 2019年6月9日 下午10:07:26
 * @version V1.0
 */
@ControllerAdvice	//控制器增强
public class ExceptionCatch {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);
	
	//谷歌的ImmutableMap是线程安全且不可修改的map
	private static ImmutableMap<Class<? extends Throwable>, ResultCode> EXCEPTIONS;
	//定义map的builder对象，去构建ImmutableMap
	protected static ImmutableMap.Builder<Class<? extends Throwable>, ResultCode> Builder = ImmutableMap.builder();
	
	
	//捕获CustomException此类异常
	@ExceptionHandler(CustomException.class)
	@ResponseBody
	public ResponseResult customException(CustomException customException) {
		//记录日志
		LOGGER.error("catch Exception:{}",customException.getMessage());
		ResultCode resultCode = customException.getResultCode();
		return new ResponseResult(resultCode);
	
	}
	
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseResult exception(Exception exception) {
		//记录日志
		LOGGER.error("catch Exception:{}",exception.getMessage());
		if(EXCEPTIONS == null) {
			EXCEPTIONS = Builder.build();
		}
		ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
		if(resultCode != null) {
			return new ResponseResult(resultCode);
		}else {
			return new ResponseResult(CommonCode.SERVER_ERROR);
		}
	}
	
	static {
		//在这里加入一些基础的异常类型判断
		Builder.put(HttpMessageNotReadableException.class,CommonCode.INVALID_PARAM);
	}
}
