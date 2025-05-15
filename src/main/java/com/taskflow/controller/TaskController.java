package com.taskflow.controller;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.Result;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskLog;
import com.taskflow.service.TaskLogService;
import com.taskflow.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskLogService taskLogService;

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

    /**
     * 清空任务列表
     */
    @DeleteMapping("/clear")
    public Result clearAllTasks() {
        boolean success = taskService.clearAllTasks();
        if (success) {
            return Result.ok("任务列表已清空");
        } else {
            return Result.fail("任务清空失败");
        }
    }


    /**
     * 取消任务
     */
    @PostMapping("/cancel/{taskId}")
    public Result cancelTask(@PathVariable Long taskId) {
        boolean success = taskService.cancelTask(taskId);
        return success ? Result.ok("任务取消成功") : Result.fail("任务取消失败");
    }

    /**
     * 重试任务
     */
    @PostMapping("/retry/{taskId}")
    public Result retryTask(@PathVariable Long taskId) {
        String result = taskService.retryTask(taskId);
        switch (result) {
            case "RETRY_SUCCESS":
                return Result.ok("任务重试成功");
            case "RETRY_FAILED":
                return Result.fail("任务重试失败");
            case "RETRY_LIMIT_REACHED":
                return Result.fail("达到最大重试次数，任务彻底失败");
            case "ALREADY_SUCCESS":
                return Result.fail("任务已成功，不能重试");
            case "TASK_NOT_FOUND":
                return Result.fail("任务不存在");
            default:
                return Result.fail("未知错误");
        }
    }

    /**
     * 获取任务日志
     */
    @GetMapping("/log/{taskId}")
    public Result getLogsByTaskId(@PathVariable Long taskId) {
        List<TaskLog> logs = taskLogService.getLogsByTaskId(taskId);
        return Result.ok(logs);
    }


}

