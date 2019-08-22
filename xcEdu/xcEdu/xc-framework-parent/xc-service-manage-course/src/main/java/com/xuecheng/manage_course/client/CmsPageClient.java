package com.xuecheng.manage_course.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.CmsPostPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;

@FeignClient(value = "xc-service-manage-cms")	//指定远程调用的服务名
public interface CmsPageClient {
	
	//添加页面，用于课程预览
	@PostMapping("/cms/page/save")
	public CmsPageResult saveCmsPage(@RequestBody CmsPage cmsPage);
	
	//一键发布页面
	@PostMapping("/cms/page/postPageQuick")
	public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage);

}
