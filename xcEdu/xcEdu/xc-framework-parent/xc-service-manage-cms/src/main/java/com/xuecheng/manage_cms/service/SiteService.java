package com.xuecheng.manage_cms.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;

@Service
public class SiteService {
	
	@Autowired
	private CmsSiteRepository cmsSiteRepository;
	
	public QueryResponseResult findAll() {
		List<CmsSite> list = cmsSiteRepository.findAll();
		for(int i=0;i<list.size();i++) {
			System.err.println(list.get(i));
		}
		QueryResult<CmsSite> queryResult = new QueryResult<>();
		queryResult.setList(list);
		queryResult.setTotal(list.size());
		QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
		return queryResponseResult;
	}
	
	//根据Id查询站点信息
	public CmsSite findCmsSiteById(String siteId) {
		Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
		if(optional.isPresent()) {
			return optional.get();
		}
		return null;
	}
}
