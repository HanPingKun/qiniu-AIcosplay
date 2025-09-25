package com.echo.verse.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author hpk
 */
@Configuration
public class RedisConfig {

    /**
     * 创建一个专门用于 ChatMemoryStore 的 RedisTemplate Bean。
     * 我们明确指定 Key 和 Value 的序列化方式都为 String。
     * Key: conversationId (String)
     * Value: 聊天记录的 JSON 字符串 (String)
     *
     * @param connectionFactory Spring Boot 自动配置的 Redis 连接工厂
     * @return 配置好的 RedisTemplate 实例
     */
    @Bean("chatMemoryRedisTemplate") // 给这个 Bean 起一个明确的名字
    public RedisTemplate<String, String> chatMemoryRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 String 序列化方式
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
