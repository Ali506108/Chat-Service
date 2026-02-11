package com.chatapp.chat_service.domain.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
@Builder
@Table("Group")
public class Group {

    @PrimaryKey
    private UUID groupID;

    private String title;

    private String description;

    private UUID admin;

    private List<UUID> members;

    private Instant created_at;

    private Instant updated_at;

}
