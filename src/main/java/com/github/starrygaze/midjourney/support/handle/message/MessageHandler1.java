package com.github.starrygaze.midjourney.support.handle.message;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.starrygaze.midjourney.enums.TaskStatus;
import com.github.starrygaze.midjourney.entity.Task;
import net.dv8tion.jda.api.entities.Message;

/**
 * MessageHandler 是一个接口，定义了处理消息的两个主要方法：onMessageReceived 和 onMessageUpdate。这两个方法都需要接收一个 Message 对象，分别用于处理接收到的新消息和更新的消息。
 */
public interface MessageHandler1 {

	void onMessageReceived(Message message);

	void onMessageUpdate(Message message);

	/**
	 * MessageHandler 接口还定义了一个默认方法 finishTask，该方法接收一个 Task 对象和一个 Message 对象。
	 * finishTask 方法的主要作用是根据接收到的 Message 对象完成指定的 Task 对象。
	 * 这个方法首先设置 Task 对象的结束时间为当前时间。
	 *
	 * MessageHandler 接口还定义了一个默认方法 finishTask，该方法接收一个 Task 对象和一个 Message 对象。
	 * finishTask 方法的主要作用是根据接收到的 Message 对象完成指定的 Task 对象。这个方法首先设置 Task 对象的结束时间为当前时间。
	 *
	 * 如果 Message 对象包含任何附件，finishTask 方法将 Task 对象的状态设置为 TaskStatus.SUCCESS。
	 * 然后，它从 Message 对象的第一个附件获取 URL，并将该 URL 设置为 Task 对象的图片 URL。
	 * 然后从 URL 中提取出哈希值，这个哈希值保存在 URL 最后的 "_" 和 "." 之间，提取出来的哈希值被设置为 Task 对象的消息哈希值。
	 *
	 * 如果 Message 对象不包含任何附件，finishTask 方法将 Task 对象的状态设置为 TaskStatus.FAILURE。
	 * @param task
	 * @param message
	 */
	default void finishTask(Task task, Message message) {
		task.setFinishTime(System.currentTimeMillis());
		if (!message.getAttachments().isEmpty()) {
			task.setStatus(TaskStatus.SUCCESS);
			String imageUrl = message.getAttachments().get(0).getUrl();
			task.setImageUrl(imageUrl);
			int hashStartIndex = imageUrl.lastIndexOf("_");
			task.setMessageHash(CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true));
		} else {
			task.setStatus(TaskStatus.FAILURE);
		}
	}

}
