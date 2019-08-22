package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * @ClassName: CustomException   
 * @Description: 自定义异常类
 * @author: Yudachi
 * @date: 2019年6月9日 下午10:00:19
 * @version V1.0
 */
public class CustomException extends RuntimeException{

	public ResultCode resultCode;
	
	public CustomException(ResultCode resultCode) {
		this.resultCode = resultCode;
	}
	public ResultCode getResultCode() {
		return resultCode;
	}
	
}
