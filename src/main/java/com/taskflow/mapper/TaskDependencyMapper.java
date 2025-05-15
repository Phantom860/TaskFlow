package com.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.entity.TaskDependency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskDependencyMapper extends BaseMapper<TaskDependency> {
    @Select("""
    SELECT COUNT(*) 
    FROM task_dependencies d
    JOIN tasks t ON d.depends_on = t.id
    WHERE d.task_id = #{taskId}
      AND t.status != 'SUCCESS'
""")
    int countUnfinishedDependencies(@Param("taskId") Long taskId);

}
