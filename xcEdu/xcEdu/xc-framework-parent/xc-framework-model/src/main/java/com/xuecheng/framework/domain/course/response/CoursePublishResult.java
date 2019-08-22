package com.xuecheng.framework.domain.course.response;

import java.io.Serializable;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class CoursePublishResult extends ResponseResult{
	private String previewUrl;
	public CoursePublishResult(ResultCode resultCode,String previewUrl) {
		super(resultCode);
		this.previewUrl = previewUrl;
	}
}
