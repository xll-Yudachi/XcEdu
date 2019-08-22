package com.xuecheng.manage_cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;

@Service
public class SysDictionaryService {

	@Autowired
	private SysDictionaryRepository sysDictionaryRepository;
	
	public SysDictionary findSysDictionaryByDType(String d_type){
		return sysDictionaryRepository.findByDType(d_type).get(0);
	}
}
