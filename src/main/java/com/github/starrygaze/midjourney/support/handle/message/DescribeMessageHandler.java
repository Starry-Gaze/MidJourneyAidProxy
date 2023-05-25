package com.github.starrygaze.midjourney.support.handle.message;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.starrygaze.midjourney.enums.TaskStatus;
import com.github.starrygaze.midjourney.service.task.TaskService;
import com.github.starrygaze.midjourney.entity.Task;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DescribeMessageHandler implements MessageHandler {
	private final TaskService taskQueueService;

	@Override
	public void onMessageReceived(Message message) {
	}

	@Override
	public void onMessageUpdate(Message message) {
		List<MessageEmbed> embeds = message.getEmbeds();
		if (embeds.isEmpty()) {
			return;
		}
		String prompt = embeds.get(0).getDescription();
		String imageUrl = embeds.get(0).getImage().getUrl();
		int hashStartIndex = imageUrl.lastIndexOf("/");
		String taskId = CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true);
		Task task = this.taskQueueService.getTask(taskId);
		if (task == null) {
			return;
		}
		task.setMessageId(message.getId());
		task.setPrompt(prompt);
		task.setPromptEn(prompt);
		task.setImageUrl(imageUrl);
		task.setFinishTime(System.currentTimeMillis());
		task.setStatus(TaskStatus.SUCCESS);
		task.awake();
	}

}
