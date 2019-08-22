package com.xuecheng.manage_cms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;

@Service
public class TemplateService {
	@Autowired
	private CmsTemplateRepository cmsTemplateRepository;
	
	public QueryResponseResult findAllTemp() {
		List<CmsTemplate> list = cmsTemplateRepository.findAll();
		QueryResult<CmsTemplate> queryResult = new QueryResult<>();
		queryResult.setList(list);
		queryResult.setTotal(list.size());
		return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
	}
}
