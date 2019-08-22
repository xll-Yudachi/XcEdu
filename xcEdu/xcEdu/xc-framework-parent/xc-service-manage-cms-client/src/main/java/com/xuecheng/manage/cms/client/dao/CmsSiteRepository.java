package com.xuecheng.manage.cms.client.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.xuecheng.framework.domain.cms.CmsSite;

public interface CmsSiteRepository extends MongoRepository<CmsSite, String>{

}
