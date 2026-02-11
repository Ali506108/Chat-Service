package com.chatapp.chat_service.infrastructure.persistence.jpa;

import com.chatapp.chat_service.api.dto.CreateGroupDto;
import com.chatapp.chat_service.api.dto.GroupDto;
import com.chatapp.chat_service.domain.model.Group;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupService {

    Mono<GroupDto> createGroup(CreateGroupDto dto);

    Mono<GroupDto> getGroupById(UUID groupID);

    Flux<GroupDto> getAllGroup(int page , int size);

    Mono<GroupDto> updateGroup(UUID groupID, CreateGroupDto dto);

}
