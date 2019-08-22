package com.xuecheng.manage_course.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.CmsPostPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.CourseBaseRepository;
import com.xuecheng.manage_course.dao.CourseMapper;
import com.xuecheng.manage_course.dao.CourseMarketRepository;
import com.xuecheng.manage_course.dao.CoursePicRepository;
import com.xuecheng.manage_course.dao.CoursePubRepository;
import com.xuecheng.manage_course.dao.TeachplanMapper;
import com.xuecheng.manage_course.dao.TeachplanMediaPubRepository;
import com.xuecheng.manage_course.dao.TeachplanMediaRepository;
import com.xuecheng.manage_course.dao.TeachplanRepository;

@Service
public class CourseService {

	@Autowired
	private CourseBaseRepository courseBaseRepository;
	@Autowired
	private TeachplanMapper teachplanMapper;
	@Autowired
	private TeachplanRepository teachplanRepository;
	@Autowired
	private CourseMapper courseMapper;
	@Autowired
	private CourseMarketRepository courseMarketRepository;
	@Autowired
	private CoursePicRepository coursePicRepository;
	@Autowired
	private CmsPageClient cmsPageClient;
	@Autowired
	private CoursePubRepository coursePubRepository;
	@Autowired
	private TeachplanMediaRepository teachplanMediaRepository;
	@Autowired
	private TeachplanMediaPubRepository teachplanMediaPubRepository;
	
	// 课程发布信息
	@Value("${coursePublish.dataUrlPre}")
	private String publish_dataUrlPre;
	@Value("${coursePublish.pagePhysicalPath}")
	private String publish_page_physicalpath;
	@Value("${coursePublish.pageWebPath}")
	private String publish_page_webpath;
	@Value("${coursePublish.siteId}")
	private String publish_siteId;
	@Value("${coursePublish.templateId}")
	private String publish_templateId;
	@Value("${coursePublish.previewUrl}")
	private String previewUrl;

	// 课程计划查询
	public TeachplanNode findTeachplanList(String courseId) {
		return teachplanMapper.selectList(courseId);
	}

	// 添加课程计划
	@Transactional
	public ResponseResult addTeachPlan(Teachplan teachplan) {
		if (teachplan == null || StringUtils.isEmpty(teachplan.getCourseid())
				|| StringUtils.isEmpty(teachplan.getPname())) {
			ExceptionCast.cast(CommonCode.INVALID_PARAM);
		}

		// 取出课程Id
		String courseid = teachplan.getCourseid();
		// 取出父节点id
		String parentid = teachplan.getParentid();
		if (StringUtils.isEmpty(parentid)) {
			// 如果父节点为空 则获取根节点
			parentid = getTeachplanRoot(courseid);
		}
		// 取出父节点信息
		Optional<Teachplan> teachplanOptional = teachplanRepository.findById(parentid);
		if (!teachplanOptional.isPresent()) {
			ExceptionCast.cast(CommonCode.INVALID_PARAM);
		}
		// 父节点
		Teachplan teachplanParent = teachplanOptional.get();
		// 父节点级别
		String parentGrade = teachplanParent.getGrade();
		// 设置父节点
		teachplan.setParentid(parentid);
		teachplan.setStatus("0");
		// 子节点的级别根据父节点来判断
		if (parentGrade.equals("1")) {
			teachplan.setGrade("2");
		} else if (parentGrade.equals("2")) {
			teachplan.setGrade("3");
		}
		// 设置课程id
		teachplan.setCourseid(teachplanParent.getCourseid());
		teachplanRepository.save(teachplan);

		return new ResponseResult(CommonCode.SUCCESS);
	}

	// 查询课程的根节点，如果查询不到要自动添加根节点
	private String getTeachplanRoot(String courseId) {
		// 校验课程id
		Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
		if (!optional.isPresent()) {
			return null;
		}
		CourseBase courseBase = optional.get();

		// 取出课程计划根节点
		List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
		if (teachplanList == null || teachplanList.size() <= 0) {
			// 新增一个根节点
			Teachplan teachplanRoot = new Teachplan();
			teachplanRoot.setCourseid(courseId);
			teachplanRoot.setPname(courseBase.getName());
			teachplanRoot.setParentid("0");
			teachplanRoot.setGrade("1");
			teachplanRoot.setStatus("0");
			teachplanRepository.save(teachplanRoot);
			return teachplanRoot.getId();
		}
		Teachplan teachplan = teachplanList.get(0);
		return teachplan.getId();
	}

