package com.xuecheng.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

	@Autowired
	private RestHighLevelClient highLevelClient;
	@Autowired
	private RestClient restClient;

	// 搜索全部记录
	@Test
	public void testSearchAll() throws Exception {

		// 日期格式化对象
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 搜索请求对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 指定类型
		searchRequest.types("doc");
		// 搜索源构建对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 设置搜索方式 matchAllQuery搜索全部
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		// 设置source源字段过滤 第一个参数结果集表示查询包括哪些字段 第二个参数结果集表示查询不包括哪些字段
		searchSourceBuilder.fetchSource(new String[] { "name", "studymodel", "price", "timestamp" }, new String[] {});
		// 向搜索请求对象中设置搜索源
		searchRequest.source(searchSourceBuilder);
		// 执行搜索，向ES发起http请求
		SearchResponse searchResponse = highLevelClient.search(searchRequest);
		// 搜索结果
		SearchHits hits = searchResponse.getHits();
		// 匹配到的总记录数
		long totalHits = hits.getTotalHits();
		// 得到匹配度高的文档
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit : searchHits) {
			// 文档的主键
			String id = hit.getId();
			// 源文档的内容
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			String name = (String) sourceAsMap.get("name");
			// 描述 由于前面设置了源文档字段过滤 这时description是取不到的
			String description = (String) sourceAsMap.get("description");
			// 学习模式
			String studymodel = (String) sourceAsMap.get("studymodel");
			// 价格
			Double price = (Double) sourceAsMap.get("price");
			// 日期
			Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));

			System.out.println(id);
			System.out.println(name);
			System.out.println(studymodel);
			System.out.println(price);
			System.out.println(timestamp);
		}
	}

	// 分页查询记录
	@Test
	public void testSearchPage() throws Exception {

		// 日期格式化对象
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 搜索请求对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 指定类型
		searchRequest.types("doc");
		// 搜索源构建对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		//设置分页参数
		int page = 2;
		int size = 1;
		int from = (page-1)*size;
		searchSourceBuilder.from(from);	//从多少个数据开始
		searchSourceBuilder.size(size);	//显示多少个数据
		// 设置搜索方式 matchAllQuery搜索全部
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		// 设置source源字段过滤 第一个参数结果集表示查询包括哪些字段 第二个参数结果集表示查询不包括哪些字段
		searchSourceBuilder.fetchSource(new String[] { "name", "studymodel", "price", "timestamp" }, new String[] {});
		// 向搜索请求对象中设置搜索源
		searchRequest.source(searchSourceBuilder);
		// 执行搜索，向ES发起http请求
		SearchResponse searchResponse = highLevelClient.search(searchRequest);
		// 搜索结果
		SearchHits hits = searchResponse.getHits();
		// 匹配到的总记录数
		long totalHits = hits.getTotalHits();
		// 得到匹配度高的文档
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit : searchHits) {
			// 文档的主键
			String id = hit.getId();
			// 源文档的内容
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			String name = (String) sourceAsMap.get("name");
			// 描述 由于前面设置了源文档字段过滤 这时description是取不到的
			String description = (String) sourceAsMap.get("description");
			// 学习模式
			String studymodel = (String) sourceAsMap.get("studymodel");
			// 价格
			Double price = (Double) sourceAsMap.get("price");
			// 日期
			Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));

			System.out.println(id);
			System.out.println(name);
			System.out.println(studymodel);
			System.out.println(price);
			System.out.println(timestamp);
		}
	}
}
