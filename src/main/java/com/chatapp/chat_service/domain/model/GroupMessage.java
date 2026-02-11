package com.chatapp.chat_service.domain.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@Table("GroupMessage")
public class GroupMessage {

    @PrimaryKey
    private UUID MessageId;

    @Column("ChanelId")
    private UUID ChanelId;

    @Column("userFrom")
    private UUID userFrom;

    @Column("content")
    private String content;

    @Column("created_at")
    private Instant created_at;

}
