# Spring Boot 4.0 Redis Serialization Fix

## Problem

In Spring Boot 4.0, `GenericJacksonJsonRedisSerializer` constructor changed:

```java
// Spring Boot 3.x (OLD - no args constructor)
new GenericJacksonJsonRedisSerializer()

// Spring Boot 4.0 (NEW - requires ObjectMapper)
public GenericJacksonJsonRedisSerializer(ObjectMapper mapper) {
    this(mapper, JacksonObjectReader.create(), JacksonObjectWriter.create());
}
```

**Error**: `Expected 1 argument but found 0`

---

## Solution 1: Pass ObjectMapper (Recommended)

```java
@Configuration
public class RedisConfig {

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {
        
        var serializer = new GenericJacksonJsonRedisSerializer(redisObjectMapper);
        
        var context = RedisSerializationContext
                .<String, Object>newSerializationContext(new StringRedisSerializer())
                .key(new StringRedisSerializer())
                .value(serializer)
                .hashKey(new StringRedisSerializer())
                .hashValue(serializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
```

---

## Solution 2: Use Builder Pattern (Spring Boot 4.0+)

```java
@Bean
public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
        ReactiveRedisConnectionFactory connectionFactory) {
    
    var serializer = GenericJacksonJsonRedisSerializer.create(builder -> {
        builder.configure(mapper -> {
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.registerModule(new JavaTimeModule());
        });
    });
    
    var context = RedisSerializationContext
            .<String, Object>newSerializationContext(new StringRedisSerializer())
            .value(serializer)
            .hashValue(serializer)
            .build();

    return new ReactiveRedisTemplate<>(connectionFactory, context);
}
```

---

## Solution 3: Use Jackson2JsonRedisSerializer (Alternative)

```java
@Bean
public ReactiveRedisTemplate<String, Group> groupRedisTemplate(
        ReactiveRedisConnectionFactory connectionFactory) {
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    Jackson2JsonRedisSerializer<Group> serializer = 
        new Jackson2JsonRedisSerializer<>(mapper, Group.class);
    
    var context = RedisSerializationContext
            .<String, Group>newSerializationContext(new StringRedisSerializer())
            .value(serializer)
            .hashValue(serializer)
            .build();

    return new ReactiveRedisTemplate<>(connectionFactory, context);
}
```

---

## Complete Working Example

```java
@Configuration
public class GroupRedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(DataRedisProperties properties) {
        var clientConfig = LettuceClientConfiguration.builder()
                .clientResources(clientResources())
                .commandTimeout(Duration.ofSeconds(2))
                .shutdownTimeout(Duration.ZERO)
                .build();

        var config = new RedisStandaloneConfiguration(
                properties.getHost(),
                properties.getPort()
        );

        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return ClientResources.builder()
                .ioThreadPoolSize(Runtime.getRuntime().availableProcessors() * 2)
                .computationThreadPoolSize(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        // Enable default typing for polymorphic types
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        return mapper;
    }

    @Bean
    public ReactiveRedisTemplate<String, Group> groupRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {
        
        var serializer = new GenericJacksonJsonRedisSerializer(redisObjectMapper);
        
        var context = RedisSerializationContext
                .<String, Group>newSerializationContext(new StringRedisSerializer())
                .key(new StringRedisSerializer())
                .value(serializer)
                .hashKey(new StringRedisSerializer())
                .hashValue(serializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
```

---

## Key Changes in Spring Boot 4.0

### 1. Jackson 3.x Migration
Spring Boot 4.0 uses Jackson 3.x (package: `tools.jackson.*`)

```java
// OLD (Jackson 2.x)
import com.fasterxml.jackson.databind.ObjectMapper;

// NEW (Jackson 3.x) - Same import!
import com.fasterxml.jackson.databind.ObjectMapper;
// But internally uses tools.jackson.*
```

### 2. GenericJacksonJsonRedisSerializer Constructor
```java
// Spring Boot 3.x
new GenericJacksonJsonRedisSerializer()

// Spring Boot 4.0
new GenericJacksonJsonRedisSerializer(objectMapper)
```

### 3. RedisSerializationContext Builder
```java
// Must specify generic types explicitly
RedisSerializationContext.<String, Group>newSerializationContext(...)
```

---

## Common Errors & Fixes

### Error 1: Expected 1 argument but found 0
```java
// ❌ Wrong
new GenericJacksonJsonRedisSerializer()

// ✅ Correct
new GenericJacksonJsonRedisSerializer(objectMapper)
```

### Error 2: Cannot infer type arguments
```java
// ❌ Wrong
RedisSerializationContext.newSerializationContext(new StringRedisSerializer())

// ✅ Correct
RedisSerializationContext.<String, Group>newSerializationContext(new StringRedisSerializer())
```

### Error 3: Type mismatch in serializer
```java
// ❌ Wrong - mixing serializers
.value(new GenericJacksonJsonRedisSerializer(mapper))
.hashValue(new Jackson2JsonRedisSerializer<>(Group.class))

// ✅ Correct - consistent serializers
var serializer = new GenericJacksonJsonRedisSerializer(mapper);
.value(serializer)
.hashValue(serializer)
```

---

## Best Practices

1. **Create dedicated ObjectMapper bean** for Redis
2. **Register JavaTimeModule** for Instant, LocalDateTime support
3. **Disable WRITE_DATES_AS_TIMESTAMPS** for readable dates
4. **Use same serializer** for value and hashValue
5. **Enable default typing** for polymorphic types
6. **Configure connection pooling** properly

---

## Dependencies (Spring Boot 4.0)

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}
```

Note: Jackson 3.x is included automatically in Spring Boot 4.0
