package com.xuecheng.manage.cms.client.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage.cms.client.dao.CmsPageRepository;
import com.xuecheng.manage.cms.client.dao.CmsSiteRepository;

@Service
public class PageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);
	
	@Autowired
	private CmsPageRepository cmsPageRepository;
	@Autowired
	private GridFsTemplate gridFsTemplate;
	@Autowired
	private GridFSBucket gridFSBucket;
	@Autowired
	private CmsSiteRepository cmsSiteRepository;
	
	/**
	 * 保存html页面到服务器的物理路径
	 */
	public void savePageToServerPath(String pageId) {
		//根据页面id查询页面信息
		CmsPage cmsPage = findCmsPageById(pageId);
		if(cmsPage == null) {
			ExceptionCast.cast(CmsCode.CMS_PAGE_NOEXISTS);
		}
		//得到html文件的Id,从cmsPage中获取htmlFileId内容
		String htmlFileId = cmsPage.getHtmlFileId();
		
		//从gridFS中查询html文件
		InputStream inputStream = getFileById(htmlFileId);
		if(inputStream == null) {
			LOGGER.error("getFileById InputStream is null , htmlFileId:{}",htmlFileId);
			return ;
		}
		
		//得到站点id
		String siteId = cmsPage.getSiteId();
		//得到站点的信息
		CmsSite cmsSite = this.findCmsSiteById(siteId);
		if(cmsSite == null) {
			ExceptionCast.cast(CmsCode.CMS_SITE_NOEXISTS);
		}
		//得到站点的物理路径
		String sitePhysicalPath = cmsSite.getSitePhysicalPath();
		
		//得到页面的物理路径
		String pagePath = sitePhysicalPath + cmsPage.getPagePhysicalPath() + cmsPage.getPageName();
		
		System.err.println(pagePath);
		
		//将html文件保存到服务器物理路径上
		FileOutputStream fileOutputStream = null;
		try {
			IOUtils.copy(inputStream, new FileOutputStream(new File(pagePath)));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//根据页面id查询页面信息
	public CmsPage findCmsPageById(String pageId) {
		Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
		if(optional.isPresent()) {
			return optional.get();
		}
		return null;
	}
	
	//根据文件id从GridFS中查询文件内容
	public InputStream getFileById(String fileId) {
		//文件对象
		GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
		//打开文件下载流
		GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
		//定义下载器资源
		GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
		try {
			return gridFsResource.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//根据站点id查询站点信息
	public CmsSite findCmsSiteById(String siteId) {
		Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
		if(optional.isPresent()) {
			return optional.get();
		}
		return null;
	}
}
