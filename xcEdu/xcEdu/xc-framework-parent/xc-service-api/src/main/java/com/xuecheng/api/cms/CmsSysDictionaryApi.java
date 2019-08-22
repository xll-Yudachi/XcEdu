package com.xuecheng.api.cms;

import java.util.List;

import com.xuecheng.framework.domain.system.SysDictionary;

import io.swagger.annotations.Api;

@Api(value="系统属性配置",description = "系统属性配置查询")
public interface CmsSysDictionaryApi {

	//系统属性配置查询
	public SysDictionary findSysDictionaryByDType(String d_type);
	
}
