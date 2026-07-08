package com.smartfarming.task;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * 异步任务管理器
 * 管理长时间运行的异步任务（如因果分析）
 */
@Service
public class AsyncTaskManager {

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING, RUNNING, COMPLETED, FAILED
    }

    /**
     * 任务信息
     */
    @Data
    public static class TaskInfo {
        private String taskId;
        private TaskStatus status;
        private String result;
        private String error;
        private LocalDateTime createTime;
        private LocalDateTime completeTime;
    }

    /**
     * 任务存储
     */
    private final ConcurrentHashMap<String, TaskInfo> taskStore = new ConcurrentHashMap<>();

    /**
     * 提交异步任务
     */
    public String submitTask(Runnable task) {
        String taskId = UUID.randomUUID().toString();

        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(taskId);
        taskInfo.setStatus(TaskStatus.PENDING);
        taskInfo.setCreateTime(LocalDateTime.now());
        taskStore.put(taskId, taskInfo);

        CompletableFuture.runAsync(() -> {
            taskInfo.setStatus(TaskStatus.RUNNING);
            try {
                task.run();
                taskInfo.setStatus(TaskStatus.COMPLETED);
            } catch (Exception e) {
                taskInfo.setStatus(TaskStatus.FAILED);
                taskInfo.setError(e.getMessage());
            } finally {
                taskInfo.setCompleteTime(LocalDateTime.now());
            }
        }, taskExecutor);

        return taskId;
    }

    /**
     * 提交因果分析任务
     */
    public String submitCausalAnalysis(Long barnId, Map<String, Object> params) {
        return submitTask(() -> {
            // 因果分析逻辑由调用方实现
            // 这里只是提供任务管理框架
        });
    }

    /**
     * 获取任务状态
     */
    public TaskInfo getTaskStatus(String taskId) {
        return taskStore.get(taskId);
    }

    /**
     * 获取所有任务
     */
    public Map<String, TaskInfo> getAllTasks() {
        return taskStore;
    }
}
