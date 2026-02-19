package com.chatapp.chat_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter @Setter
@NoArgsConstructor
@Table("direct_chat")
public class Direct {

    @PrimaryKey
    private UUID chatId;

    private UUID senderUserId;

    private UUID receiverUserId;

}
