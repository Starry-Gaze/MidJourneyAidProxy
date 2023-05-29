package com.github.starrygaze.midjourney.support.handle.message;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.starrygaze.midjourney.enums.TaskStatus;
import com.github.starrygaze.midjourney.service.task.TaskService;
import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.util.ConvertUtils;
import com.github.starrygaze.midjourney.entity.MessageData;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImagineMessageHandler1 implements MessageHandler1 {
	private final TaskService taskQueueService;

	@Override
	public void onMessageReceived(Message message) {
		MessageData messageData = ConvertUtils.matchImagineContent(message.getContentRaw());
		if (messageData == null) {
			return;
		}
		String taskId = ConvertUtils.findTaskIdByFinalPrompt(messageData.getPrompt());
		if (CharSequenceUtil.isBlank(taskId)) {
			return;
		}
		Task task = this.taskQueueService.getTask(taskId);
		if (task == null) {
			return;
		}
		task.setMessageId(message.getId());
		if ("Waiting to start".equals(messageData.getStatus())) {
			task.setStatus(TaskStatus.IN_PROGRESS);
		} else {
			finishTask(task, message);
		}
		task.awake();
	}

	@Override
	public void onMessageUpdate(Message message) {
	}

}
