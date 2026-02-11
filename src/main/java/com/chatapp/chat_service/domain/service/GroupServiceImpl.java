package com.chatapp.chat_service.domain.service;

import com.chatapp.chat_service.api.dto.CreateGroupDto;
import com.chatapp.chat_service.api.dto.GroupDto;
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
import java.util.UUID;

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
                .doOnSuccess(msg ->
                        log.debug("Group successfully saved : {} {} " , msg.getGroupID() , msg.getTitle())
                )
                .flatMap(redisService::saveGroup)
                .doOnSuccess(msg ->
                        log.debug("Group successfully saved to redis")
                )
                .map(mapper::toDto)
                .onErrorResume(msg ->
                        Mono.error(new RuntimeException("Failed to save group"))
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
                .onErrorMap(msg ->
                        new RuntimeException("Failed to find group by id: " + groupID)
                );
    }

    @Override
    public Flux<GroupDto> getAllGroup(int page , int size) {
        return repository.findAllBy(PageRequest.of(page , size))
                .map(mapper::toDto)
                .doOnNext(msg ->
                        log.debug("Fetched group: {} ", msg)
                )
                .switchIfEmpty(Flux.empty())
                .onErrorResume(msg ->
                        Flux.error(new RuntimeException("Failed to fetch all groups"))
                );
    }

    @Override
    public Mono<GroupDto> updateGroup(UUID groupID, CreateGroupDto dto) {
        return null;
    }
}
