package com.github.starrygaze.midjourney.support;

import com.github.starrygaze.midjourney.ProxyProperties;
import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 这个类DiscordStarter实现了ApplicationListener接口，用于监听Spring Boot应用的启动事件ApplicationStartedEvent。
 * 当应用启动后，Spring Boot会触发这个事件，并执行这个类的onApplicationEvent方法。
 */
@Component
public class DiscordStarter implements ApplicationListener<ApplicationStartedEvent> {

	/**
	 *
	 */
	@Resource
	private ProxyProperties properties;

	/**
	 *
	 */
	@Resource
	private DiscordMessageListener discordMessageListener;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		//创建 DefaultShardManagerBuilder: 该构造器用于创建Discord Bot的Shard Manager，Shard Manager负责管理Bot的所有shard。
		//在创建构造器时，需要指定Bot的Token以及Gateway Intents。Gateway Intents是Bot需要的权限，本例中需要的权限有GUILD_MESSAGES和MESSAGE_CONTENT。
		DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(this.properties.getDiscord().getBotToken(),
				GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
		//添加事件监听器: 在Shard Manager中添加事件监听器，这里添加的是DiscordMessageListener。这意味着当Discord的消息事件发生时，将会由discordMessageListener进行处理。
		builder.addEventListeners(this.discordMessageListener);
		// 配置代理: 如果在应用的配置中设置了代理，那么会配置Java的系统属性和WebSocket的代理设置，以使得Bot的网络请求通过指定的代理服务器。
		ProxyProperties.ProxyConfig proxy = this.properties.getProxy();
		if (Strings.isNotBlank(proxy.getHost())) {
			System.setProperty("http.proxyHost", proxy.getHost());
			System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
			System.setProperty("https.proxyHost", proxy.getHost());
			System.setProperty("https.proxyPort", String.valueOf(proxy.getPort()));
			WebSocketFactory webSocketFactory = new WebSocketFactory();
			ProxySettings proxySettings = webSocketFactory.getProxySettings();
			proxySettings.setHost(proxy.getHost());
			proxySettings.setPort(proxy.getPort());
			builder.setWebsocketFactory(webSocketFactory);
		}
		//构建和启动Shard Manager: 最后，调用build方法创建并启动Shard Manager。
		builder.build();
	}

}