package com.github.starrygaze.midjourney.service.store.impl;

import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.service.store.TaskStoreService;
import org.springframework.data.redis.core.*;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 这个类叫做 RedisTaskStoreServiceImpl，它是 TaskStoreService 接口的实现类。从名字中可以看出，这个服务的实现是基于Redis的，Redis是一种高效的键值对数据库，支持持久化数据。
 */
public class RedisTaskStoreServiceImpl implements TaskStoreService {
	private static final String KEY_PREFIX = "mj-task::";

	private final Duration timeout;

	/**
	 * 初始化（构造函数）：在构造函数中，接收一个超时时间和一个 RedisTemplate 对象，RedisTemplate 是 Spring Data Redis 提供的一个用于操作Redis的工具类。
	 */
	private final RedisTemplate<String, Task> redisTemplate;

	/**
	 * saveTask(Task task)：这个方法用来将一个任务保存到Redis中。任务的ID是Redis的键，任务对象本身是Redis的值，同时设置了一个超时时间。
	 * @param timeout
	 * @param redisTemplate
	 */
	public RedisTaskStoreServiceImpl(Duration timeout, RedisTemplate<String, Task> redisTemplate) {
		this.timeout = timeout;
		this.redisTemplate = redisTemplate;
	}

	/**
	 * deleteTask(String id)：这个方法用来从Redis中删除一个任务。需要提供的参数是任务的ID。
	 * @param task
	 */
	@Override
	public void saveTask(Task task) {
		this.redisTemplate.opsForValue().set(getRedisKey(task.getId()), task, this.timeout);
	}

	/**
	 * getTask(String id)：这个方法用来从Redis中获取一个任务。需要提供的参数是任务的ID。
	 * @param id
	 */
	@Override
	public void deleteTask(String id) {
		this.redisTemplate.delete(getRedisKey(id));
	}

	/**
	 * listTask()：这个方法返回Redis中所有的任务。首先通过执行一个Redis Scan命令来获取所有的键，然后取出对应的任务对象。
	 * @param id
	 * @return
	 */
	@Override
	public Task getTask(String id) {
		return this.redisTemplate.opsForValue().get(getRedisKey(id));
	}

	/**
	 * getRedisKey(String id)：这个辅助方法用于生成Redis的键，它将任务的ID添加到一个前缀后面。
	 * @return
	 */
	@Override
	public List<Task> listTask() {
		Set<String> keys = redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
			Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(KEY_PREFIX + "*").count(1000).build());
			return cursor.stream().map(String::new).collect(Collectors.toSet());
		});
		if (keys == null || keys.isEmpty()) {
			return Collections.emptyList();
		}
		ValueOperations<String, Task> operations = this.redisTemplate.opsForValue();
		return keys.stream().map(operations::get)
				.filter(Objects::nonNull)
				.toList();
	}

	private String getRedisKey(String id) {
		return KEY_PREFIX + id;
	}

}
