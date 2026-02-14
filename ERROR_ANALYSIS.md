# Анализ ошибки Spring Boot

## Проблема
```
Error creating bean with name 'reactiveCassandraTemplate'
```

## Причина
Отсутствует конфигурация для Reactive Cassandra. Spring не может создать `reactiveCassandraTemplate` bean.

## Что не хватает

### 1. Cassandra Configuration класс
Нужно создать конфигурацию с:
- `@EnableReactiveCassandraRepositories`
- Наследование от `AbstractReactiveCassandraConfiguration`
- Настройка keyspace, contact points, port

### 2. В application.yaml
Проверить правильность свойств:
- `spring.cassandra.*` вместо `spring.data.cassandra.*` для некоторых свойств
- Убедиться что ScyllaDB/Cassandra запущена на порту 9042

### 3. Зависимости
Проверить наличие в build.gradle/pom.xml:
- `spring-boot-starter-data-cassandra-reactive`

## Решение
Создать класс конфигурации Cassandra с правильными настройками подключения.
