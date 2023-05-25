package com.github.starrygaze.midjourney.support;

import com.github.starrygaze.midjourney.ProxyProperties;
import com.github.starrygaze.midjourney.enums.TaskStatus;
import com.github.starrygaze.midjourney.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {
	/**
	 * taskService 和 properties 是该类的成员变量，它们由Spring自动注入。其中 taskService 用于操作任务，properties 包含应用的配置信息。
	 */
	private final TaskService taskService;
	private final ProxyProperties properties;

	/**
	 * 方法的主要功能是检查已提交和进行中的任务。如果任务已经开始执行，并且从开始执行到当前时间超过了配置的超时时间，那么将任务状态设置为失败，并设置失败原因为"任务超时"。
	 */
	//注解表明 checkTasks 方法是一个定时任务，fixedRate = 30000L 表示这个任务每30秒执行一次。
	@Scheduled(fixedRate = 30000L)
	public void checkTasks() {
		long currentTime = System.currentTimeMillis();
		long timeout = TimeUnit.MINUTES.toMillis(this.properties.getQueue().getTimeoutMinutes());
		TaskCondition condition = new TaskCondition()
				.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
		this.taskService.findTask(condition)
				.filter(t -> currentTime - t.getStartTime() > timeout)
				.forEach(task -> {
					task.setFinishTime(System.currentTimeMillis());
					task.setFailReason("任务超时");
					task.setStatus(TaskStatus.FAILURE);
					task.awake();
				});
	}
}
