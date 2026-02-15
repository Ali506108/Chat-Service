package com.chatapp.chat_service.infrastructure.persistence.redis;

import com.chatapp.chat_service.domain.model.Group;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GroupRedisService {

    private final ReactiveRedisTemplate<String, Group> redisTemplate;
    private static final Duration GROUP_TTL  = Duration.ofMinutes(30);
    private static final String GROUP_PREFIX = "group:";

    public Mono<Group> saveGroup(Group group) {
        String key = GROUP_PREFIX + group.getGroupID();
        return redisTemplate.opsForValue()
                .set(key , group , GROUP_TTL)
                .thenReturn(group)
                .doOnSuccess(g -> log.debug("Cached group : {} " , g.getGroupID()))
                .onErrorResume(err -> {
                    log.warn("Redis cache write failed for group {}: {}",
                            group.getGroupID(), err.getMessage());
                    return Mono.just(group);
                });
    }

    public Mono<Group> getGroupById(String groupID) {
        String key = GROUP_PREFIX + groupID;
        return redisTemplate.opsForValue().get(key)
                .doOnSuccess(group -> log.debug("Data retrieved successfully"))
                .onErrorResume(err -> {
                    log.error("Error retrieving data from redis {}", err.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<Group> deleteGroup(String groupID) {
        String key = GROUP_PREFIX + groupID;
        return redisTemplate.opsForValue().delete(key)
                .doOnSuccess(msg -> log.debug("Data deleted successfully"))
                .onErrorResume(err -> {
                    log.error("Error deleting data from redis {}", err.getMessage());
                    return Mono.empty();
                })
                .then(getGroupById(groupID));
    }

    public Flux<Group> saveAll(List<Group> groups) {
        return Flux.fromIterable(groups)
                .flatMap(this::saveGroup , 8);
    }
}
