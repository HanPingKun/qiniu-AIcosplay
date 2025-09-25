package com.echo.verse.app.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * 使用 Redis 作为 ChatMemoryStore 的手动实现，确保稳定性和可控性。
 * 本实现与 LangChain4j 的 JSON 序列化/反序列化工具集成。
 * @author hpk
 */
@Repository // 将这个类声明为 Spring Bean，这样其他地方就可以注入它了
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final RedisTemplate<String, String> redisTemplate;
    // 聊天记录在Redis中保留7天
    private static final Duration MEMORY_TTL = Duration.ofDays(7);

    /**
     * 构造函数注入。
     * 使用 @Qualifier 注解，明确指定注入我们在 RedisConfig 中定义的 "chatMemoryRedisTemplate" Bean。
     *
     * @param redisTemplate 注入的、专门用于聊天记忆的 RedisTemplate 实例
     */
    public RedisChatMemoryStore(@Qualifier("chatMemoryRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = convertToString(memoryId);
        String json = redisTemplate.opsForValue().get(key);

        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用 LangChain4j 的工具将 JSON 字符串反序列化为消息列表
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = convertToString(memoryId);
        // 使用 LangChain4j 的工具将消息列表序列化为 JSON 字符串
        String json = ChatMessageSerializer.messagesToJson(messages);

        // 将聊天记录存入 Redis，并设置过期时间
        redisTemplate.opsForValue().set(key, json, MEMORY_TTL);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = convertToString(memoryId);
        redisTemplate.delete(key);
    }

    /**
     * 健壮的辅助方法，用于将传入的 Object 类型的 memoryId 转换为 String。
     *
     * @param memoryId LangChain4j 传入的内存ID
     * @return 转换后的 String 类型 ID
     * @throws IllegalArgumentException 如果 memoryId 不是 String 类型
     */
    private String convertToString(Object memoryId) {
        if (memoryId instanceof String) {
            return (String) memoryId;
        } else {
            throw new IllegalArgumentException("RedisChatMemoryStore expects memoryId to be of type String, but received " + (memoryId == null ? "null" : memoryId.getClass().getName()));
        }
    }
}
