package com.xuecheng.framework.domain.cms.request;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class CmsPostPageResult extends ResponseResult{
	 private String pageUrl;
	 public CmsPostPageResult(ResultCode resultCode,String pageUrl) {
		 super(resultCode);
		 this.pageUrl = pageUrl;
	 }
}
