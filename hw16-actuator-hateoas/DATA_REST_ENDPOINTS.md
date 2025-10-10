# Data REST Endpoints

## Books API

### GET - Все книги
```bash
curl http://localhost:8080/datarest/books
```

### GET - Книга по ID
```bash
curl http://localhost:8080/datarest/books/1
```

### POST - Создать книгу
```bash
curl -X POST http://localhost:8080/datarest/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Effective Java",
    "author": "http://localhost:8080/datarest/authors/1",
    "genres": [
      "http://localhost:8080/datarest/genres/1",
      "http://localhost:8080/datarest/genres/2"
    ]
  }'
```

### PUT - Обновить книгу
```bash
curl -X PUT http://localhost:8080/datarest/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Effective Java: Updated",
    "author": "http://localhost:8080/datarest/authors/1",
    "genres": [
      "http://localhost:8080/datarest/genres/1"
    ]
  }'
```

### PATCH - Частичное обновление
```bash
curl -X PATCH http://localhost:8080/datarest/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Title"
  }'
```

### DELETE - Удалить книгу
```bash
curl -X DELETE http://localhost:8080/datarest/books/1
```

## Comments API

### GET - Все комментарии
```bash
curl http://localhost:8080/datarest/comments
```

### GET - Комментарий по ID
```bash
curl http://localhost:8080/datarest/comments/1
```

### POST - Создать комментарий
```bash
curl -X POST http://localhost:8080/datarest/comments \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Excellent book! Highly recommended.",
    "book": "http://localhost:8080/datarest/books/1",
    "createdAt": "2025-10-11T12:00:00"
  }'
```

### PUT - Обновить комментарий
```bash
curl -X PUT http://localhost:8080/datarest/comments/1 \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Amazing book! Must read!",
    "book": "http://localhost:8080/datarest/books/1",
    "createdAt": "2025-10-11T12:00:00"
  }'
```

### DELETE - Удалить комментарий
```bash
curl -X DELETE http://localhost:8080/datarest/comments/1
```

## Authors API

### GET - Все авторы
```bash
curl http://localhost:8080/datarest/authors
```

### GET - Автор по ID
```bash
curl http://localhost:8080/datarest/authors/1
```

## Genres API

### GET - Все жанры
```bash
curl http://localhost:8080/datarest/genres
```

### GET - Жанр по ID
```bash
curl http://localhost:8080/datarest/genres/1
```

## Проверка метрик

### Метрики книг
```bash
curl http://localhost:8081/actuator/metrics/library.books.created
curl http://localhost:8081/actuator/metrics/library.books.updated
curl http://localhost:8081/actuator/metrics/library.books.deleted
curl http://localhost:8081/actuator/metrics/library.books.total
```

### Метрики комментариев
```bash
curl http://localhost:8081/actuator/metrics/library.comments.created
curl http://localhost:8081/actuator/metrics/library.comments.updated
curl http://localhost:8081/actuator/metrics/library.comments.deleted
curl http://localhost:8081/actuator/metrics/library.comments.total
```

### Все метрики библиотеки
```bash
curl http://localhost:8081/actuator/metrics | jq '.names[] | select(startswith("library"))'
```
