package com.xuecheng.file_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xuecheng.api.filesystem.FileSystemControllerApi;
import com.xuecheng.file_system.service.FileSystemService;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;

@RestController
@RequestMapping("/filesystem")
public class FileSystemController implements FileSystemControllerApi{

	@Autowired
	private FileSystemService fileSystemService;
	
	@Override
	@PostMapping("/upload")
	public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata) {
		return fileSystemService.upload(multipartFile, filetag, businesskey, metadata);
	}

}
