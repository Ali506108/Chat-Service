package com.chatapp.chat_service.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateGroupDto(

        String title,

        String description,

        UUID admin,

        List<UUID>members
) {
}
