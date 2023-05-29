package com.github.starrygaze.midjourney.support.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.starrygaze.midjourney.enums.Action;
import com.github.starrygaze.midjourney.enums.TaskStatus;
import com.github.starrygaze.midjourney.service.task.TaskService;
import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.support.TaskCondition;
import com.github.starrygaze.midjourney.support.handle.message.MessageHandler1;
import com.github.starrygaze.midjourney.util.ConvertUtils;
import com.github.starrygaze.midjourney.entity.MessageData;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Set;

/**
 * UVMessageHandler 是一个消息处理类，它实现了 MessageHandler 接口，主要用于处理接收到的消息以及更新的消息。具体来说，它主要处理了与 "UV"（Upscale/Variation）相关的任务。
 *
 * 在这两个方法中，它们都使用了TaskService的findTask(TaskCondition condition)方法来查找符合条件的任务。这个方法返回一个满足TaskCondition条件的任务的Stream，然后选取最新提交的任务。
 *
 * 总的来说，这个类的主要作用是处理与 "UV"（Upscale/Variation）相关的任务的消息，包括接收到的新消息和更新的消息。
 */
@Component
@RequiredArgsConstructor
public class UVMessageHandler implements MessageHandler1 {
	private final TaskService taskQueueService;

	/**
	 * onMessageReceived(Message message): 当接收到新的消息时，该方法被调用。首先，它尝试将原始消息内容匹配为MessageData。如果匹配成功，则创建一个新的TaskCondition，并使用它来查找与这个消息相关的任务。
	 * 如果找到符合条件的任务，它就更新任务的messageId，完成任务，并唤醒该任务。
	 * @param message
	 */
	@Override
	public void onMessageReceived(Message message) {
		MessageData messageData = ConvertUtils.matchUVContent(message.getContentRaw());
		if (messageData == null) {
			return;
		}
		TaskCondition condition = new TaskCondition()
				.setKey(message.getReferencedMessage().getId() + "-" + messageData.getAction())
				.setStatusSet(Set.of(TaskStatus.IN_PROGRESS, TaskStatus.SUBMITTED));
		Task task = this.taskQueueService.findTask(condition)
				.max(Comparator.comparing(Task::getSubmitTime))
				.orElse(null);
		if (task == null) {
			return;
		}
		task.setMessageId(message.getId());
		finishTask(task, message);
		task.awake();
	}

	/**
	 * onMessageUpdate(Message message): 当消息更新时，该方法被调用。它首先尝试将更新的消息内容匹配为MessageData。如果匹配成功，它会创建一个新的TaskCondition，并使用它来查找与这个消息相关的任务。
	 * 如果找到符合条件的任务，它将更新任务的状态为IN_PROGRESS，并唤醒该任务。
	 * @param message
	 */
	@Override
	public void onMessageUpdate(Message message) {
		String content = message.getContentRaw();
		MessageData data = ConvertUtils.matchImagineContent(content);
		if (data == null) {
			data = ConvertUtils.matchUVContent(content);
		}
		if (data == null) {
			return;
		}
		String relatedTaskId = ConvertUtils.findTaskIdByFinalPrompt(data.getPrompt());
		if (CharSequenceUtil.isBlank(relatedTaskId)) {
			return;
		}
		TaskCondition condition = new TaskCondition()
				.setActionSet(Set.of(Action.UPSCALE, Action.VARIATION))
				.setRelatedTaskId(relatedTaskId)
				.setStatusSet(Set.of(TaskStatus.SUBMITTED));
		Task task = this.taskQueueService.findTask(condition)
				.max(Comparator.comparing(Task::getSubmitTime))
				.orElse(null);
		if (task == null) {
			return;
		}
		task.setStatus(TaskStatus.IN_PROGRESS);
		task.awake();
	}

}
