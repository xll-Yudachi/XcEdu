package com.xuecheng.manage_cms.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.xuecheng.framework.domain.cms.CmsConfig;

public interface CmsConfigRepository extends MongoRepository<CmsConfig, String>{

}
