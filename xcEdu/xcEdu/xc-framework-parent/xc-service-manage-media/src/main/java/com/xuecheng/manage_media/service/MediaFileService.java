package com.xuecheng.manage_media.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;

@Service
public class MediaFileService {

	@Autowired
	private MediaFileRepository mediaFileRepository;
	
	public QueryResponseResult<MediaFile> findList(int page,int size,QueryMediaFileRequest queryMediaFileRequest){
		
		if(queryMediaFileRequest == null) {
			queryMediaFileRequest = new QueryMediaFileRequest();
		}
		
		//条件值对象
		MediaFile mediaFile = new MediaFile();
		if(StringUtils.isNotEmpty(queryMediaFileRequest.getTag())) {
			mediaFile.setTag(queryMediaFileRequest.getTag());
		}
		if(StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())) {
			mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
		}
		if(StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())) {
			mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
		}
		
		//条件匹配器
		ExampleMatcher exampleMatcher = ExampleMatcher.matching()
										.withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains())
										.withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains());
		//定义example条件对象
		Example<MediaFile> example = Example.of(mediaFile, exampleMatcher);

		//分页查询对象
		if(page <= 0) {
			page = 1;
		}
		page = page - 1;
		if(size <= 0) {
			size = 10;
		}
		Pageable pageable = PageRequest.of(page, size);
	
		//分页查询
		Page<MediaFile> all = mediaFileRepository.findAll(example, pageable);
		//总记录数
		long total = all.getTotalElements();
		//数据列表
		List<MediaFile> list = all.getContent();
		//返回的数据集
		QueryResult<MediaFile> queryResult = new QueryResult<>();
		queryResult.setList(list);
		queryResult.setTotal(total);
		
		QueryResponseResult<MediaFile> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
		return queryResponseResult;
	}
}
