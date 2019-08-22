package com.xuecheng.manage_cms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xuecheng.api.cms.CmsSysDictionaryApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.service.SysDictionaryService;

@RestController
@RequestMapping("/sys/dictionary")
public class CmsSysDictionaryController implements CmsSysDictionaryApi{

	@Autowired
	private SysDictionaryService sysDictionaryService;
	
	@Override
	@GetMapping("/get/{dType}")
	public SysDictionary findSysDictionaryByDType(@PathVariable("dType") String d_type) {
		 return sysDictionaryService.findSysDictionaryByDType(d_type);
	}

}
