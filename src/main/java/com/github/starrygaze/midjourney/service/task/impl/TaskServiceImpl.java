package com.github.starrygaze.midjourney.service.task.impl;

import com.github.starrygaze.midjourney.ProxyProperties;
import com.github.starrygaze.midjourney.enums.TaskStatus;
import com.github.starrygaze.midjourney.result.Message;
import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.service.discord.DiscordService;
import com.github.starrygaze.midjourney.service.notify.NotifyService;
import com.github.starrygaze.midjourney.service.store.TaskStoreService;
import com.github.starrygaze.midjourney.service.task.TaskService;
import com.github.starrygaze.midjourney.support.TaskCondition;
import com.github.starrygaze.midjourney.util.MimeTypeUtils;
import eu.maxschuster.dataurl.DataUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

/**
 * 它主要负责处理一些与任务 (Task) 相关的操作，如任务的获取、提交和状态更新等。
 */
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
	@Resource
	private TaskStoreService taskStoreService;
	@Resource
	private DiscordService discordService;
	@Resource
	private NotifyService notifyService;

	private final ThreadPoolTaskExecutor taskExecutor;
	private final List<Task> runningTasks;

	public TaskServiceImpl(ProxyProperties properties) {
		ProxyProperties.TaskQueueConfig queueConfig = properties.getQueue();
		this.runningTasks = Collections.synchronizedList(new ArrayList<>(queueConfig.getCoreSize() * 2));
		this.taskExecutor = new ThreadPoolTaskExecutor();
		this.taskExecutor.setCorePoolSize(queueConfig.getCoreSize());
		this.taskExecutor.setMaxPoolSize(queueConfig.getCoreSize());
		this.taskExecutor.setQueueCapacity(queueConfig.getQueueSize());
		this.taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		this.taskExecutor.setThreadNamePrefix("TaskQueue-");
		this.taskExecutor.initialize();
	}

	/**
	 * getTask(String id): 这个方法接收一个任务ID作为参数，然后在当前正在执行的任务列表中查找并返回对应的任务对象。如果没有找到，就返回 null。
	 * @param id
	 * @return
	 */
	@Override
	public Task getTask(String id) {
		return this.runningTasks.stream().filter(t -> id.equals(t.getId())).findFirst().orElse(null);
	}

	/**
	 * findTask(TaskCondition condition): 这个方法接收一个任务条件（TaskCondition）对象作为参数，然后在当前正在执行的任务列表中查找并返回满足这个条件的所有任务对象的 Stream 流。
	 * @param condition
	 * @return
	 */
	@Override
	public Stream<Task> findTask(TaskCondition condition) {
		return this.runningTasks.stream().filter(condition);
	}

	/**
	 * 这些方法都是提交任务的方法，它们接收一些参数（包括一个任务对象和一些其他参数），然后提交一个任务到任务执行器（ThreadPoolTaskExecutor）进行处理。
	 * 在提交任务的同时，也将这个任务保存到任务存储服务中，并将任务状态更新为提交状态，并通过通知服务发送通知。
	 * @param task
	 * @return
	 */
	@Override
	public Message<String> submitImagine(Task task) {
		return submitTask(task, () -> {
			Message<Void> result = this.discordService.imagine(task.getFinalPrompt());
			checkAndWait(task, result);
		});
	}

	/**
	 * 这些方法都是提交任务的方法，它们接收一些参数（包括一个任务对象和一些其他参数），然后提交一个任务到任务执行器（ThreadPoolTaskExecutor）进行处理。
	 * 在提交任务的同时，也将这个任务保存到任务存储服务中，并将任务状态更新为提交状态，并通过通知服务发送通知。
	 * @param task
	 * @param targetMessageId
	 * @param targetMessageHash
	 * @param index
	 * @return
	 */
	@Override
	public Message<String> submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index) {
		return submitTask(task, () -> {
			Message<Void> result = this.discordService.upscale(targetMessageId, index, targetMessageHash);
			checkAndWait(task, result);
		});
	}

	/**
	 *
	 * 这些方法都是提交任务的方法，它们接收一些参数（包括一个任务对象和一些其他参数），然后提交一个任务到任务执行器（ThreadPoolTaskExecutor）进行处理。
	 * 在提交任务的同时，也将这个任务保存到任务存储服务中，并将任务状态更新为提交状态，并通过通知服务发送通知。
	 * @param task
	 * @param targetMessageId
	 * @param targetMessageHash
	 * @param index
	 * @return
	 */
	@Override
	public Message<String> submitVariation(Task task, String targetMessageId, String targetMessageHash, int index) {
		return submitTask(task, () -> {
			Message<Void> result = this.discordService.variation(targetMessageId, index, targetMessageHash);
			checkAndWait(task, result);
		});
	}

	/**
	 *
	 * 这些方法都是提交任务的方法，它们接收一些参数（包括一个任务对象和一些其他参数），然后提交一个任务到任务执行器（ThreadPoolTaskExecutor）进行处理。
	 * 在提交任务的同时，也将这个任务保存到任务存储服务中，并将任务状态更新为提交状态，并通过通知服务发送通知。
	 * @param task
	 * @param dataUrl
	 * @return
	 */
	@Override
	public Message<String> submitDescribe(Task task, DataUrl dataUrl) {
		return submitTask(task, () -> {
			String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
			Message<String> uploadResult = this.discordService.upload(taskFileName, dataUrl);
			if (uploadResult.getCode() != Message.SUCCESS_CODE) {
				task.setFinishTime(System.currentTimeMillis());
				task.setFailReason(uploadResult.getDescription());
				changeStatusAndNotify(task, TaskStatus.FAILURE);
				return;
			}
			String finalFileName = uploadResult.getResult();
			Message<Void> result = this.discordService.describe(finalFileName);
			checkAndWait(task, result);
		});
	}

	/**
	 * submitTask(Task task, Runnable runnable)，用于提交任务到任务执行器（ThreadPoolTaskExecutor）进行处理，并处理一些相关的逻辑，如保存任务到任务存储服务、捕获和处理异常等。
	 * @param task
	 * @param runnable
	 * @return
	 */
	private Message<String> submitTask(Task task, Runnable runnable) {
		this.taskStoreService.saveTask(task);
		int size;
		try {
			size = this.taskExecutor.getThreadPoolExecutor().getQueue().size();
			this.taskExecutor.execute(() -> {
				task.setStartTime(System.currentTimeMillis());
				this.runningTasks.add(task);
				try {
					this.taskStoreService.saveTask(task);
					runnable.run();
				} finally {
					this.runningTasks.remove(task);
				}
			});
		} catch (RejectedExecutionException e) {
			this.taskStoreService.deleteTask(task.getId());
			return Message.failure("队列已满，请稍后尝试");
		}
		if (size == 0) {
			return Message.success(task.getId());
		} else {
			return Message.success(Message.WAITING_CODE, "排队中，前面还有" + size + "个任务", task.getId());
		}
	}

	/**
	 * checkAndWait(Task task, Message<Void> result)：这个方法接收一个任务对象和一个消息对象作为参数。根据消息的状态码，它会更新任务的状态和完成时间，
	 * 并通过通知服务发送通知。如果消息的状态码不等于成功状态码，它会将任务的失败原因设置为消息的描述，更改任务状态为失败，并发出通知。
	 * 如果消息的状态码是成功的，它会更改任务状态为提交并发出通知。然后，它会在任务处于进行中状态时，使任务线程等待（休眠），直到任务的状态不再是进行中状态。
	 * 如果线程在等待时被中断，它会将中断状态传播回当前线程。
	 * @param task
	 * @param result
	 */
	private void checkAndWait(Task task, Message<Void> result) {
		if (result.getCode() != Message.SUCCESS_CODE) {
			task.setFinishTime(System.currentTimeMillis());
			task.setFailReason(result.getDescription());
			changeStatusAndNotify(task, TaskStatus.FAILURE);
			return;
		}
		changeStatusAndNotify(task, TaskStatus.SUBMITTED);
		do {
			try {
				task.sleep();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			changeStatusAndNotify(task, task.getStatus());
		} while (task.getStatus() == TaskStatus.IN_PROGRESS);
		log.debug("task finished, id: {}, status: {}", task.getId(), task.getStatus());
	}

	/**
	 * changeStatusAndNotify(Task task, TaskStatus status)：这个方法接收一个任务对象和一个任务状态作为参数。
	 * 它会改变任务的状态，保存更改后的任务到任务存储服务，并通过通知服务发送通知。
	 * @param task
	 * @param status
	 */
	private void changeStatusAndNotify(Task task, TaskStatus status) {
		task.setStatus(status);
		this.taskStoreService.saveTask(task);
		this.notifyService.notifyTaskChange(task);
	}

}