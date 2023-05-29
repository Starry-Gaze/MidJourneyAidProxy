package com.github.starrygaze.midjourney.wss.user;

import com.github.starrygaze.midjourney.ProxyProperties;
import com.github.starrygaze.midjourney.enums.two.MessageType;
import com.github.starrygaze.midjourney.wss.handle.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class UserMessageListener implements ApplicationListener<ApplicationStartedEvent> {
	@Resource
	private ProxyProperties properties;
	private final List<MessageHandler> messageHandlers = new ArrayList<>();

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		this.messageHandlers.addAll(event.getApplicationContext().getBeansOfType(MessageHandler.class).values());
	}

	public void onMessage(DataObject raw) {
		MessageType messageType = MessageType.of(raw.getString("t"));
		if (messageType == null) {
			return;
		}
		DataObject data = raw.getObject("d");
		if (ignoreAndLogMessage(data, messageType)) {
			return;
		}
		for (MessageHandler messageHandler : this.messageHandlers) {
			messageHandler.handle(messageType, data);
		}
	}

	private boolean ignoreAndLogMessage(DataObject data, MessageType messageType) {
		String channelId = data.getString("channel_id");
		if (!this.properties.getDiscord().getChannelId().equals(channelId)) {
			return true;
		}
		Optional<DataObject> author = data.optObject("author");
		if (author.isEmpty()) {
			return true;
		}
		String authorName = author.get().getString("username");
		log.debug("{} - {}: {}", messageType.name(), authorName, data.getString("content"));
		return !this.properties.getDiscord().getMjBotName().equals(authorName);
	}
}