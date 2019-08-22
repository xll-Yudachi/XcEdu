package com.xuecheng.manage_media.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xuecheng.api.media.MediaFileControllerApi;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.MediaFileService;

@RestController
@RequestMapping("/media/file")
public class MediaFileController implements MediaFileControllerApi{

	@Autowired
	private MediaFileService mediaFileService;
	
	@Override
	@GetMapping("/list/{page}/{size}")
	public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
		return mediaFileService.findList(page,size,queryMediaFileRequest);
	}

	@Override
	public ResponseResult process(String id) {
		return null;
	}
	
	
}
