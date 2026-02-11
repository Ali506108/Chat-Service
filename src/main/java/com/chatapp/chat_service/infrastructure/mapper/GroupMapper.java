package com.chatapp.chat_service.infrastructure.mapper;

import com.chatapp.chat_service.api.dto.CreateGroupDto;
import com.chatapp.chat_service.api.dto.GroupDto;
import com.chatapp.chat_service.domain.model.Group;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
@AllArgsConstructor
public class GroupMapper {

    private final Clock clock;
    private final IdGenerator generator;

    public Group toDomain(CreateGroupDto dto) {
        return new Group(
                generator.generateId(),
                dto.title() ,
                dto.description(),
                dto.admin(),
                dto.members(),
                clock.instant(),
                clock.instant()
        );
    }

    public GroupDto toDto(Group group) {
        return new GroupDto(
                group.getGroupID(),
                group.getTitle(),
                group.getDescription(),
                group.getAdmin(),
                group.getMembers(),
                group.getCreated_at(),
                group.getUpdated_at()
        );
    }

}