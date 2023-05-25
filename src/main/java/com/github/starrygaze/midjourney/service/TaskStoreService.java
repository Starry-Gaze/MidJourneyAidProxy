package com.github.starrygaze.midjourney.service;

import com.github.starrygaze.midjourney.support.Task;

import java.util.List;

public interface TaskStoreService {

	void saveTask(Task task);

	void deleteTask(String id);

	Task getTask(String id);

	List<Task> listTask();

}
