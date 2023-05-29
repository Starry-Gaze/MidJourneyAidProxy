package com.github.starrygaze.midjourney.service.task;

import com.github.starrygaze.midjourney.result.Message;
import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.support.TaskCondition;
import com.github.starrygaze.midjourney.vo.SubmitResultVO;
import eu.maxschuster.dataurl.DataUrl;

import java.util.stream.Stream;

public interface TaskService {

	Task getTask(String id);

	Task getRunningTask(String id);

	Stream<Task> findTask(TaskCondition condition);

	Stream<Task> findRunningTask(TaskCondition condition);

	Message<String> submitImagine(Task task);

	SubmitResultVO submitImagine2(Task task);

	Message<String> submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index);

	SubmitResultVO submitUpscale2(Task task, String targetMessageId, String targetMessageHash, int index);

	Message<String> submitVariation(Task task, String targetMessageId, String targetMessageHash, int index);

	SubmitResultVO submitVariation2(Task task, String targetMessageId, String targetMessageHash, int index);

	Message<String> submitDescribe(Task task, DataUrl dataUrl);

	SubmitResultVO submitDescribe2(Task task, DataUrl dataUrl);
}