package com.xuecheng.manage_cms.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.xuecheng.framework.domain.cms.CmsTemplate;

public interface CmsTemplateRepository extends MongoRepository<CmsTemplate, String>{

}
