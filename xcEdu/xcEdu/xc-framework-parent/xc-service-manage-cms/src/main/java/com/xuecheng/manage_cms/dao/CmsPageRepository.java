package com.xuecheng.manage_cms.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.xuecheng.framework.domain.cms.CmsPage;

public interface CmsPageRepository extends MongoRepository<CmsPage, String>{

	public CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);

}