	// 课程列表分页查询
	public QueryResponseResult findCourseListPage(int page, int size, CourseListRequest courseListRequest) {
		if (courseListRequest == null) {
			courseListRequest = new CourseListRequest();
		}
		if (page <= 0) {
			page = 0;
		}
		if (size <= 0) {
			size = 20;
		}

		// 设置分页参数
		PageHelper.startPage(page, size);
		// 分页查询
		Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
		// 查询列表
		List<CourseInfo> list = courseListPage.getResult();
		// 总记录数
		long total = courseListPage.getTotal();
		// 查询结果集
		QueryResult<CourseInfo> queryResult = new QueryResult<>();
		queryResult.setList(list);
		queryResult.setTotal(total);
		return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
	}

	// 添加基础课程信息
	@Transactional
	public AddCourseResult addCourseBase(CourseBase courseBase) {
		// 课程状态默认为未发布
		courseBase.setStatus("202001");
		courseBaseRepository.save(courseBase);
		return new AddCourseResult(CommonCode.SUCCESS, courseBase.getId());
	}

	// 查询基础课程信息
	@Transactional
	public CourseBase findCoursebaseById(String courseId) {
		Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	// 修改基础课程信息
	@Transactional
	public ResponseResult updateCoursebase(String courseId, CourseBase courseBase) {
		CourseBase temp = findCoursebaseById(courseId);
		if (temp == null) {
			ExceptionCast.cast(CourseCode.COURSE_NO_EXIST);
		}
		temp.setName(courseBase.getName());
		temp.setMt(courseBase.getMt());
		temp.setSt(courseBase.getSt());
		temp.setGrade(courseBase.getGrade());
		temp.setStudymodel(courseBase.getStudymodel());
		temp.setUsers(courseBase.getUsers());
		temp.setDescription(courseBase.getDescription());
		courseBaseRepository.save(temp);
		return new ResponseResult(CommonCode.SUCCESS);
	}

	// 查询课程营销信息
	public CourseMarket findCourseMarketById(String courseid) {
		Optional<CourseMarket> optional = courseMarketRepository.findById(courseid);
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	// 修改课程营销信息
	@Transactional
	public CourseMarket updateCourseMarket(String courseid, CourseMarket courseMarket) {
		CourseMarket temp = findCourseMarketById(courseid);
		if (temp == null) {
			// 添加课程营销信息
			temp = new CourseMarket();
			BeanUtils.copyProperties(courseMarket, temp);
			// 设置课程id
			temp.setId(courseid);
			courseMarketRepository.save(temp);
		} else {
			temp.setCharge(courseMarket.getCharge());
			temp.setStartTime(courseMarket.getStartTime());
			temp.setEndTime(courseMarket.getEndTime());
			temp.setPrice(courseMarket.getPrice());
			temp.setQq(courseMarket.getQq());
			temp.setValid(courseMarket.getValid());
			courseMarketRepository.save(temp);
		}
		return temp;
	}

	// 向课程管理数据库添加课程与图片的关联信息
	@Transactional
	public ResponseResult addCoursePic(String courseId, String pic) {
		// 课程图片信息
		CoursePic coursePic = null;
		// 查询课程图片
		Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
		if (picOptional.isPresent()) {
			coursePic = picOptional.get();
		}
		if (coursePic == null) {
			coursePic = new CoursePic();
		}
		coursePic.setPic(pic);
		coursePic.setCourseid(courseId);
		coursePicRepository.save(coursePic);
		return new ResponseResult(CommonCode.SUCCESS);
	}

	// 查询课程图片
	public CoursePic findCoursePic(String courseId) {
		Optional<CoursePic> optional = coursePicRepository.findById(courseId);
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	@Transactional
	public ResponseResult deleteCoursePic(String courseId) {
		long result = coursePicRepository.deleteByCourseid(courseId);
		if (result > 0) {
			return new ResponseResult(CommonCode.SUCCESS);
		}
		return new ResponseResult(CommonCode.FAIL);
	}

	// 查询课程视图，包括基本信息、图片、营销、课程计划
	public CourseView getCourseView(String id) {

		CourseView courseView = new CourseView();

		// 课程基本信息
		Optional<CourseBase> coursebaseOptional = courseBaseRepository.findById(id);
		if (coursebaseOptional.isPresent()) {
			CourseBase courseBase = coursebaseOptional.get();
			courseView.setCourseBase(courseBase);
		}

		// 课程图片
		Optional<CoursePic> coursepicOptional = coursePicRepository.findById(id);
		if (coursebaseOptional.isPresent()) {
			CoursePic coursePic = coursepicOptional.get();
			courseView.setCoursePic(coursePic);
		}

		// 课程营销
		Optional<CourseMarket> coursemarketOptional = courseMarketRepository.findById(id);
		if (coursemarketOptional.isPresent()) {
			CourseMarket courseMarket = coursemarketOptional.get();
			courseView.setCourseMarket(courseMarket);
		}

		// 课程计划信息
		TeachplanNode teachplanNode = teachplanMapper.selectList(id);
		courseView.setTeachplanNode(teachplanNode);

		return courseView;

	}

	// 课程预览
	public CoursePublishResult preview(String id) {

		CourseBase courseBase = findCoursebaseById(id);

		// 请求cms添加页面
		// 准备cmsPage信息
		CmsPage cmsPage = new CmsPage();
		// 站点
		cmsPage.setSiteId(publish_siteId);
		// 模板
		cmsPage.setTemplateId(publish_templateId);
		// 页面名称
		cmsPage.setPageName(id + ".html");
		// 页面别名
		cmsPage.setPageAliase(courseBase.getName());
		// 页面访问路径
		cmsPage.setPageWebPath(publish_page_webpath);
		// 页面存储路径
		cmsPage.setPagePhysicalPath(publish_page_physicalpath);
		// 数据url
		cmsPage.setDataUrl(publish_dataUrlPre + id);
		// 远程请求cms保存页面信息
		CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
		if (!cmsPageResult.isSuccess()) {
			return new CoursePublishResult(CommonCode.FAIL, null);
		}
		// 页面id
		String pageId = cmsPageResult.getCmsPage().getPageId();
		String pageUrl = previewUrl + pageId;
		return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
	}

	// 课程发布
	@Transactional
	public CoursePublishResult publish(String courseId) {

		// 发布课程详情页面
		CmsPostPageResult cmsPostPageResult = publishPage(courseId);
		if (!cmsPostPageResult.isSuccess()) {
			ExceptionCast.cast(CommonCode.FAIL);
		}
		// 更新课程状态
		saveCoursePubState(courseId);

		// 保存课程索引信息
		//先创建一个coursePub对象
		CoursePub coursePub = createCoursePub(courseId);
		//将coursePub对象保存到数据库
		saveCoursePub(courseId, coursePub);
		
		// 课程缓存...

		// 页面url
		String pageUrl = cmsPostPageResult.getPageUrl();
		
		//向teachplanMediaPub中保存课程媒资信息
		saveTeachplanMediaPub(courseId);
		
		return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
	}

	//向teachplanMediaPub中保存课程媒资信息
	private void saveTeachplanMediaPub(String courseId) {
		//查询课程媒资信息
		List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
		//将课程计划媒资信息存储到待索引表
		teachplanMediaPubRepository.deleteByCourseId(courseId);
		List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<TeachplanMediaPub>();
		for(TeachplanMedia teachplanMedia : teachplanMediaList) {
			TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
			BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);;
			teachplanMediaPub.setTimestamp(new Date());
			teachplanMediaPubList.add(teachplanMediaPub);
		}
		teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
	}
	
	// 发布课程正式页面
	public CmsPostPageResult publishPage(String courseId) {
		// 课程信息
		CourseBase courseBase = findCoursebaseById(courseId);

		// 发布课程预览界面
		CmsPage cmsPage = new CmsPage();
		// 站点
		cmsPage.setSiteId(publish_siteId);
		// 模板
		cmsPage.setTemplateId(publish_templateId);
		// 页面名称
		cmsPage.setPageName(courseId + ".html");
		// 页面别名
		cmsPage.setPageAliase(courseBase.getName());
		// 页面访问路径
		cmsPage.setPageWebPath(publish_page_webpath);
		// 页面存储路径
		cmsPage.setPagePhysicalPath(publish_page_physicalpath);
		// 数据url
		cmsPage.setDataUrl(publish_dataUrlPre + courseId);
		// 一键发布页面
		CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);

		return cmsPostPageResult;
	}

	// 更新课程发布状态
	private CourseBase saveCoursePubState(String courseId) {
		CourseBase courseBase = findCoursebaseById(courseId);
		// 更新发布状态
		courseBase.setStatus("202002");
		CourseBase update = courseBaseRepository.save(courseBase);
		return update;
	}

	// 创建CoursePub对象
	private CoursePub createCoursePub(String id) {
		CoursePub coursePub = new CoursePub();

		// 设置主键
		coursePub.setId(id);

		// 基础信息
		Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
		if (courseBaseOptional.isPresent()) {
			CourseBase courseBase = courseBaseOptional.get();
			BeanUtils.copyProperties(courseBase, coursePub);
		}
		// 课程图片
		Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
		if (coursePicOptional.isPresent()) {
			CoursePic coursePic = coursePicOptional.get();
			BeanUtils.copyProperties(coursePic, coursePub);
		}
		// 课程营销信息
		Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
		if (courseMarketOptional.isPresent()) {
			CourseMarket courseMarket = courseMarketOptional.get();
			BeanUtils.copyProperties(courseMarket, coursePub);
		}
		//课程计划
		TeachplanNode teachplanNode = teachplanMapper.selectList(id);
		//将课程计划转换成json串
		String teachplanString = JSON.toJSONString(teachplanNode);
		coursePub.setTeachplan(teachplanString);
		
		return coursePub;
	}
	
	//保存CoursePub
	public CoursePub saveCoursePub(String id,CoursePub coursePub) {
		if(StringUtils.isEmpty(id)) {
			ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
		}
		CoursePub coursePubNew = null;
		Optional<CoursePub> optional = coursePubRepository.findById(id);
		if(optional.isPresent()) {
			coursePubNew = optional.get();
		}else {
			coursePubNew = new CoursePub();
		}
		
		BeanUtils.copyProperties(coursePub, coursePubNew);
		//设置主键
		coursePubNew.setId(id);
		//更新时间戳为最新时间
		coursePubNew.setTimestamp(new Date());
		//发布时间
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY‐MM‐dd HH:mm:ss");
		String date = simpleDateFormat.format(new Date());
		coursePubNew.setPubTime(date);
		coursePubRepository.save(coursePubNew);
		return coursePubNew;
	}
	
	//保存课程计划与媒资文件的关联信息
	public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
		if(teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())) {
			ExceptionCast.cast(CommonCode.INVALID_PARAM);
		}
		//检验课程计划是否是3级
		//课程计划
		String teachplanId = teachplanMedia.getTeachplanId();
		//查询到课程计划
		Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
		if(!optional.isPresent()) {
			ExceptionCast.cast(CommonCode.INVALID_PARAM);
		}
		//查询到教学计划
		Teachplan teachplan = optional.get();
		//取出等级
		String grade = teachplan.getGrade();
		if(StringUtils.isEmpty(grade) || !grade.equals("3")) {
			//只允许选择第三级的课程计划关联视频
			ExceptionCast.cast(CourseCode.COURSE_MEDIS_TEACHPLAN_GRADEERROR);
		}
		
