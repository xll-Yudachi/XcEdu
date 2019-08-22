package com.xuecheng.search.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;

@Service
public class EsCourseService {

	@Value("${xuecheng.elasticsearch.course.index}")
	private String es_index;
	@Value("${xuecheng.elasticsearch.course.type}")
	private String es_type;
	@Value("${xuecheng.elasticsearch.course.source_field}")
	private String source_field;
	@Value("${xuecheng.elasticsearch.media.index}")
	private String media_es_index;
	@Value("${xuecheng.elasticsearch.media.type}")
	private String media_es_type;
	@Value("${xuecheng.elasticsearch.media.source_field}")
	private String media_source_field;
	
	@Autowired
	private RestHighLevelClient restHighLevelClient;

	// 关键字查询
	public QueryResponseResult<CoursePub> searchList(int page, int size, CourseSearchParam courseSearchParam) {
		// 设置索引
		SearchRequest searchRequest = new SearchRequest(es_index);
		// 设置类型
		searchRequest.types(es_type);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		// source源字段过滤
		String[] source_fields = source_field.split(",");
		searchSourceBuilder.fetchSource(source_fields, new String[] {});
		// 关键字
		if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
			// 匹配关键字
			MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders
					.multiMatchQuery(courseSearchParam.getKeyword(), "name", "teachplan", "description");
			// 设置配置占比
			multiMatchQueryBuilder.minimumShouldMatch("70%");
			// 提升课程名字段的boost值
			multiMatchQueryBuilder.field("name", 10);
			boolQueryBuilder.must(multiMatchQueryBuilder);
		}
		//根据一级分类查询
		if(StringUtils.isNotEmpty(courseSearchParam.getMt())) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
		}
		//根据二级分类查询
		if(StringUtils.isNotEmpty(courseSearchParam.getSt())) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
		}
		//根据难度等级查询
		if(StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
		}
		
		// 布尔查询
		searchSourceBuilder.query(boolQueryBuilder);
		//设置分页参数
		if(page <= 0) {
			page = 1;
		}
		if(size <= 0) {
			size = 12;
		}
		//起始记录下标
		int from = (page-1)*size;
		searchSourceBuilder.from(from);
		searchSourceBuilder.size(size);
		
		//设置高亮
		HighlightBuilder highlightBuilder = new HighlightBuilder();
		highlightBuilder.preTags("<font class='eslight'>");
		highlightBuilder.postTags("</font>");
		//设置高亮字段
		highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
		searchSourceBuilder.highlighter(highlightBuilder);
		
		// 请求搜索
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = null;
		try {
			searchResponse = restHighLevelClient.search(searchRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 结果集处理
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		// 记录总数
		long totalHits = hits.getTotalHits();
		// 数据列表
		List<CoursePub> list = new ArrayList<>();
		for (SearchHit hit : searchHits) {
			CoursePub coursePub = new CoursePub();
			// 取出source
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			
			//课程id
			String id = (String)sourceAsMap.get("id");
			coursePub.setId(id);
			
			// 取出课程名
			String name = (String) sourceAsMap.get("name");
			
			//取出高亮字段课程名(name)
			Map<String, HighlightField> highlightFields = hit.getHighlightFields();
			if(highlightFields != null) {
				HighlightField highlightFieldName = highlightFields.get("name");
				if(highlightFieldName != null) {
					Text[] fragments = highlightFieldName.fragments();
					StringBuffer stringBuffer = new StringBuffer();
					for(Text text : fragments) {
						stringBuffer.append(text);
					}
					name = stringBuffer.toString();
				}
			}
			
			coursePub.setName(name);
			
			// 图片
			String pic = (String) sourceAsMap.get("pic");
			coursePub.setPic(pic);
			// 价格
			Double price = null;
			price = (Double)sourceAsMap.get("price");
			coursePub.setPrice(price);
			// 原价
			Double price_old = null;
			price_old = (Double)sourceAsMap.get("price_old");
			coursePub.setPrice_old(price_old);
			
			list.add(coursePub);
		}
		
		QueryResult<CoursePub> queryResult = new QueryResult<>();
		queryResult.setList(list);
		queryResult.setTotal(totalHits);
		QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
		
		return queryResponseResult;
	}
	
	public Map<String, CoursePub> getall(String id){
		//设置索引库
		SearchRequest searchRequest = new SearchRequest(es_index);
		//设置类型
		searchRequest.types(es_type);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		//查询条件设置,根据课程id查询
		searchSourceBuilder.query(QueryBuilders.termQuery("id", id));
		//注入条件设置
		searchRequest.source(searchSourceBuilder);
		//请求结果
		SearchResponse searchResponse = null;
		try {
			//执行搜索
			searchResponse = restHighLevelClient.search(searchRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//获取搜索结果
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		Map<String, CoursePub> map = new HashMap<String, CoursePub>();
		for(SearchHit hit : searchHits) {
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			String courseId = (String) sourceAsMap.get("id");
			String name = (String) sourceAsMap.get("name");
			String grade = (String) sourceAsMap.get("grade");
			String charge = (String) sourceAsMap.get("charge");
			String pic = (String) sourceAsMap.get("pic");
			String description = (String) sourceAsMap.get("description");
			String teachplan = (String) sourceAsMap.get("teachplan");
			CoursePub coursePub = new CoursePub();
			coursePub.setId(courseId);
			coursePub.setName(name);
			coursePub.setPic(pic);
			coursePub.setGrade(grade);
			coursePub.setTeachplan(teachplan);
			coursePub.setDescription(description);
			coursePub.setCharge(charge);
			map.put(courseId,coursePub);
		}
		return map;
	}
	
	public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {
		//设置索引
		SearchRequest searchRequest = new SearchRequest(media_es_index);
		//设置类型
		searchRequest.types(media_es_type);
		//设置搜索源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		//source源字段过滤
		String[] source_fields = media_source_field.split(",");
		searchSourceBuilder.fetchSource(source_fields, new String[]{});
		//设置查询条件，根据课程计划id查询(可传入多个id)
		searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id", teachplanIds));
		searchRequest.source(searchSourceBuilder);
		
		//搜索结果对象
		SearchResponse searchResponse = null;
		
		try {
			//执行搜索
			searchResponse = restHighLevelClient.search(searchRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//获取搜索结果
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		Map<String, CoursePub> map = new HashMap<>();
		//数据列表
		List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
		for(SearchHit hit : searchHits) {
			TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			//取出课程计划媒资信息
			String courseid = (String) sourceAsMap.get("courseid");
			String media_id = (String) sourceAsMap.get("media_id");
			String media_url = (String) sourceAsMap.get("media_url");
			String teachplan_id = (String) sourceAsMap.get("teachplan_id");
			String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
			
			teachplanMediaPub.setCourseId(courseid);
			teachplanMediaPub.setMediaUrl(media_url);
			teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
			teachplanMediaPub.setMediaId(media_id);
			teachplanMediaPub.setTeachplanId(teachplan_id);
			
			//将数据加入列表
			teachplanMediaPubList.add(teachplanMediaPub);
		}
		
		//构建返回课程媒资信息对象
		QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
		queryResult.setList(teachplanMediaPubList);
		QueryResponseResult<TeachplanMediaPub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
		return queryResponseResult;
	}
}
