package com.xuecheng.manage_media.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.xuecheng.framework.domain.media.MediaFile;

public interface MediaFileRepository extends MongoRepository<MediaFile, String>{

}
