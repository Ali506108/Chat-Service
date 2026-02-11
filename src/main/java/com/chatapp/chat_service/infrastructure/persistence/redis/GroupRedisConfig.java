package com.chatapp.chat_service.infrastructure.persistence.redis;

import com.chatapp.chat_service.domain.model.Group;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import io.lettuce.core.resource.ClientResources;

import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.time.Duration;
import java.util.Map;

@Configuration
public class GroupRedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(DataRedisProperties redisProperties) {
        var clientConfig = LettuceClientConfiguration.builder()
                .clientResources(clientResources())
                .commandTimeout(Duration.ofSeconds(2))
                .shutdownTimeout(Duration.ZERO)
                .build();

        var config = new RedisStandaloneConfiguration(
                redisProperties.getHost(),
                redisProperties.getPort()
        );


        return new LettuceConnectionFactory(config , clientConfig);
    }

    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return ClientResources.builder()
                .ioThreadPoolSize(24)
                .computationThreadPoolSize(6)
                .build();
    }


    @Bean
    public ObjectMapper redisObjectMapper() {
        return JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .findAndAddModules()
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper mapper
    ) {
        RedisTemplate<String , Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        var serializer = new GenericJacksonJsonRedisSerializer(mapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public ReactiveRedisTemplate<String , Group> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory ,
            ObjectMapper mapper
    ) {

        RedisSerializer<String> keySer = new StringRedisSerializer();
        RedisSerializer<Group> valueSer = new JacksonJsonRedisSerializer<>(mapper , Group.class);

        var keyPair = RedisSerializationContext.SerializationPair.fromSerializer(keySer);
        var valuePair = RedisSerializationContext.SerializationPair.fromSerializer(valueSer);

        RedisSerializationContext<String , Group> context =
                RedisSerializationContext.<String , Group>newSerializationContext(keyPair)
                        .key(keyPair)
                        .value(valuePair)
                        .hashKey(keyPair)
                        .hashValue(valuePair)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory , context);

    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory
    ) {
        var config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(3))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                ).serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJacksonJsonRedisSerializer(redisObjectMapper()))
                )
                .disableCachingNullValues();
        Map<String , RedisCacheConfiguration> configurationMap = Map.of(
                "Group" , config.entryTtl(Duration.ofDays(7)),
                "DirectMessage" , config.entryTtl(Duration.ofMinutes(60))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(configurationMap)
                .transactionAware()
                .build();
    }
}
