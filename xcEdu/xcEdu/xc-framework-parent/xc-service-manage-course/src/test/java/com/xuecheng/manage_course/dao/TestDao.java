package com.xuecheng.manage_course.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
	
	@Autowired
	private TeachplanMapper teachplanMapper;
	
	@Test
	public void testFindTeachPlan() {
		TeachplanNode teachplanNode = teachplanMapper.selectList("4028e581617f945f01617f9dabc40000");
		System.out.println(teachplanNode);
		System.err.println(teachplanNode.getChildren());
		System.err.println(teachplanNode.getChildren().get(0).getPname());
		System.out.println(teachplanNode.getPname());
	}
	
}

