package com.chatapp.chat_service.domain.service;

import com.chatapp.chat_service.api.dto.CreateGroupDto;
import com.chatapp.chat_service.api.dto.GroupDto;
import com.chatapp.chat_service.domain.exception.ServiceExceptions;
import com.chatapp.chat_service.domain.model.Group;
import com.chatapp.chat_service.domain.repository.GroupRepository;
import com.chatapp.chat_service.infrastructure.mapper.GroupMapper;
import com.chatapp.chat_service.infrastructure.persistence.jpa.GroupService;
import com.chatapp.chat_service.infrastructure.persistence.redis.GroupRedisService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository repository;
    private final GroupRedisService redisService;
    private final GroupMapper mapper;

    @Override
    public Mono<GroupDto> createGroup(CreateGroupDto dto) {
        return Mono.just(dto)
                .map(mapper::toDomain)
                .flatMap(repository::save)
                .flatMap(redisService::saveGroup)
                .map(mapper::toDto)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(TimeoutException.class , ex ->
                        Mono.error(new ServiceExceptions("Timeout while creating group"))
                );
    }

    @Override
    public Mono<GroupDto> getGroupById(UUID groupID) {
        return redisService.getGroupById(String.valueOf(groupID))
                .switchIfEmpty(Mono.defer( () ->
                        repository.findById(groupID)
                                .flatMap(redisService::saveGroup)
                                .onErrorResume(err ->
                                        Mono.error(new RuntimeException("Failed to save group to redis and find data in repo"))
                                )

                )).map(mapper::toDto)
                .timeout(Duration.ofSeconds(3))
                .onErrorMap(msg ->
                        new RuntimeException("Failed to find group by id: " + groupID)
                );
    }

    @Override
    public Flux<GroupDto> getAllGroup(int page , int size) {
        return repository.findAllBy(PageRequest.of(page , size))
                .map(mapper::toDto)
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(msg ->
                        Flux.error(new RuntimeException("Failed to fetch all groups"))
                );
    }

    @Override
    public Mono<GroupDto> updateGroup(UUID groupID, CreateGroupDto dto) {
        return repository.findById(groupID)
                .flatMap(group -> updateGroup(group, dto))
                .flatMap(redisService::saveGroup)
                .doOnSuccess(success ->
                        log.debug("Group successfully updated and saved to redis")
                )
                .map(mapper::toDto)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(msg -> {
                    log.error("Failed to update group", msg);
                    return Mono.error(new RuntimeException("Failed to update group, ", msg));
                });
    }

    private Mono<Group> updateGroup(Group group , CreateGroupDto dto){

        group.setGroupID(group.getGroupID());
        group.setAdmin(dto.admin());

        group.setTitle(dto.title());
        group.setDescription(dto.description());
        group.setMembers(dto.members());

        group.setCreated_at(Instant.now());
        group.setUpdated_at(Instant.now());

        return repository.save(group);
    }
}