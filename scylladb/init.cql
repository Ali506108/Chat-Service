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

CREATE TABLE IF NOT EXISTS direct_message (
                                              messageid uuid PRIMARY KEY,
                                              userfrom uuid,
                                              userto uuid,
                                              content text,
                                              created_at timestamp
);

CREATE TABLE IF NOT EXISTS "GroupMessage" (
                                              messageid uuid PRIMARY KEY,
                                              chanelid uuid,
                                              userfrom uuid,
                                              content text,
                                              created_at timestamp
);
