package com.dprince.scheduler.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
public class DynamicTaskManager {
    private final ScheduledTaskRegistrar taskScheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();


    public void addTask(@NotEmpty String taskId,
                        @NonNull Runnable task,
                        @NonNull CronTrigger trigger) {
        // Cancel the task if it already exists
        removeTask(taskId);

        // Schedule the new task
        if(taskScheduler.getScheduler()!=null) {
            ScheduledFuture<?> scheduledFuture = taskScheduler.getScheduler().schedule(task, trigger);
            if (scheduledFuture != null) tasks.put(taskId, scheduledFuture);
        }
    }

    public void removeTask(String taskId) {
        ScheduledFuture<?> scheduledFuture = tasks.remove(taskId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false); // Cancel the task
        }
    }
}