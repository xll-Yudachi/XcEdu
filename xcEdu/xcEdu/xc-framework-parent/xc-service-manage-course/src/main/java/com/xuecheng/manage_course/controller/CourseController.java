package com.xuecheng.manage_course.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.xuecheng.api.course.CourseControllerApi;
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
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.framework.utils.XcOauth2Util.UserJwt;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_course.service.CourseService;

import io.reactivex.netty.protocol.http.HttpContentHolder;

@RestController
@RequestMapping("/course")
public class CourseController implements CourseControllerApi {

	@Autowired
	private CourseService courseService;
	
	//当用户拥有course_teachplan_list权限时可以访问此方法
	@PreAuthorize("hasAuthority('course_teachplan_list')")
	@Override
	@GetMapping("/teachplan/list/{courseId}")
	public TeachplanNode findTeachPlanList(@PathVariable("courseId") String courseId) {
		return courseService.findTeachplanList(courseId);
	}

	//当用户拥有course_teachplan_add权限时可以访问此方法
	@PreAuthorize("hasAuthority('course_teachplan_add')")
	@Override
	@PostMapping("/teachplan/add")
	public ResponseResult addTeachPlan(@RequestBody Teachplan teachplan) {
		return courseService.addTeachPlan(teachplan);
	}

	//粗粒度查询
	@Override
	@GetMapping("/coursebase/list_/{page}/{size}")
	public QueryResponseResult findCourseListPage(@PathVariable("page") int page, @PathVariable("size") int size,
			CourseListRequest courseListRequest) {
		return courseService.findCourseListPage(page, size, courseListRequest);
	}

	@Override
	@PostMapping("/coursebase/add")
	public AddCourseResult addCourseResult(@RequestBody CourseBase courseBase) {
		return courseService.addCourseBase(courseBase);
	}

	@Override
	@GetMapping("/coursebase/get/{courseId}")
	public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) {
		return courseService.findCoursebaseById(courseId);
	}

	@Override
	@PutMapping("/coursebase/update/{id}")
	public ResponseResult updateCourseBase(@PathVariable("id") String courseId, @RequestBody CourseBase courseBase) {
		return courseService.updateCoursebase(courseId, courseBase);
	}

	@Override
	@GetMapping("/coursemarket/get/{courseId}")
	public CourseMarket findCourseMarketById(@PathVariable("courseId") String courseId) {
		return courseService.findCourseMarketById(courseId);
	}

	@Override
	@PostMapping("/coursemarket/update/{id}")
	public ResponseResult updateCourseMarket(@PathVariable("id") String id, @RequestBody CourseMarket courseMarket) {
		CourseMarket course_market = courseService.updateCourseMarket(id, courseMarket);
		if(course_market == null) {
			return new ResponseResult(CommonCode.FAIL);
		}else {
			return new ResponseResult(CommonCode.SUCCESS);
		}
	}

	@Override
	@PostMapping("/coursepic/add")
	public ResponseResult addCoursePic(@RequestParam("courseId") String courseId, @RequestParam("pic") String pic) {
		return courseService.addCoursePic(courseId,pic);
	}

	//当用户拥有course_pic_list权限时可以访问此方法
	@PreAuthorize("hasAuthority('course_pic_list')")
	@Override
	@GetMapping("/coursepic/list/{courseId}")
	public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
		return courseService.findCoursePic(courseId);
	}

	@Override
	@DeleteMapping("/coursepic/delete")
	public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
		return courseService.deleteCoursePic(courseId);
	}

	@Override
	@GetMapping("/courseview/{id}")
	public CourseView courseView(@PathVariable("id") String id) {
		return courseService.getCourseView(id);
	}

	@Override
	@PostMapping("/preview/{id}")
	public CoursePublishResult preview(@PathVariable("id") String id) {
		System.err.println(id);
		return courseService.preview(id);
	}

	@Override
	@PostMapping("/publish/{id}")
	public CoursePublishResult publish(@PathVariable("id") String id) {
		return courseService.publish(id);
	}

	@Override
	@PostMapping("/savemedia")
	public ResponseResult savemedia(@RequestBody TeachplanMedia teachplanMedia) {
		return courseService.savemedia(teachplanMedia);
	}

	@Override
	@GetMapping("/coursebase/list/{page}/{size}")
	public QueryResponseResult<CourseInfo> findCourseList(@PathVariable("page") int page, @PathVariable("size") int size, CourseListRequest courseListRequest) {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		XcOauth2Util xcOauth2Util = new XcOauth2Util();
		UserJwt userJwt = xcOauth2Util.getUserJwtFromHeader(request);
		if(userJwt == null) {
			ExceptionCast.cast(CommonCode.UNAUTHENTICATED);
		}
		String companyId = userJwt.getCompanyId();
		return courseService.findCourseList(companyId, page, size, courseListRequest);
	}

}
