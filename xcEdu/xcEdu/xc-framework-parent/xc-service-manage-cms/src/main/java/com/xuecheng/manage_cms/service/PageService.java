package com.xuecheng.manage_cms.service;

import static org.hamcrest.CoreMatchers.nullValue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.RequestToViewNameTranslator;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.CmsPostPageResult;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class PageService {

	@Autowired
	private CmsPageRepository cmsPageRepository;
	@Autowired
	private CmsConfigRepository cmsConfigRepository;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private CmsTemplateRepository cmsTemplateRepository;
	@Autowired
	private GridFsTemplate gridFsTemplate;
	@Autowired
	private GridFSBucket gridFSBucket;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private SiteService siteService;
	
	public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
		if (queryPageRequest == null) {
			queryPageRequest = new QueryPageRequest();
		}

		// 自定义条件查询
		// 定义条件匹配器
		ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("pageAliase",
		ExampleMatcher.GenericPropertyMatchers.contains());
		// 条件值对象
		CmsPage cmsPage = new CmsPage();
		// 设置条件值(站点id)
		if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
			cmsPage.setSiteId(queryPageRequest.getSiteId());
		}
		// 设置模板Id作为查询条件
		if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
			cmsPage.setTemplateId(queryPageRequest.getTemplateId());
		}
		// 设置页面别名作为查询条件
		if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
			cmsPage.setPageAliase(queryPageRequest.getPageAliase());
		}

		// 定义条件对象Example
		Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

		if (page <= 0) {
			page = 1;
		}
		page = page - 1;
		if (size <= 0) {
			size = 10;
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<CmsPage> pageData = cmsPageRepository.findAll(example, pageable);
		QueryResult queryResult = new QueryResult<>();
		queryResult.setList(pageData.getContent());
		queryResult.setTotal(pageData.getTotalElements());
		QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);

		return queryResponseResult;
	}

	public CmsPageResult add(CmsPage cmsPage) {
		CmsPage temp = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(),
				cmsPage.getSiteId(), cmsPage.getPageWebPath());
		if (temp == null) {
			cmsPage.setPageId(null);
			cmsPageRepository.save(cmsPage);
			return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
		}
		// 添加失败
		return new CmsPageResult(CommonCode.FAIL, null);
	}

	// 根据页面Id查询页面
	public CmsPage findById(String id) {
		Optional<CmsPage> optional = cmsPageRepository.findById(id);
		if (optional.isPresent()) {
			CmsPage cmsPage = optional.get();
			return cmsPage;
		}
		return null;
	}

	// 修改页面
	public CmsPageResult update(String id,CmsPage cmsPage) {
		//根据Id从数据查询页面信息
		CmsPage temp = findById(id);
		if(temp != null) {
			//更新模板id
			temp.setTemplateId(cmsPage.getTemplateId());
			//更新所属站点
			temp.setSiteId(cmsPage.getSiteId());
				//更新页面别名
			temp.setPageAliase(cmsPage.getPageAliase());
			//更新页面名称
			temp.setPageName(cmsPage.getPageName());
			//更新访问路径
			temp.setPageWebPath(cmsPage.getPageWebPath());
			//更新物理路径
			temp.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
			//更新dataUrl
			temp.setDataUrl(cmsPage.getDataUrl());
			cmsPageRepository.save(temp);
			return new CmsPageResult(CommonCode.SUCCESS, temp);
		}
		//修改失败
		return new CmsPageResult(CommonCode.FAIL, null);
	}
	//根据id删除页面
	public ResponseResult delete(String id) {
		Optional<CmsPage> optional = cmsPageRepository.findById(id);
		if(optional.isPresent()) {
			cmsPageRepository.deleteById(id);
			return new ResponseResult(CommonCode.SUCCESS);
		}
		return new ResponseResult(CommonCode.FAIL);
	}
	
	//根据id查询CmsConfig
	public CmsConfig getConfigById(String id) {
		Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
		if(optional.isPresent()) {
			CmsConfig cmsConfig = optional.get();
			return cmsConfig;
		}
		return null;
	}
	
	//页面静态化
	public String getPageHtml(String pageId) {
		//获取模型数据
		Map model = getModelByPageId(pageId);
		if(model == null) {
			//数据模型获取不到
			ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
		}
		//获取页面的模板信息
		String template = getTemplateByPageId(pageId);
		if(StringUtils.isEmpty(template)) {
			ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
		}
		//执行静态化
		String html = generateHtml(template, model);
		
		return html;
	}
	
	
	//获取数据模型
	private Map getModelByPageId(String pageId) {
		//取出页面信息
		CmsPage cmsPage = findById(pageId);
		if(cmsPage == null) {
			ExceptionCast.cast(CmsCode.CMS_PAGE_NOEXISTS);
		}
		//取出页面的dataUrl
		String dataUrl = cmsPage.getDataUrl();
		if(StringUtils.isEmpty(dataUrl)) {
			//页面dataUrl为空
			ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
		}
		//通过restTemplate请求dataUrl获取数据
		ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
		Map body = forEntity.getBody();
		return body;
	}
	
	//获取页面模板信息
	private String getTemplateByPageId(String pageId) {
		//取出页面的信息
		CmsPage cmsPage = findById(pageId);
		if(cmsPage == null) {
			ExceptionCast.cast(CmsCode.CMS_PAGE_NOEXISTS);
		}
		//获取页面模板Id
		String templateId = cmsPage.getTemplateId();
		if(StringUtils.isEmpty(templateId)) {
			ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
		}
		//查询模板信息
		Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
		if(optional.isPresent()) {
			CmsTemplate cmsTemplate = optional.get();
			//获取模板文件id
			String templateFieldId = cmsTemplate.getTemplateFileId();
			//从GridFS中取模板文件内容
			//根据文件ID查询文件
			GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFieldId)));
			//打开一个下载流对象
			GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
			//创建GridFsResource对象，获取流
			GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
			//从流中获取数据
			try {
				String content = IOUtils.toString(gridFsResource.getInputStream(),"utf-8");
				return content;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	//执行静态化
	private String generateHtml(String templateContent,Map model) {
		//创建配置对象
		Configuration configuration = new Configuration(Configuration.getVersion());
		//创建模板加载器
		StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
		stringTemplateLoader.putTemplate("template", templateContent);
		//向Configuration中配置模板加载器
		configuration.setTemplateLoader(stringTemplateLoader);
		//获取模板
		try {
			Template template = configuration.getTemplate("template");
			//调用api进行静态化
			String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//页面发布
	public ResponseResult postPage(String pageId) {
		//执行页面静态化
		String pageHtml = getPageHtml(pageId);
		//将页面静态化文件存储到GridFS中
		CmsPage cmsPage = saveHtml(pageId, pageHtml);
		//向RabbitMQ发送消息
		sendPostPage(pageId);
		return new ResponseResult(CommonCode.SUCCESS);
	}
	
	//保存html到GridFS中
	private CmsPage saveHtml(String pageId,String htmlContent) {
		//先得到页面信息
		CmsPage cmsPage = findById(pageId);
		if(cmsPage == null) {
			ExceptionCast.cast(CommonCode.INVALID_PARAM);
		}
		ObjectId objectId = null;
		try {
			//将htmlContent内容转换成输入流
			InputStream inputStream = IOUtils.toInputStream(htmlContent,"utf-8");
			//将html文件内容保存到GridFS中
			objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//将Html文件id更新到cmsPage中
		cmsPage.setHtmlFileId(objectId.toHexString());
		cmsPageRepository.save(cmsPage);
		return cmsPage;
	}
	
	//想RabbitMQ发送信息
	private void sendPostPage(String pageId) {
		//得到页面信息
		CmsPage cmsPage = findById(pageId);
		if(cmsPage == null) {
			ExceptionCast.cast(CommonCode.INVALID_PARAM);
		}
		//创建消息对象
		Map<String,String> map = new HashMap<>();
		map.put("pageId", pageId);
		//转换成Json
		String jsonString = JSON.toJSONString(map);
		//发送给RabbitMQ
		String siteId = cmsPage.getSiteId();
		rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,jsonString);
	}
	
	//保存页面
	public CmsPageResult save(CmsPage cmsPage) {
		//判断页面是否存在
		CmsPage cmsPageTemp = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
		if(cmsPageTemp != null) {
			//进行更新
			return update(cmsPageTemp.getPageId(), cmsPage);
		}
		return add(cmsPage);
	}
	
	//一键发布页面
	public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
		//添加页面
		CmsPageResult save = save(cmsPage);
		if(!save.isSuccess()) {
			return new CmsPostPageResult(CommonCode.FAIL, null);
		}
		CmsPage temp = save.getCmsPage();
		//要发布的页面id
		String pageId = temp.getPageId();
		//发布页面
		ResponseResult responseResult = postPage(pageId);
		if(!responseResult.isSuccess()) {
			return new CmsPostPageResult(CommonCode.FAIL, null);
		}
		//得到页面的Url
		//站点Id
		String siteId = temp.getSiteId();
		//查询站点信息
		CmsSite cmsSite = siteService.findCmsSiteById(siteId);
		//站点域名
		String siteDomain = cmsSite.getSiteDomain();
		//站点web路径
		String siteWebPath = cmsSite.getSiteWebPath();
		//页面web路径
		String pageWebPath = temp.getPageWebPath();
		//页面名称
		String pageName = temp.getPageName();
		//页面的web访问路径
		String pageUrl = siteDomain + siteWebPath + pageWebPath + pageName;
		return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);
	}
}
