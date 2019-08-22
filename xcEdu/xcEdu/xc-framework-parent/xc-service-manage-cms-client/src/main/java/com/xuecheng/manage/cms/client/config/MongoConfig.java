package com.xuecheng.manage.cms.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;

@Configuration
public class MongoConfig {

	@Value("${spring.data.mongodb.database}")
	private String db;
	
	@Bean
	public GridFSBucket getGridFSBucket(MongoClient mongoClient) {
		MongoDatabase database = mongoClient.getDatabase(db);
		GridFSBucket bucket = GridFSBuckets.create(database);
		return bucket;
	}
}
