package com.xuecheng.search;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestIndex {

	@Autowired
	private RestHighLevelClient highLevelClient;
	@Autowired
	private RestClient restClient;
	
	//创建索引库
	@Test
	public void testCreateIndex() throws Exception{
		//创建索引对象
		CreateIndexRequest createIndexRequest = new CreateIndexRequest("xcedu_course");
		//设置索引对象参数
		createIndexRequest.settings(Settings.builder().put("number_of_shards","1").put("number_of_replicas","0"));
		//操作索引的客户端
		IndicesClient indicesClient = highLevelClient.indices();
		//执行创建索引操作
		CreateIndexResponse createIndexResponse = indicesClient.create(createIndexRequest);
		//得到响应
		boolean acknowledged = createIndexResponse.isAcknowledged();
		
		System.out.println(acknowledged);
	}	
	
	//删除索引库
	@Test
	public void testDeleteIndex() throws Exception{
		//删除索引对象
		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_course");
		//操作索引的客户端
		IndicesClient indicesClient = highLevelClient.indices();
		//执行删除索引
		DeleteIndexResponse deleteIndexResponse = indicesClient.delete(deleteIndexRequest);
		//得到响应
		boolean acknowledged = deleteIndexResponse.isAcknowledged();
	
		System.out.println(acknowledged);
	}
	
	
}
