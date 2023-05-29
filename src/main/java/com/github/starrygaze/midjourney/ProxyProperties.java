package com.github.starrygaze.midjourney;

import com.github.starrygaze.midjourney.enums.TranslateWay;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "mj")
public class ProxyProperties {

    private final TaskStore taskStore = new TaskStore();
    /**
     * discord配置.
     */
    private final DiscordConfig discord = new DiscordConfig();
    /**
     * 代理配置.
     */
    private final ProxyConfig proxy = new ProxyConfig();
    /**
     * 任务队列配置.
     */
    private final TaskQueueConfig queue = new TaskQueueConfig();
    /**
     * 百度翻译配置.
     */
    private final BaiduTranslateConfig baiduTranslate = new BaiduTranslateConfig();
    /**
     * openai配置.
     */
    private final OpenaiConfig openai = new OpenaiConfig();
    /**
     * 中文prompt翻译方式.
     */
    private TranslateWay translateWay = TranslateWay.NULL;
    /**
     * 任务状态变更回调地址.
     */
    private String notifyHook;

    /**
     * discord 配置
     */
    @Data
    public static class DiscordConfig {
        /**
         * 你的服务器id.
         */
        private String guildId;
        /**
         * 你的频道id.
         */
        private String channelId;
        /**
         * 你的登录token.
         */
        private String userToken;
        /**
         * 是否使用user_token连接wss，默认false(使用bot_token).
         */
        //private boolean userWss = false;
        /**
         * 你的机器人token.
         */
        private String botToken;
        /**
         * Midjourney机器人的名称.
         */
        private String mjBotName = "Midjourney Bot";
        /**
         * 调用discord接口时的user-agent.
         */
        private String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";
    }

    /**
     * 百度翻译配置
     */
    @Data
    public static class BaiduTranslateConfig {
        /**
         * 百度翻译的APP_ID.
         */
        private String appid;
        /**
         * 百度翻译的密钥.
         */
        private String appSecret;
    }

    /**
     * openai配置
     */
    @Data
    public static class OpenaiConfig {
        /**
         * gpt的api-key.
         */
        private String gptApiKey;
        /**
         * 超时时间.
         */
        private Duration timeout = Duration.ofSeconds(30);
        /**
         * 使用的模型.
         */
        private String model = "gpt-3.5-turbo";
        /**
         * 返回结果的最大分词数.
         */
        private int maxTokens = 2048;
        /**
         * 相似度，取值 0-2.
         */
        private double temperature = 0;
    }

    /**
     * 任务存储
     */
    @Data
    public static class TaskStore {
        /**
         * timeout of task: default 30 days
         * 默认值为30天。这个属性表示任务的超时时间，即如果任务在这个时间周期内未完成，那么这个任务可能会被认为是超时的。这个值用Java 8的
         * java.time.Duration 类型来表示，允许方便的时间长度表示和操作。
         */
        private Duration timeout = Duration.ofDays(30);
        /**
         * default: TaskStore.IN_MEMORY
         * type: TaskStore.REDIS for Redis TaskStore
         * 默认值为IN_MEMORY，即默认情况下任务数据会在内存中存储。
         * 也可以设置为REDIS，表示使用Redis作为任务存储。这个属性的类型是一个名为 Type 的枚举，它有两个可能的值：REDIS 和 IN_MEMORY。
         */
        private Type type = Type.IN_MEMORY;

        public enum Type {
            /**
             * redis.
             */
            REDIS,
            /**
             * in_memory.
             */
            IN_MEMORY
        }
    }

    /**
     * 代理配置
     */
    @Data
    public static class ProxyConfig {
        /**
         * 代理host.
         */
        private String host;
        /**
         * 代理端口.
         */
        private Integer port;
    }

    /**
     * 任务队列配置
     */
    @Data
    public static class TaskQueueConfig {
        /**
         * 并发数.
         */
        private int coreSize = 3;
        /**
         * 等待队列长度.
         */
        private int queueSize = 10;
        /**
         * 任务超时时间(分钟).
         */
        private int timeoutMinutes = 5;
    }
}
