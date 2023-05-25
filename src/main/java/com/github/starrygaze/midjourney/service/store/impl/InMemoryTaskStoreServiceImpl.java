package com.github.starrygaze.midjourney.service.store.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.ListUtil;
import com.github.starrygaze.midjourney.service.store.TaskStoreService;
import com.github.starrygaze.midjourney.entity.Task;

import java.time.Duration;
import java.util.List;

/**
 * 这个类叫做 InMemoryTaskStoreServiceImpl，它是 TaskStoreService 接口的实现类。
 * 从名字中可以看出，这个服务的实现是基于内存的，意味着所有的数据会存储在应用的内存中，而不是一个外部的持久化数据库。
 */
public class InMemoryTaskStoreServiceImpl implements TaskStoreService {

	/**
	 * 初始化（构造函数）：在构造函数中，它使用一个 TimedCache 对象来保存任务对象。TimedCache 是一种带有过期时间的缓存。timeout 参数定义了缓存的过期时间。
	 */
	private final TimedCache<String, Task> taskMap;

	public InMemoryTaskStoreServiceImpl(Duration timeout) {
		this.taskMap = CacheUtil.newTimedCache(timeout.toMillis());
	}

	/**
	 * saveTask(Task task)：这个方法用来将一个任务保存到 taskMap 缓存中。任务的 ID 是缓存的键，任务对象本身是缓存的值。
	 * @param task
	 */
	@Override
	public void saveTask(Task task) {
		this.taskMap.put(task.getId(), task);
	}

	/**
	 * deleteTask(String key)：这个方法用来从 taskMap 缓存中删除一个任务。需要提供的参数是任务的 ID。
	 * @param key
	 */
	@Override
	public void deleteTask(String key) {
		this.taskMap.remove(key);
	}

	/**
	 * getTask(String key)：这个方法用来从 taskMap 缓存中获取一个任务。需要提供的参数是任务的 ID。
	 * @param key
	 * @return
	 */
	@Override
	public Task getTask(String key) {
		return this.taskMap.get(key);
	}

	/**
	 * listTask()：这个方法返回 taskMap 缓存中所有的任务。它首先将缓存的迭代器转化为一个列表。
	 * @return
	 */
	@Override
	public List<Task> listTask() {
		return ListUtil.toList(this.taskMap.iterator());
	}

}
