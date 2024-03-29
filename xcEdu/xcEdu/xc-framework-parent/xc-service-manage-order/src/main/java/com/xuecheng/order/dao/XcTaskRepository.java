package com.xuecheng.order.dao;

import java.util.Date;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.task.XcTask;

public interface XcTaskRepository extends JpaRepository<XcTask, String>{

	//查询某个时间之前的前N条任务
	Page<XcTask> findByUpdateTimeBefore(Pageable pageable, Date updateTime);
	
	//更新updateTime
	@Modifying
	@Query("update XcTask t set t.updateTime = :updateTime where t.id = :id")
	public int updateTaskTime(@Param(value = "updateTime") Date updateTime, @Param(value = "id") String id);
	
	@Modifying
	@Query("update XcTask t set t.version = :version + 1 where t.id = :id and t.version = :version")
	public int updateTaskVersion(@Param(value = "id") String id, @Param(value = "version") int version);
}
