package spring.config;

import com.github.starrygaze.midjourney.ProxyProperties;
import com.github.starrygaze.midjourney.service.store.TaskStoreService;
import com.github.starrygaze.midjourney.service.translate.TranslateService;
import com.github.starrygaze.midjourney.service.store.impl.InMemoryTaskStoreServiceImpl;
import com.github.starrygaze.midjourney.service.store.impl.RedisTaskStoreServiceImpl;
import com.github.starrygaze.midjourney.service.translate.impl.BaiduTranslateServiceImpl;
import com.github.starrygaze.midjourney.service.translate.impl.GPTTranslateServiceImpl;
import com.github.starrygaze.midjourney.entity.Task;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 这是一个Spring的配置类，用于定义和管理Spring中的bean。Bean是Spring中的一个重要概念，可以被视为应用程序中的一个组件，如服务层的服务、数据访问层的存储服务、模型对象等。
 */
//这是一个类级别的注解，标记该类为Spring的配置类。
@Configuration
public class BeanConfig {

    /**
     * 这个方法用来创建TranslateService的bean。它的实现依赖于ProxyProperties的配置，该配置决定了使用BaiduTranslateServiceImpl还是GPTTranslateServiceImpl。
     * 如果TranslateWay属性为BAIDU，就使用BaiduTranslateServiceImpl，
     * 如果为GPT，就使用GPTTranslateServiceImpl。
     *
     * @param properties
     * @return
     */
    @Bean
    TranslateService translateService(ProxyProperties properties) {
        return switch (properties.getTranslateWay()) {
            case BAIDU -> new BaiduTranslateServiceImpl(properties.getBaiduTranslate());
            case GPT -> new GPTTranslateServiceImpl(properties.getOpenai());
            default -> prompt -> prompt;
        };
    }

    /**
     * 这个方法用来创建TaskStoreService的bean。它的实现依赖于ProxyProperties和RedisConnectionFactory。
     * 如果TaskStore.Type属性为IN_MEMORY，就使用InMemoryTaskStoreServiceImpl，
     * 如果为REDIS，就使用RedisTaskStoreServiceImpl。
     *
     * @param proxyProperties
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    TaskStoreService taskStoreService(ProxyProperties proxyProperties, RedisConnectionFactory redisConnectionFactory) {
        ProxyProperties.TaskStore.Type type = proxyProperties.getTaskStore().getType();
        Duration timeout = proxyProperties.getTaskStore().getTimeout();
        return switch (type) {
            case IN_MEMORY -> new InMemoryTaskStoreServiceImpl(timeout);
            case REDIS -> new RedisTaskStoreServiceImpl(timeout, taskRedisTemplate(redisConnectionFactory));
        };
    }

    /**
     * 这个方法用来创建一个配置了连接工厂和序列化器的RedisTemplate的bean，这个RedisTemplate用于操作Redis中Task对象的数据。
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    RedisTemplate<String, Task> taskRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Task> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
