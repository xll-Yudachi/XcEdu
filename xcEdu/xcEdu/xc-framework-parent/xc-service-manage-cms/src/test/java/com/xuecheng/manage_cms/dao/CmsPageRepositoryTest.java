package com.xuecheng.manage_cms.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.xuecheng.framework.domain.cms.CmsPage;

/**
 * @ClassName: CmsPageRepositoryTest   
 * @Description: CmsPageRepository测试类
 * @author: Yudachi
 * @date: 2019年6月8日 上午11:51:33
 * @version V1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {
	
	@Autowired
	CmsPageRepository cmsPageRepository;
	
	@Test
	public void testFindAll() {
		List<CmsPage> list = cmsPageRepository.findAll();
		System.out.println(list);
	}
}
