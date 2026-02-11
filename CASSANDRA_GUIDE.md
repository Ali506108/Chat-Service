# Cassandra/ScyllaDB Data Modeling Guide

## Core Concepts

### Primary Key Structure
```
PRIMARY KEY = PARTITION KEY + CLUSTERING KEY
```

### Partition Key
- Determines which node stores the data
- Data with same partition key = same node
- **Rule**: Query must include partition key

### Clustering Key
- Sorts data within partition
- Enables range queries
- Default order: ASC

---

## Chat Application Data Model

### 1. Direct Messages Table
```cql
CREATE TABLE direct_messages (
    chat_id UUID,              -- PARTITION KEY
    created_at TIMESTAMP,      -- CLUSTERING KEY
    message_id UUID,
    sender_id UUID,
    recipient_id UUID,
    content TEXT,
    status TEXT,
    PRIMARY KEY (chat_id, created_at)
) WITH CLUSTERING ORDER BY (created_at DESC);
```

**Why this design?**
- All messages for one chat = same partition
- Messages sorted by time (newest first)
- Query: `SELECT * FROM direct_messages WHERE chat_id = ? LIMIT 50`

### 2. Group Messages Table
```cql
CREATE TABLE group_messages (
    group_id UUID,             -- PARTITION KEY
    created_at TIMESTAMP,      -- CLUSTERING KEY
    message_id UUID,
    sender_id UUID,
    content TEXT,
    PRIMARY KEY (group_id, created_at)
) WITH CLUSTERING ORDER BY (created_at DESC);
```

### 3. User Chats (Inbox)
```cql
CREATE TABLE user_chats (
    user_id UUID,              -- PARTITION KEY
    last_message_at TIMESTAMP, -- CLUSTERING KEY
    chat_id UUID,
    other_user_id UUID,
    last_message TEXT,
    unread_count INT,
    PRIMARY KEY (user_id, last_message_at)
) WITH CLUSTERING ORDER BY (last_message_at DESC);
```

**Query**: Get user's recent chats
```cql
SELECT * FROM user_chats WHERE user_id = ? LIMIT 20;
```

---

## Key Rules

### ✅ Good Queries
```cql
-- Has partition key
SELECT * FROM direct_messages WHERE chat_id = ?;

-- Partition key + clustering key range
SELECT * FROM direct_messages 
WHERE chat_id = ? AND created_at > ?;
```

### ❌ Bad Queries
```cql
-- No partition key = FULL TABLE SCAN
SELECT * FROM direct_messages WHERE sender_id = ?;

-- Requires ALLOW FILTERING (slow)
SELECT * FROM direct_messages WHERE content = ?;
```

---

## Spring Boot Mapping

### Entity Example
```java
@Table("direct_messages")
public class DirectMessage {
    
    @PrimaryKeyColumn(name = "chat_id", type = PrimaryKeyType.PARTITIONED)
    private UUID chatId;
    
    @PrimaryKeyColumn(name = "created_at", type = PrimaryKeyType.CLUSTERED, 
                      ordering = Ordering.DESCENDING)
    private Instant createdAt;
    
    @Column("message_id")
    private UUID messageId;
    
    @Column("sender_id")
    private UUID senderId;
    
    @Column("content")
    private String content;
}
```

### Repository Query
```java
public interface DirectMessageRepository 
    extends ReactiveCassandraRepository<DirectMessage, UUID> {
    
    Flux<DirectMessage> findByChatIdOrderByCreatedAtDesc(
        UUID chatId, Pageable pageable
    );
}
```

---

## Performance Tips

### 1. Partition Size
- Keep partitions < 100MB
- For chat: ~10K messages per partition
- Solution: Time-bucket partitions if needed

### 2. Denormalization
- No JOINs in Cassandra
- Duplicate data across tables
- Example: Store last message in `user_chats` table

### 3. Time-Series Pattern
```cql
-- Partition by user + date bucket
PRIMARY KEY ((user_id, date_bucket), created_at)
```

### 4. Composite Partition Key
```cql
-- Distribute load across multiple nodes
PRIMARY KEY ((user_id, bucket_id), created_at)
WHERE bucket_id = user_id % 10
```

---

## Common Patterns

### Message History
```cql
PRIMARY KEY (chat_id, created_at)
ORDER BY created_at DESC
```

### User Activity Feed
```cql
PRIMARY KEY (user_id, activity_timestamp)
ORDER BY activity_timestamp DESC
```

### Unread Messages Counter
```cql
-- Use COUNTER type
CREATE TABLE unread_counts (
    user_id UUID,
    chat_id UUID,
    count COUNTER,
    PRIMARY KEY (user_id, chat_id)
);

UPDATE unread_counts SET count = count + 1 
WHERE user_id = ? AND chat_id = ?;
```

---

## ScyllaDB Advantages

1. **Shard-per-core** architecture
2. **Lower P99 latency** (< 1ms)
3. **Better for real-time** chat apps
4. **Same CQL** as Cassandra
5. **10x throughput** improvement
