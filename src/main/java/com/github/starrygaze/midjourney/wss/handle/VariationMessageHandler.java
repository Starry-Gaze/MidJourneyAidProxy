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
 * variation消息处理; todo: 进度、完成的content不包含index，同个任务不同变换对应不上.
 * 开始(create): Making variations for image #1 with prompt **[0152010266005012] cat** - <@1012983546824114217> (Waiting to start)
 * 进度(update): **[0152010266005012] cat** - Variations by <@1012983546824114217> (0%) (relaxed)
 * 完成(create): **[0152010266005012] cat** - Variations by <@1012983546824114217> (relaxed)
 */
@Slf4j
@Component
public class VariationMessageHandler extends MessageHandler {
	private static final String START_CONTENT_REGEX = "Making variations for image #(\\d) with prompt \\*\\*\\[(\\d+)\\] (.*?)\\*\\* - <@\\d+> \\((.*?)\\)";
	private static final String CONTENT_REGEX = "\\*\\*\\[(\\d+)\\] (.*?)\\*\\* - Variations by <@\\d+> \\((.*?)\\)";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		String content = message.getString("content");
		if (MessageType.CREATE.equals(messageType)) {
			UVContentParseData start = parseStart(content);
			if (start != null) {
				// 开始
				TaskCondition condition = new TaskCondition()
						.setRelatedTaskId(start.getTaskId())
						.setActionSet2(Set.of(TaskAction.VARIATION))
						.setStatusSet(Set.of(TaskStatus.SUBMITTED));
				Task task = this.taskService.findRunningTask(condition)
						.filter(t -> CharSequenceUtil.endWith(t.getDescription(), "V" + start.getIndex()))
						.min(Comparator.comparing(Task::getSubmitTime))
						.orElse(null);
				if (task == null) {
					return;
				}
				task.setStatus(TaskStatus.IN_PROGRESS);
				task.awake();
				return;
			}
			UVContentParseData end = parse(content);
			if (end == null) {
				return;
			}
			TaskCondition condition = new TaskCondition()
					.setRelatedTaskId(end.getTaskId())
					.setActionSet2(Set.of(TaskAction.VARIATION))
					.setStatusSet(Set.of(TaskStatus.IN_PROGRESS));
			Task task = this.taskService.findRunningTask(condition)
					.min(Comparator.comparing(Task::getSubmitTime))
					.orElse(null);
			if (task == null) {
				return;
			}
			finishTask(task, message);
			task.awake();
		} else if (MessageType.UPDATE == messageType) {
			UVContentParseData parseData = parse(content);
			if (parseData == null || CharSequenceUtil.equalsAny(parseData.getStatus(), "relaxed", "fast")) {
				return;
			}
			TaskCondition condition = new TaskCondition()
					.setRelatedTaskId(parseData.getTaskId())
					.setActionSet2(Set.of(TaskAction.VARIATION))
					.setStatusSet(Set.of(TaskStatus.IN_PROGRESS));
			Task task = this.taskService.findRunningTask(condition)
					.min(Comparator.comparing(Task::getSubmitTime))
					.orElse(null);
			if (task == null) {
				return;
			}
			task.setProgress(parseData.getStatus());
			updateTaskImageUrl(task, message);
			task.awake();
		}
	}

	/**
	 * bot-wss模式，取不到执行进度.
	 *
	 * @param messageType messageType
	 * @param message     message
	 */
	@Override
	public void handle(MessageType messageType, Message message) {
		String content = message.getContentRaw();
		if (MessageType.CREATE.equals(messageType)) {
			UVContentParseData parseData = parse(content);
			if (parseData == null) {
				return;
			}
			TaskCondition condition = new TaskCondition()
					.setRelatedTaskId(parseData.getTaskId())
					.setActionSet2(Set.of(TaskAction.VARIATION))
					.setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
			Task task = this.taskService.findRunningTask(condition)
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

	private UVContentParseData parse(String content) {
		Matcher matcher = Pattern.compile(CONTENT_REGEX).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		UVContentParseData parseData = new UVContentParseData();
		parseData.setTaskId(matcher.group(1));
		parseData.setPrompt(matcher.group(2));
		parseData.setStatus(matcher.group(3));
		return parseData;
	}
}
