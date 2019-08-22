package com.xuecheng.manage_cms.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.xuecheng.framework.domain.system.SysDictionary;

public interface SysDictionaryRepository extends MongoRepository<SysDictionary, String>{

	public List<SysDictionary> findByDType(String d_type);
}
