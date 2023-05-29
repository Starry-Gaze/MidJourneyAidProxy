package com.github.starrygaze.midjourney.service.store;

import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.support.TaskCondition;

import java.util.List;

public interface TaskStoreService {

	void saveTask(Task task);

	void save(Task task);

	void deleteTask(String id);

	void delete(String id);

	Task getTask(String id);

	Task get(String id);

	List<Task> listTask();

	List<Task> list();

	List<Task> list(TaskCondition condition);

	Task findOne(TaskCondition condition);
}
