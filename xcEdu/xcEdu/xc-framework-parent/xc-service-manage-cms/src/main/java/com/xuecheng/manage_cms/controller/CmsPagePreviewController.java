package com.xuecheng.manage_cms.controller;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;

@Controller
public class CmsPagePreviewController extends BaseController{
	
	@Autowired
	private PageService pageService;
	
	//页面预览
	@RequestMapping(value="/cms/preview/{pageId}",method = RequestMethod.GET)
	public void preview(@PathVariable("pageId") String pageId) throws IOException{
		
		//执行静态化
		String pageHtml = pageService.getPageHtml(pageId);
		//通过response对象将内容输出到浏览器
		ServletOutputStream outputStream = response.getOutputStream();
		//返回的页面中使用到了SSI包含技术 必须用Nginx解析并且页面必须指定为html
		response.setHeader("Content-type", "text/html;charset=utf-8");
		outputStream.write(pageHtml.getBytes("utf-8"));
	}
}
