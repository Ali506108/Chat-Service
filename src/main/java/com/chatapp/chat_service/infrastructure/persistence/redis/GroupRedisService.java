package com.chatapp.chat_service.infrastructure.persistence.redis;

import com.chatapp.chat_service.domain.model.Group;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class GroupRedisService {

    private final ReactiveRedisTemplate<String, Group> redisTemplate;

    public Mono<Group> saveGroup(Group group) {
        return redisTemplate.opsForValue().set(group.getGroupID().toString() , group)
                .thenReturn(group)
                .doOnSuccess(g -> log.debug("Cached group : {} " , g.getGroupID()))
                .onErrorResume(err -> {
                    log.warn("Redis cache write failed for group {}: {}",
                            group.getGroupID(), err.getMessage());
                    return Mono.just(group);
                });
    }

    public Mono<Group> getGroupById(String groupID) {
        return redisTemplate.opsForValue().get(groupID)
                .doOnSuccess(group -> log.debug("Data retrieved successfully"))
                .onErrorResume(err -> {
                    log.error("Error retrieving data from redis {}", err.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<Group> deleteGroup(String groupID) {
        return redisTemplate.opsForValue().delete(groupID)
                .doOnSuccess(msg -> log.debug("Data deleted successfully"))
                .onErrorResume(err -> {
                    log.error("Error deleting data from redis {}", err.getMessage());
                    return Mono.empty();
                })
                .then(getGroupById(groupID));
    }

}
