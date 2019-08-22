package com.xuecheng.ucenter.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xuecheng.framework.domain.ucenter.XcUser;

public interface XcUserRepository extends JpaRepository<XcUser, String>{
	
	//根据姓名查询学生
	public XcUser findByUsername(String username);
	
}
