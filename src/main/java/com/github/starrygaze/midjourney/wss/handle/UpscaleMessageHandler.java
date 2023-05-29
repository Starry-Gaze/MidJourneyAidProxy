package com.github.starrygaze.midjourney.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.entity.two.UVContentParseData;
import com.github.starrygaze.midjourney.enums.TaskStatus;
import com.github.starrygaze.midjourney.enums.two.MessageType;
import com.github.starrygaze.midjourney.enums.two.TaskAction;
import com.github.starrygaze.midjourney.support.TaskCondition;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * upscale消息处理.
 * 开始(create): Upscaling image #1 with **[0152010266005012] cat** - <@1012983546824114217> (Waiting to start)
 * 进度: 无
 * 完成(create): **[0152010266005012] cat** - Image #1 <@1012983546824114217>
 */
@Slf4j
@Component
public class UpscaleMessageHandler extends MessageHandler {
	private static final String START_CONTENT_REGEX = "Upscaling image #(\\d) with \\*\\*\\[(\\d+)\\] (.*?)\\*\\* - <@\\d+> \\((.*?)\\)";
	private static final String END_CONTENT_REGEX = "\\*\\*\\[(\\d+)\\] (.*?)\\*\\* - Image #(\\d) <@\\d+>";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		if (MessageType.CREATE != messageType) {
			return;
		}
		String content = message.getString("content");
		UVContentParseData start = parseStart(content);
		if (start != null) {
			TaskCondition condition = new TaskCondition()
					.setRelatedTaskId(start.getTaskId())
					.setActionSet2(Set.of(TaskAction.UPSCALE))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED));
			Task task = this.taskService.findRunningTask(condition)
					.filter(t -> CharSequenceUtil.endWith(t.getDescription(), "U" + start.getIndex()))
					.min(Comparator.comparing(Task::getSubmitTime))
					.orElse(null);
			if (task == null) {
				return;
			}
			task.setStatus(TaskStatus.IN_PROGRESS);
			task.awake();
			return;
		}
		UVContentParseData end = parseEnd(content);
		if (end != null) {
			TaskCondition condition = new TaskCondition()
					.setRelatedTaskId(end.getTaskId())
					.setActionSet2(Set.of(TaskAction.UPSCALE))
					.setStatusSet(Set.of(TaskStatus.IN_PROGRESS));
			Task task = this.taskService.findRunningTask(condition)
					.filter(t -> CharSequenceUtil.endWith(t.getDescription(), "U" + end.getIndex()))
					.min(Comparator.comparing(Task::getSubmitTime))
					.orElse(null);
			if (task == null) {
				return;
			}
			finishTask(task, message);
			task.awake();
		}
	}

	@Override
	public void handle(MessageType messageType, Message message) {
		if (MessageType.CREATE != messageType) {
			return;
		}
		String content = message.getContentRaw();
		UVContentParseData parseData = parseEnd(content);
		if (parseData != null) {
			TaskCondition condition = new TaskCondition()
					.setRelatedTaskId(parseData.getTaskId())
					.setActionSet2(Set.of(TaskAction.UPSCALE))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskService.findRunningTask(condition)
					.filter(t -> CharSequenceUtil.endWith(t.getDescription(), "U" + parseData.getIndex()))
					.min(Comparator.comparing(Task::getSubmitTime))
					.orElse(null);
			if (task == null) {
				return;
			}
			finishTask(task, message);
			task.awake();
		}
	}

	private UVContentParseData parseStart(String content) {
		Matcher matcher = Pattern.compile(START_CONTENT_REGEX).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		UVContentParseData parseData = new UVContentParseData();
		parseData.setIndex(Integer.parseInt(matcher.group(1)));
		parseData.setTaskId(matcher.group(2));
		parseData.setPrompt(matcher.group(3));
		parseData.setStatus(matcher.group(4));
		return parseData;
	}

	private UVContentParseData parseEnd(String content) {
		Matcher matcher = Pattern.compile(END_CONTENT_REGEX).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		UVContentParseData parseData = new UVContentParseData();
		parseData.setTaskId(matcher.group(1));
		parseData.setPrompt(matcher.group(2));
		parseData.setIndex(Integer.parseInt(matcher.group(3)));
		parseData.setStatus("done");
		return parseData;
	}

}