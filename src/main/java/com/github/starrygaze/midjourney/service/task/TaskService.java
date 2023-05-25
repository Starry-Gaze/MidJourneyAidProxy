package com.github.starrygaze.midjourney.service.task;

import com.github.starrygaze.midjourney.result.Message;
import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.support.TaskCondition;
import eu.maxschuster.dataurl.DataUrl;

import java.util.stream.Stream;

public interface TaskService {

	Task getTask(String id);

	Stream<Task> findTask(TaskCondition condition);

	Message<String> submitImagine(Task task);

	Message<String> submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index);

	Message<String> submitVariation(Task task, String targetMessageId, String targetMessageHash, int index);

	Message<String> submitDescribe(Task task, DataUrl dataUrl);
}