package com.github.starrygaze.midjourney.service.notify;

import com.github.starrygaze.midjourney.entity.Task;

public interface NotifyService {
	void notifyTaskChange(Task task);
}
