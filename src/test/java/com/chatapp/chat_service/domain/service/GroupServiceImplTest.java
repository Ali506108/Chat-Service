package com.chatapp.chat_service.domain.service;

import com.chatapp.chat_service.api.dto.CreateGroupDto;
import com.chatapp.chat_service.api.dto.GroupDto;
import com.chatapp.chat_service.domain.model.Group;
import com.chatapp.chat_service.domain.repository.GroupRepository;
import com.chatapp.chat_service.infrastructure.mapper.GroupMapper;
import com.chatapp.chat_service.infrastructure.persistence.redis.GroupRedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @InjectMocks
    GroupServiceImpl service;

    @Mock
    GroupRepository repository;

    @Mock
    GroupRedisService redisService;

    @Mock
    GroupMapper mapper;

    private final UUID id = UUID.randomUUID();
    private final List<UUID> list = List.of(UUID.randomUUID() , UUID.randomUUID());
    private final UUID admin = UUID.randomUUID();
    private final Instant now = Instant.now();

    // Create test's
    @Test
    void createGroup() {

        CreateGroupDto dto = new CreateGroupDto("test" , "desc" , admin , list);
        Group group = Group.builder()
                .groupID(id)
                .title("test")
                .description("desc")
                .admin(admin)
                .members(list)
                .created_at(now)
                .updated_at(now)
                .build();

        GroupDto dto1 = new GroupDto(id , "test" , "desc" , admin , list , now , now);

        when(mapper.toDomain(dto)).thenReturn(group);
        when(repository.save(group)).thenReturn(Mono.just(group));
        when(redisService.saveGroup(group)).thenReturn(Mono.just(group));
        when(mapper.toDto(group)).thenReturn(dto1);

        StepVerifier.create(service.createGroup(dto))
                .expectNext(dto1)
                .verifyComplete();
        verify(repository).save(group);
        verify(redisService).saveGroup(group);
    }

    @Test
    void createGroup_repository_failed() {
        CreateGroupDto dto = new CreateGroupDto("test", "desc", admin, list);

        Group group = new Group(id, "test", "desc", admin, list, now, now);

        when(mapper.toDomain(dto)).thenReturn(group);
        when(repository.save(group)).thenReturn(Mono.error(new RuntimeException("Db down")));

        StepVerifier.create(service.createGroup(dto))
                .expectError(RuntimeException.class)
                .verify();
    }

    // Find_By_Id
    @Test
    void getGroupById_by_redis_success() {
        Group group =  new Group(id , "test" , "desc" , admin ,list , now , now);
        GroupDto dto = new GroupDto(id, "test", "desc", admin, list, now, now);

        when(redisService.getGroupById(String.valueOf(id))).thenReturn(Mono.just(group));
        when(mapper.toDto(group)).thenReturn(dto);

        StepVerifier.create(service.getGroupById(id))
                .expectNext(dto)
                .verifyComplete();
        verify(redisService).getGroupById(id.toString());
        verify(repository, never() ).findById(id);
        verify(mapper).toDto(group);
    }

    @Test
    void getGroupById_by_repo_success() {
        Group group =  new Group(id, "test", "desc", admin, list, now, now);
        GroupDto dto = new GroupDto(id, "test", "desc", admin, list, now, now);

        when(redisService.getGroupById(String.valueOf(id))).thenReturn(Mono.empty());
        when(repository.findById(id)).thenReturn(Mono.just(group));
        when(redisService.saveGroup(group)).thenReturn(Mono.just(group));
        when(mapper.toDto(group)).thenReturn(dto);

        StepVerifier.create(service.getGroupById(id))
                .expectNext(dto)
                .verifyComplete();

        verify(redisService).getGroupById(id.toString());
        verify(repository).findById(id);
        verify(mapper).toDto(group);
    }

    @Test
    void getGroupById_failed() {
        Group group =  new Group(id, "test", "desc", admin, list, now, now);
        GroupDto dto = new GroupDto(id, "test", "desc", admin, list, now, now);

        when(redisService.getGroupById(String.valueOf(id))).thenReturn(Mono.empty());
        when(repository.findById(id)).thenReturn(Mono.error(new RuntimeException("Db down")));

        StepVerifier.create(service.getGroupById(id))
                .expectError(RuntimeException.class)
                .verify();
    }

    // Find_All
    @Test
    void find_All_success() {
        Group group = new Group(id, "test", "desc", admin, list, now, now);
        GroupDto dto = new GroupDto(id, "test", "desc", admin, list, now, now);

        when(repository.findAllBy(any(PageRequest.class))).thenReturn(Flux.just(group));
        when(mapper.toDto(group)).thenReturn(dto);

        StepVerifier.create(service.getAllGroup(1,2))
                .expectNext(dto)
                .verifyComplete();

        verify(repository).findAllBy(PageRequest.of(1,2));
        verify(mapper).toDto(group);

    }

    @Test
    void find_All_failed() {
        when(repository.findAllBy(any(PageRequest.class))).thenReturn(Flux.error(new RuntimeException("Db down")));

        StepVerifier.create(service.getAllGroup(1, 2))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository).findAllBy(PageRequest.of(1, 2));

    }


}