package com.xuecheng.file_system.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.xuecheng.framework.domain.filesystem.FileSystem;

public interface FileSystemRepository extends MongoRepository<FileSystem, String>{

}
