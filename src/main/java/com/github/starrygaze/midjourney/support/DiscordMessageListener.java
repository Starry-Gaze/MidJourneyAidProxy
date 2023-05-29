package com.github.starrygaze.midjourney.support;
import com.github.starrygaze.midjourney.ProxyProperties;
import com.github.starrygaze.midjourney.support.handle.UVMessageHandler;
import com.github.starrygaze.midjourney.support.handle.message.DescribeMessageHandler1;
import com.github.starrygaze.midjourney.support.handle.message.ImagineMessageHandler1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMessageListener extends ListenerAdapter {

	/**
	 * 用于读取应用的配置属性
	 */
	private final ProxyProperties properties;

	/**
	 * 不同类型的消息处理器
	 */
	private final ImagineMessageHandler1 imagineMessageHandler;
	private final UVMessageHandler uvMessageHandler;
	private final DescribeMessageHandler1 describeMessageHandler;

	/**
	 * 这个方法用于判断是否忽略并记录一条消息。如果消息的频道ID与配置中的频道ID不一致，
	 * 或者消息的发送者并不是配置中指定的MJ Bot，那么该方法会返回true，表示忽略并记录这条消息。
	 * @param message
	 * @param eventName
	 * @return
	 */
	private boolean ignoreAndLogMessage(Message message, String eventName) {
		String channelId = message.getChannel().getId();
		if (!this.properties.getDiscord().getChannelId().equals(channelId)) {
			return true;
		}
		String authorName = message.getAuthor().getName();
		log.debug("{} - {}: {}", eventName, authorName, message.getContentRaw());
		return !this.properties.getDiscord().getMjBotName().equals(authorName);
	}

	/**
	 * 当收到消息更新事件时，会触发这个方法。
	 * 它会根据消息的内容和发送者来决定是调用describeMessageHandler处理器进行处理，还是调用uvMessageHandler处理器进行处理。
	 * @param event
	 */
	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		Message message = event.getMessage();
		if (ignoreAndLogMessage(message, "消息变更")) {
			return;
		}
		if (message.getInteraction() != null && "describe".equals(message.getInteraction().getName())) {
			this.describeMessageHandler.onMessageUpdate(message);
		} else {
			this.uvMessageHandler.onMessageUpdate(message);
		}
	}

	/**
	 * 当收到一条新的消息时，会触发这个方法。它根据消息的类型和内容，调用相应的处理器进行处理。
	 * 如果消息类型是SLASH_COMMAND或DEFAULT，它将调用imagineMessageHandler处理器；
	 * 如果消息类型是INLINE_REPLY且包含一个参考消息，那么将调用uvMessageHandler处理器。
	 * @param event
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		if (ignoreAndLogMessage(message, "消息接收")) {
			return;
		}
		if (MessageType.SLASH_COMMAND.equals(message.getType()) || MessageType.DEFAULT.equals(message.getType())) {
			this.imagineMessageHandler.onMessageReceived(message);
		} else if (MessageType.INLINE_REPLY.equals(message.getType()) && message.getReferencedMessage() != null) {
			this.uvMessageHandler.onMessageReceived(message);
		}
	}

}
