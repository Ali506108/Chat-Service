# Redis Configuration Guide - FAANG Level

## Redis Use Cases in Chat Application

### 1. Session Management
- WebSocket session tracking
- User online/offline status
- Active connections mapping

### 2. Caching
- User profiles
- Group metadata
- Recent messages (hot data)

### 3. Pub/Sub
- Real-time message delivery
- Presence notifications
- Typing indicators

### 4. Rate Limiting
- Message throttling
- API rate limits

---

## Production-Ready Configuration

### application.yml
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 2000ms
      connect-timeout: 1000ms
      
      lettuce:
        pool:
          enabled: true
          max-active: 32      # Max connections
          max-idle: 16        # Idle connections to keep
          min-idle: 8         # Minimum idle connections
          max-wait: 1000ms    # Max wait for connection
          time-between-eviction-runs: 30s
        
        shutdown-timeout: 100ms
        
      # Cluster configuration (production)
      cluster:
        nodes:
          - redis-node-1:6379
          - redis-node-2:6379
          - redis-node-3:6379
        max-redirects: 3
```

---

## FAANG-Level Redis Config

### 1. Connection Factory (Reactive)
```java
@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(
            RedisProperties properties) {
        
        var clientConfig = LettuceClientConfiguration.builder()
                .clientResources(clientResources())
                .commandTimeout(Duration.ofMillis(2000))
                .shutdownTimeout(Duration.ofMillis(100))
                .build();

        var config = new RedisStandaloneConfiguration(
                properties.getHost(),
                properties.getPort()
        );
        
        if (properties.getPassword() != null) {
            config.setPassword(properties.getPassword());
        }

        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return ClientResources.builder()
                .ioThreadPoolSize(Runtime.getRuntime().availableProcessors() * 2)
                .computationThreadPoolSize(Runtime.getRuntime().availableProcessors())
                .build();
    }
}
```

### 2. Reactive Redis Template
```java
@Configuration
public class RedisTemplateConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        
        var serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .hashValue(new GenericJackson2JsonRedisSerializer())
                .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveJsonRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        
        var serializer = new GenericJackson2JsonRedisSerializer();
        var context = RedisSerializationContext
                .<String, Object>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .hashValue(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
```

### 3. Cache Configuration
```java
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        var config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();

        // Different TTL for different caches
        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
            "users", config.entryTtl(Duration.ofHours(1)),
            "groups", config.entryTtl(Duration.ofMinutes(30)),
            "messages", config.entryTtl(Duration.ofMinutes(5))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }
}
```

---

## Redis Patterns for Chat

### 1. Online Users Tracking
```java
@Service
public class PresenceService {
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    // Set user online
    public Mono<Boolean> setUserOnline(String userId) {
        return redisTemplate.opsForValue()
                .set("presence:" + userId, "online", Duration.ofMinutes(5));
    }
    
    // Check if user online
    public Mono<Boolean> isUserOnline(String userId) {
        return redisTemplate.hasKey("presence:" + userId);
    }
    
    // Get all online users
    public Flux<String> getOnlineUsers() {
        return redisTemplate.scan(ScanOptions.scanOptions()
                .match("presence:*")
                .count(100)
                .build())
                .map(key -> key.replace("presence:", ""));
    }
}
```

### 2. Message Caching (Hot Data)
```java
@Service
public class MessageCacheService {
    
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    // Cache last N messages
    public Mono<Long> cacheMessage(String chatId, Message message) {
        String key = "chat:messages:" + chatId;
        return redisTemplate.opsForList()
                .leftPush(key, message)
                .flatMap(size -> redisTemplate.expire(key, Duration.ofHours(24))
                        .thenReturn(size))
                .flatMap(size -> {
                    if (size > 100) {
                        return redisTemplate.opsForList().trim(key, 0, 99);
                    }
                    return Mono.just(size);
                });
    }
    
    // Get cached messages
    public Flux<Message> getCachedMessages(String chatId, int limit) {
        return redisTemplate.opsForList()
                .range("chat:messages:" + chatId, 0, limit - 1)
                .cast(Message.class);
    }
}
```

### 3. Rate Limiting (Token Bucket)
```java
@Service
public class RateLimiterService {
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    public Mono<Boolean> allowRequest(String userId, int maxRequests, Duration window) {
        String key = "rate_limit:" + userId;
        
        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(key, window)
                                .thenReturn(true);
                    }
                    return Mono.just(count <= maxRequests);
                });
    }
}
```

### 4. Pub/Sub for Real-Time Messages
```java
@Configuration
public class RedisPubSubConfig {
    
