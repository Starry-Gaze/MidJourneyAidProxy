package spring.config;

import com.github.starrygaze.midjourney.ProxyProperties;
import com.github.starrygaze.midjourney.service.TaskStoreService;
import com.github.starrygaze.midjourney.service.TranslateService;
import com.github.starrygaze.midjourney.service.store.InMemoryTaskStoreServiceImpl;
import com.github.starrygaze.midjourney.service.store.RedisTaskStoreServiceImpl;
import com.github.starrygaze.midjourney.service.translate.BaiduTranslateServiceImpl;
import com.github.starrygaze.midjourney.service.translate.GPTTranslateServiceImpl;
import com.github.starrygaze.midjourney.support.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class BeanConfig {

    @Bean
    TranslateService translateService(ProxyProperties properties) {
        return switch (properties.getTranslateWay()) {
            case BAIDU -> new BaiduTranslateServiceImpl(properties.getBaiduTranslate());
            case GPT -> new GPTTranslateServiceImpl(properties.getOpenai());
            default -> prompt -> prompt;
        };
    }

    @Bean
    TaskStoreService taskStoreService(ProxyProperties proxyProperties, RedisConnectionFactory redisConnectionFactory) {
        ProxyProperties.TaskStore.Type type = proxyProperties.getTaskStore().getType();
        Duration timeout = proxyProperties.getTaskStore().getTimeout();
        return switch (type) {
            case IN_MEMORY -> new InMemoryTaskStoreServiceImpl(timeout);
            case REDIS -> new RedisTaskStoreServiceImpl(timeout, taskRedisTemplate(redisConnectionFactory));
        };
    }

    @Bean
    RedisTemplate<String, Task> taskRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Task> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
