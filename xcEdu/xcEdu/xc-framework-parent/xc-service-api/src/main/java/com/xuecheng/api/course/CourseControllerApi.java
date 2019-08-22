package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="课程管理接口",description = "课程管理接口,提供课程的增、删、查、改")
public interface CourseControllerApi {

	@ApiOperation("课程计划查询")
	public TeachplanNode findTeachPlanList(String courseId);
	
	@ApiOperation("添加课程计划")
	public ResponseResult addTeachPlan(Teachplan teachplan);
	
	@ApiOperation("查询课程信息")
	public QueryResponseResult findCourseListPage(int page,int size,CourseListRequest courseListRequest);

	@ApiOperation("添加课程基础信息")
	public AddCourseResult addCourseResult(CourseBase courseBase);
	
	@ApiOperation("获取课程基础信息")
	public CourseBase getCourseBaseById(String courseId);
	
	@ApiOperation("更新课程基础信息")
	public ResponseResult updateCourseBase(String courseId,CourseBase courseBase);
	
	@ApiOperation("获取课程营销信息")
	public CourseMarket findCourseMarketById(String courseId);
	
	@ApiOperation("更新课程营销信息")
	public ResponseResult updateCourseMarket(String id,CourseMarket courseMarket);
	
	@ApiOperation("添加课程图片")
	public ResponseResult addCoursePic(String courseId,String pic);
	
	@ApiOperation("查询课程图片")
	public CoursePic findCoursePic(String courseId);
	
	@ApiOperation("删除课程图片")
	public ResponseResult deleteCoursePic(String courseId);
	
	@ApiOperation("课程视图查询")
	public CourseView courseView(String id);
	
	@ApiOperation("课程预览")
	public CoursePublishResult preview(String id);
	
	@ApiOperation("课程发布")
	public CoursePublishResult publish(String id);
	
	@ApiOperation("保存课程计划与媒资文件关联")
	public ResponseResult savemedia(TeachplanMedia teachplanMedia);
	
	@ApiOperation("查询我的课程")
	public QueryResponseResult<CourseInfo> findCourseList(int page, int size, CourseListRequest courseListRequest);

}
