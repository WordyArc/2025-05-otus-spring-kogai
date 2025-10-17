# Resilience4j

Быстрые тесты для удобства

---

## CircuitBreaker - Проверка работы и метрик

```bash
echo "=== Тест CircuitBreaker + Retry + RateLimiter + Bulkhead ==="

for i in {1..5}; do
  curl -s "http://localhost:8080/api/v1/external/books/search?title=Java&limit=2" | jq -c '.[:1]'
  echo "  Запрос $i выполнен"
done

echo "Состояние CircuitBreaker:"
curl -s "http://localhost:8081/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:openlibrary&tag=state:closed" | jq '.measurements[0].value'

echo "Успешных вызовов:"
curl -s "http://localhost:8081/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:openlibrary&tag=kind:successful" | jq '.measurements[0].value'
```

**Ожидаемый результат:**
- Circuit state = 1.0 (CLOSED)
- Счётчик успешных вызовов увеличился

---

## TimeLimiter - Fallback при таймауте

```bash
echo "=== Тест TimeLimiter с fallback ==="

echo "Медленный запрос (5 секунд) - должен вернуть fallback..."
time curl -s "http://localhost:8080/api/v1/external/httpbin/delay?seconds=5" | jq '.'

echo "Очень медленный запрос (10 секунд) - fallback"
time curl -s "http://localhost:8080/api/v1/external/httpbin/delay?seconds=10" | jq '.'

echo ""
echo "Метрики TimeLimiter:"
curl -s "http://localhost:8081/actuator/metrics/resilience4j.timelimiter.calls?tag=name:httpbin&tag=kind:failed" | jq '.measurements[0].value'

```

**Ожидаемый результат:**
- fallback JSON вместо реального ответа `{"fallback": true, "timedOut": true, "seconds": N}`
- Запрос выполняется за ~1 секунду (timeout работает)
- Счётчик failed вызовов > 0

---

## RateLimiter - Ограничение скорости запросов

```bash
echo "=== Тест RateLimiter - быстрые последовательные запросы ==="

echo "RateLimiter для OpenLibrary: 10 rps"
echo "Доступно разрешений ДО теста:"
curl -s "http://localhost:8081/actuator/metrics/resilience4j.ratelimiter.available.permissions?tag=name:openlibrary" | jq '.measurements[0].value'

echo "Отправка 8 запросов подряд..."
for i in {1..8}; do
  curl -s "http://localhost:8080/api/v1/external/books/search?title=Spring&limit=1" > /dev/null
  echo "  Запрос $i отправлен"
done

echo "Доступно разрешений ПОСЛЕ теста:"
curl -s "http://localhost:8081/actuator/metrics/resilience4j.ratelimiter.available.permissions?tag=name:openlibrary" | jq '.measurements[0].value'

```

**Ожидаемый результат:**
- Доступные разрешения уменьшились с 10 до ~2
- Все запросы прошли (в пределах лимита)

---

## Bulkhead - Параллельные запросы

```bash
echo "=== Тест Bulkhead - изоляция параллельных вызовов ==="

echo "Bulkhead для OpenLibrary: max 5 concurrent calls"
echo "3 параллельных запроса"

curl -s "http://localhost:8080/api/v1/external/books/search?title=Java&limit=2" | jq -c '.[:1]' &
PID1=$!
curl -s "http://localhost:8080/api/v1/external/books/search?title=Python&limit=2" | jq -c '.[:1]' &
PID2=$!
curl -s "http://localhost:8080/api/v1/external/books/search?title=Docker&limit=2" | jq -c '.[:1]' &
PID3=$!

wait $PID1
wait $PID2
wait $PID3

```

**Ожидаемый результат:**
- 3 строки JSON с книгами
- Запросы выполнены параллельно без блокировок

---

## Retry - демонстрация повторов

```bash
echo "=== Тест Retry ==="

echo "Чтение метрик ДО:"
BEFORE_TOTAL=$(curl -s "http://localhost:8081/actuator/metrics/resilience4j.retry.calls?tag=name:openlibrary" | jq '.measurements[0].value // 0')
echo "retry.calls (до): $BEFORE_TOTAL"

echo "Вызов с искусственной задержкой (fail=true) — должен активировать retry:"
echo "ожидается HTTP 500 после исчерпания попыток"
curl -s "http://localhost:8080/api/v1/external/books/search-retry-test?title=RetryDemo&limit=1&fail=true"


echo "Чтение метрик ПОСЛЕ:"
AFTER_TOTAL=$(curl -s "http://localhost:8081/actuator/metrics/resilience4j.retry.calls?tag=name:openlibrary" | jq '.measurements[0].value // 0')
echo "retry.calls (после): $AFTER_TOTAL"

echo "успех без ретраев:"; curl -s "http://localhost:8081/actuator/metrics/resilience4j.retry.calls?tag=name:openlibrary&tag=kind:successful_without_retry" | jq '.measurements[0].value // 0'
echo "успех после ретраев:"; curl -s "http://localhost:8081/actuator/metrics/resilience4j.retry.calls?tag=name:openlibrary&tag=kind:successful_with_retry" | jq '.measurements[0].value // 0'
echo "ошибки после ретраев:"; curl -s "http://localhost:8081/actuator/metrics/resilience4j.retry.calls?tag=name:openlibrary&tag=kind:failed_with_retry" | jq '.measurements[0].value // 0'
```

**Ожидаемый результат:**
- Значение `retry.calls` после запроса больше, чем до

---