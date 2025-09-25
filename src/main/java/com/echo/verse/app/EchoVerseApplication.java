package com.echo.verse.app;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hpk
 */
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.echo.verse.app.dao.mapper")
public class EchoVerseApplication {
    public static void main(String[] args) {
        SpringApplication.run(EchoVerseApplication.class, args);
    }
}
