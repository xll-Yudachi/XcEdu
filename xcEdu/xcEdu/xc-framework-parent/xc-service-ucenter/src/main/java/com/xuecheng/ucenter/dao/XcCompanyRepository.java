package com.xuecheng.ucenter.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;

public interface XcCompanyRepository extends JpaRepository<XcCompanyUser, String>{
	
	//根据用户Id查询该用户所属公司id
	public XcCompanyUser findByUserId(String userId);
	
}
