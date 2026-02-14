package com.chatapp.chat_service.api.api;

import com.chatapp.chat_service.api.dto.CreateGroupDto;
import com.chatapp.chat_service.api.dto.GroupDto;
import com.chatapp.chat_service.infrastructure.persistence.jpa.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService service;

    @PostMapping
    public Mono<GroupDto> createGroup(@RequestBody CreateGroupDto createGroupDto) {
        return service.createGroup(createGroupDto);
    }


}
