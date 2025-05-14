package com.taskflow.controller;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.Result;
import com.taskflow.entity.Task;
import com.taskflow.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 获取所有任务
     */
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    /**
     * 获取所有任务列表
     */
    @GetMapping("/list")
    public Result listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            return taskService.listTasks(page, size);
        } catch (Exception e) {
            return Result.fail("查询任务失败：" + e.getMessage());
        }
    }


    /**
     * 创建任务
     */
    @PostMapping("/add")
    public Result createTask(@RequestBody CreateTaskRequest request) {
        try {
            taskService.createTask(request);
            return Result.ok("任务创建成功");
        } catch (Exception e) {
            return Result.fail("任务创建失败: " + e.getMessage());
        }
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteTask(@PathVariable Long id) {
        boolean success = taskService.deleteTaskById(id);
        if (success) {
            return Result.ok("任务删除成功");
        } else {
            return Result.fail("任务不存在或删除失败");
        }
    }

}

