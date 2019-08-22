package com.xuecheng.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	@Test
	public void testRedis() {
		//定义Key
		String key = "user_token:4969418f-f25b-4503-b498-3d8ad3254281";
		//定义Map
		Map<String, String> mapValue = new HashMap<String, String>();
		mapValue.put("id", "101");
		mapValue.put("username", "xll");
		String value = JSON.toJSONString(mapValue);
		//向Redis中存储字符串
		stringRedisTemplate.boundValueOps(key).set(value, 60, TimeUnit.SECONDS);
		//读取过期时间，已过期返回-2
		Long expire = stringRedisTemplate.getExpire(key);
		//根据key获取value
		String values = stringRedisTemplate.opsForValue().get(key);
		System.out.println(values);
	}
}
