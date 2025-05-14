package com.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.entity.TaskDependency;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskDependencyMapper extends BaseMapper<TaskDependency> {
}
