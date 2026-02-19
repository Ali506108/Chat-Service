CREATE KEYSPACE IF NOT EXISTS chat_keyspace
    WITH replication = {
        'class': 'SimpleStrategy',
        'replication_factor': 1
        };

USE chat_keyspace;

CREATE TABLE IF NOT EXISTS "Group" (
                                       groupid uuid PRIMARY KEY,
                                       title text,
                                       description text,
                                       admin uuid,
                                       members list<uuid>,
                                       created_at timestamp,
                                       updated_at timestamp
);

create table if not exists "direct_chat" (
    chatId uuid primary key ,
    senderUserId uuid,
    receiverUserId uuid
);


create table if not exists "messages" (
    chatId uuid primary key ,
    messageID uuid primary key ,
    senderId uuid,
    content text,
    status text,
    createdAt timestamp,
    updatedAt timestamp
);