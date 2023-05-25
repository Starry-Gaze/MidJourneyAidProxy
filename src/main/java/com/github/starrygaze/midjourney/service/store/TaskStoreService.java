package com.github.starrygaze.midjourney.service.store;

import com.github.starrygaze.midjourney.entity.Task;

import java.util.List;

public interface TaskStoreService {

	void saveTask(Task task);

	void deleteTask(String id);

	Task getTask(String id);

	List<Task> listTask();

}
