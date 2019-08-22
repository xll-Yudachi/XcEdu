package com.xuecheng.framework.domain.course.ext;

import java.io.Serializable;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class CourseView implements Serializable{
	private CourseBase courseBase;
	private CoursePic coursePic;
	private CourseMarket courseMarket;
	private TeachplanNode teachplanNode;
}
