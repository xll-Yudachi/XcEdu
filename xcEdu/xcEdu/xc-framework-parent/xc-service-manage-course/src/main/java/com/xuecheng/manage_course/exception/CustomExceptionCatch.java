package com.xuecheng.manage_course.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.xuecheng.framework.exception.ExceptionCatch;
import com.xuecheng.framework.model.response.CommonCode;

/**
 * @ClassName: CustomExceptionCatch   
 * @Description: 课程管理自定义的异常类，其中定义异常类型所对应的错误代码
 * @author: Yudachi
 * @date: 2019年8月6日 下午6:57:45
 * @version V1.0
 */
//控制器增强
@ControllerAdvice
public class CustomExceptionCatch extends ExceptionCatch{
	static {
		Builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
	}
}
