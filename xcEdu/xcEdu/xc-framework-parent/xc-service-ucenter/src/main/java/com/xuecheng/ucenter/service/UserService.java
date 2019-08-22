package com.xuecheng.ucenter.service;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;

@Service
public class UserService {
	
	@Autowired
	private XcCompanyRepository xcCompanyRepository;
	@Autowired
	private XcUserRepository xcUserRepository;
	@Autowired
	private XcMenuMapper xcMenuMapper;
	
	//根据账号查询用户信息
	public XcUser findXcUserByUsername(String username) {
		return xcUserRepository.findByUsername(username);
	}
	
	//根据账号查询用户信息
	public XcUserExt getUserExt(String username) {
		//根据账户查询xcUser信息
		XcUser xcUser = findXcUserByUsername(username);
		if(xcUser == null) {
			return null;
		}
		//用户Id
		String userId = xcUser.getId();
		//查询用户所有的权限
		List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
		
		//根据用户Id查询用户所属公司Id
		XcCompanyUser xcCompanyUser = xcCompanyRepository.findByUserId(xcUser.getId());
		//取到用户公司的id
		String companyId = null;
		if(xcCompanyUser != null) {
			companyId = xcCompanyUser.getCompanyId();
		}
		XcUserExt xcUserExt = new XcUserExt();
		BeanUtils.copyProperties(xcUser, xcUserExt);
		xcUserExt.setCompanyId(companyId);
		//设置用户权限
		xcUserExt.setPermissions(xcMenus);
		return xcUserExt;
	}
	
}