		//查询teachplan
		Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
		TeachplanMedia teachplanMedia_save = null;
		if(teachplanMediaOptional.isPresent()) {
			teachplanMedia_save = teachplanMediaOptional.get();
		}else {
			teachplanMedia_save = new TeachplanMedia();
		}
		
		//将teachplanMedia_save保存到数据库
		teachplanMedia_save.setCourseId(teachplan.getCourseid());	//课程Id
		teachplanMedia_save.setMediaId(teachplanMedia.getMediaId());	//媒资文件Id
		teachplanMedia_save.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件的原始名称
		teachplanMedia_save.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件的url
		teachplanMedia_save.setTeachplanId(teachplanId);
		
		teachplanMediaRepository.save(teachplanMedia_save);
		
		return new ResponseResult(CommonCode.SUCCESS);
	}
	
	//查询我的课程
	public QueryResponseResult<CourseInfo> findCourseList(String companyId, int page, int size, CourseListRequest courseListRequest){
		if(courseListRequest == null) {
			courseListRequest = new CourseListRequest();
		}
		courseListRequest.setCompanyId(companyId);
		//分页
		PageHelper.startPage(page,size);
		//调用dao
		Page<CourseInfo> courseListPage = courseMapper.findCourseList(courseListRequest);
		List<CourseInfo> list = courseListPage.getResult();
		long total = courseListPage.getTotal();
		QueryResult<CourseInfo> queryResult = new QueryResult<>();
		queryResult.setList(list);
		queryResult.setTotal(total);
		return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
	}

}
