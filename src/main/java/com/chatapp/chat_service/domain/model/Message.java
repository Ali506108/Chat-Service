package com.chatapp.chat_service.domain.model;

import com.datastax.oss.driver.internal.core.type.codec.TimeUuidCodec;
import lombok.*;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Builder
@Getter @Setter
@NoArgsConstructor
@Table("messages")
public class Message {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private UUID chatId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED ,ordinal = 0 , ordering = Ordering.DESCENDING)
    private UUID messageID;

    private UUID senderID;

    private String content;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}