    @Bean
    public ReactiveRedisMessageListenerContainer messageListenerContainer(
            ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisMessageListenerContainer(factory);
    }
}

@Service
public class MessagePublisher {
    
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    public Mono<Long> publishMessage(String channel, Message message) {
        return redisTemplate.convertAndSend(channel, message);
    }
}

@Service
public class MessageSubscriber {
    
    private final ReactiveRedisMessageListenerContainer container;
    
    public Flux<Message> subscribeToChannel(String channel) {
        return container.receive(ChannelTopic.of(channel))
                .map(msg -> (Message) msg.getMessage());
    }
}
```

### 5. Distributed Lock
```java
@Service
public class DistributedLockService {
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    public Mono<Boolean> acquireLock(String lockKey, String lockValue, Duration ttl) {
        return redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, ttl);
    }
    
    public Mono<Boolean> releaseLock(String lockKey, String lockValue) {
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";
        
        return redisTemplate.execute(
            RedisScript.of(script, Boolean.class),
            List.of(lockKey),
            lockValue
        ).next();
    }
}
```

### 6. Session Store
```java
@Service
public class SessionService {
    
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    public Mono<Boolean> saveSession(String sessionId, UserSession session) {
        return redisTemplate.opsForHash()
                .putAll("session:" + sessionId, Map.of(
                    "userId", session.getUserId(),
                    "connectionId", session.getConnectionId(),
                    "connectedAt", session.getConnectedAt()
                ))
                .flatMap(success -> redisTemplate
                    .expire("session:" + sessionId, Duration.ofHours(24))
                    .thenReturn(success));
    }
    
    public Mono<UserSession> getSession(String sessionId) {
        return redisTemplate.opsForHash()
                .entries("session:" + sessionId)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(this::mapToUserSession);
    }
}
```

---

## Performance Optimization

### 1. Pipeline Commands
```java
public Mono<List<Object>> batchOperations(List<String> keys) {
    return redisTemplate.execute(connection -> {
        return connection.closePipeline()
                .flatMapMany(pipeline -> {
                    keys.forEach(key -> pipeline.stringCommands().get(key));
                    return pipeline.closePipeline();
                })
                .collectList();
    });
}
```

### 2. Connection Pooling
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 32    # CPU cores * 4
          max-idle: 16      # max-active / 2
          min-idle: 8       # max-active / 4
```

### 3. Key Naming Convention
```
Format: {service}:{entity}:{id}:{field}

Examples:
- chat:user:123:profile
- chat:message:456:content
- chat:presence:789:status
```

### 4. TTL Strategy
```java
- Hot data (recent messages): 1 hour
- User sessions: 24 hours
- Presence: 5 minutes (with heartbeat)
- Rate limits: 1 minute - 1 hour
- Cache: 10-30 minutes
```

---

## Monitoring & Metrics

### Custom Metrics
```java
@Component
public class RedisMetrics {
    
    private final MeterRegistry registry;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    @Scheduled(fixedRate = 10000)
    public void recordMetrics() {
        redisTemplate.execute(connection -> 
            connection.serverCommands().info("stats")
        ).subscribe(info -> {
            // Parse and record metrics
            registry.gauge("redis.connections", parseConnections(info));
            registry.gauge("redis.memory.used", parseMemory(info));
        });
    }
}
```

---

## Production Checklist

- [ ] Enable connection pooling
- [ ] Set appropriate timeouts
- [ ] Configure TTL for all keys
- [ ] Use Redis Cluster for HA
- [ ] Enable persistence (AOF + RDB)
- [ ] Set maxmemory-policy (allkeys-lru)
- [ ] Monitor memory usage
- [ ] Use pipelining for batch ops
- [ ] Implement circuit breaker
- [ ] Add retry logic with backoff
- [ ] Use reactive streams
- [ ] Implement health checks

---

## Redis vs Cassandra Decision

| Use Case | Redis | Cassandra |
|----------|-------|-----------|
| Recent messages (hot) | ✅ Cache | ❌ |
| Message history | ❌ | ✅ Persistent |
| User presence | ✅ TTL | ❌ |
| Real-time pub/sub | ✅ Fast | ❌ |
| Rate limiting | ✅ Atomic | ❌ |
| Session store | ✅ Fast | ❌ |
| Analytics | ❌ | ✅ Time-series |

**Strategy**: Redis for speed, Cassandra for durability
