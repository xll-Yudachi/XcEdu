package com.xuecheng.api.filesystem;

import org.springframework.web.multipart.MultipartFile;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="文件管理接口",description="文件管理接口，提供文件的增、删、查、改")
public interface FileSystemControllerApi {

	//上传文件
	@ApiOperation("上传文件接口")
	public UploadFileResult upload(MultipartFile multipartFile,String filetag,String businesskey,String metadata);
}
