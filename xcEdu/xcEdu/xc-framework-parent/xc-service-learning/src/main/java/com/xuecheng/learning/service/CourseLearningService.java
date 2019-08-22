package com.xuecheng.learning.service;

import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;

@Service
public class CourseLearningService {

	@Autowired
	private CourseSearchClient courseSearchClient;
	@Autowired
	private XcLearningCourseRepository xcLearningCourseRepository;
	@Autowired
	private XcTaskHisRepository xcTaskHisRepository;
	
	//获取课程
	public GetMediaResult getMediaUrl(String courseId,String teachplanId) {
		//检验学生的学习权限....是否付费等
		
		//调用搜索服务查询
		TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);
		if(teachplanMediaPub == null || StringUtils.isEmpty(teachplanMediaPub.getMediaUrl())) {
			//获取视频播放地址出错
			ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
		}
		
		return new GetMediaResult(CommonCode.SUCCESS, teachplanMediaPub.getMediaUrl());
	}
	
	//添加选课
	@Transactional
	public ResponseResult addCourse(String userId, String courseId, String valid, Date startTime, Date endTime, XcTask xcTask) {
		if(StringUtils.isEmpty(courseId)) {
			ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
		}
		if(StringUtils.isEmpty(userId)) {
			ExceptionCast.cast(LearningCode.CHOOSECOURSE_USERISNULL);
		}
		if(StringUtils.isEmpty(xcTask.getId()) || xcTask == null) {
			ExceptionCast.cast(LearningCode.CHOOSECOURSE_TASKISNULL);
		}
		
		XcLearningCourse xcLearningCourse = xcLearningCourseRepository.findByUserIdAndCourseId(userId, courseId);
		if(xcLearningCourse != null) {
			//更新选课记录
			xcLearningCourse.setStartTime(startTime);
			xcLearningCourse.setEndTime(endTime);
			xcLearningCourse.setStatus("501001");
			xcLearningCourseRepository.save(xcLearningCourse);
		}else {
			//添加新的选课记录
			xcLearningCourse = new XcLearningCourse();
			xcLearningCourse.setUserId(userId);
			xcLearningCourse.setCourseId(courseId);
			xcLearningCourse.setStartTime(startTime);
			xcLearningCourse.setEndTime(endTime);
			xcLearningCourse.setStatus("501001");
			xcLearningCourseRepository.save(xcLearningCourse);
		}
		
		//向历史任务表插入记录
		Optional<XcTaskHis> optional = xcTaskHisRepository.findById(xcTask.getId());
		if(!optional.isPresent()) {
			//添加历史任务
			XcTaskHis xcTaskHis = new XcTaskHis();
			BeanUtils.copyProperties(xcTask, xcTaskHis);
			xcTaskHisRepository.save(xcTaskHis);
		}
		return new ResponseResult(CommonCode.SUCCESS);
	}
}
