package com.github.starrygaze.midjourney.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.starrygaze.midjourney.entity.Task;
import com.github.starrygaze.midjourney.enums.Action;
import com.github.starrygaze.midjourney.enums.TaskStatus;
import com.github.starrygaze.midjourney.enums.two.TaskAction;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.function.Predicate;


/**
 * TaskCondition类是一个自定义的谓词（Predicate）类，用于表示满足特定条件的任务（Task）。
 * 这个类实现了java.util.function.Predicate<Task>接口，允许它作为函数式接口使用，用于对Task对象进行条件测试。
 *
 * TaskCondition类实现了test(Task task)方法，这是Predicate接口的方法，用于对传入的Task对象进行测试。在这个方法中，它检查任务是否满足所有的条件。
 * 如果任何一个条件不满足，就返回false，表示这个任务不满足条件。如果所有的条件都满足，就返回true，表示这个任务满足条件。
 *
 * 这个类在设计模式上使用了Builder模式（通过@Accessors(chain = true)注解实现），使得创建和使用TaskCondition对象更加方便。例如，可以通过链式调用来设置条件：
 */
@Data
@Accessors(chain = true)
public class TaskCondition implements Predicate<Task> {

	/**
	 * 
	 */
	private String id;

	/**
	 * 任务的键
	 */
	private String key;

	/**
	 * 分别表示任务的提示，英文提示和最终提示
	 */
	private String prompt;

	/**
	 * 分别表示任务的提示，英文提示和最终提示
	 */
	private String promptEn;

	/**
	 * 分别表示任务的提示，英文提示和最终提示
	 */
	private String finalPrompt;

	/**
	 *
	 */
	private String description;

	/**
	 * 相关任务的ID
	 */
	private String relatedTaskId;

	/**
	 * 相关任务的ID
	 */
	private String messageId;

	/**
	 * 任务的状态集合，表示任务可能的状态
	 */
	private Set<TaskStatus> statusSet;

	/**
	 * 任务的动作集合，表示任务可能的动作
	 */
	private Set<Action> actionSet;

	/**
	 *
	 */
	private Set<TaskAction> actionSet2;

	@Override
	public boolean test(Task task) {
		if (CharSequenceUtil.isNotBlank(this.key) && !this.key.equals(task.getKey())) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.prompt) && !this.prompt.equals(task.getPrompt())) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.promptEn) && !this.promptEn.equals(task.getPromptEn())) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.finalPrompt) && !this.finalPrompt.equals(task.getFinalPrompt())) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.description) && !this.description.equals(task.getDescription())) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.relatedTaskId) && !this.relatedTaskId.equals(task.getRelatedTaskId())) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.messageId) && !this.messageId.equals(task.getMessageId())) {
			return false;
		}

		if (this.statusSet != null && !this.statusSet.isEmpty() && !this.statusSet.contains(task.getStatus())) {
			return false;
		}
		if (this.actionSet != null && !this.actionSet.isEmpty() && !this.actionSet.contains(task.getAction())) {
			return false;
		}
		return true;
	}

}
