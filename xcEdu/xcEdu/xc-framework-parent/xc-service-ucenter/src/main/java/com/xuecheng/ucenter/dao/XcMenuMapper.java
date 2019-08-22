package com.xuecheng.ucenter.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.xuecheng.framework.domain.ucenter.XcMenu;

@Mapper
public interface XcMenuMapper {
	
	//根据用户id查询用户的权限
	public List<XcMenu> selectPermissionByUserId(String userId);
	
}
