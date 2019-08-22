package com.xuecheng.manage_media.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xuecheng.api.media.MediaUploadControllerApi;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.MediaUploadService;

@RestController
@RequestMapping("/media/upload")
public class MediaUploadController implements MediaUploadControllerApi{

	@Autowired
	private MediaUploadService mediaUploadService;
	
	//文件上传前的准备
	@Override
	@PostMapping("/register")
	public ResponseResult register(String fileMd5, String fileName, String mimetype, String fileExt, Long fileSize) {
		return mediaUploadService.register(fileMd5,fileName,mimetype,fileExt,fileSize);
	}

	@Override
	@PostMapping("/checkchunk")
	public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
		return mediaUploadService.checkchunk(fileMd5, chunk, chunkSize);
	}

	@Override
	@PostMapping("/uploadchunk")
	public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
		// TODO Auto-generated method stub
		return mediaUploadService.uploadchunk(file, chunk, fileMd5);
	}

	@Override
	@PostMapping("/mergechunks")
	public ResponseResult mergechunks(String fileMd5, String fileName, String mimetype, String fileExt, Long fileSize) {
		return mediaUploadService.mergechunks(fileMd5, fileName, mimetype, fileExt, fileSize) ;
	}

}
