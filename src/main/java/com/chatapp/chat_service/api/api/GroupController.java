package com.chatapp.chat_service.api.api;

import com.chatapp.chat_service.api.dto.CreateGroupDto;
import com.chatapp.chat_service.api.dto.GroupDto;
import com.chatapp.chat_service.infrastructure.persistence.jpa.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService service;

    @PostMapping
    public Mono<GroupDto> createGroup(@RequestBody CreateGroupDto createGroupDto) {
        return service.createGroup(createGroupDto);
    }

    @GetMapping("/{id}")
    public Mono<GroupDto> getGroupById(@PathVariable UUID id) {
        return service.getGroupById(id);
    }

    @GetMapping
    public Flux<GroupDto> getAll() {
        return service.getAllGroup(0 , 10);
    }

    @PutMapping("/{groupID}")
    public Mono<GroupDto> updateGroup(@PathVariable UUID groupID , @RequestBody CreateGroupDto dto){
        return service.updateGroup(groupID, dto);
    }
}
