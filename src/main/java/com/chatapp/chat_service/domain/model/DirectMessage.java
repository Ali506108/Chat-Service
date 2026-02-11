package com.chatapp.chat_service.domain.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;


@Table("direct_message")
@Getter
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode
public class DirectMessage {

    @PrimaryKey
    private UUID MessageId;

    @Column("userFrom")
    private UUID userFrom;

    @Column("userTo")
    private UUID userTo;

    @Column("content")
    private String content;

    @Column("created_at")
    private Instant created_at;
}
