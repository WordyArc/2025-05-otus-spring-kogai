for i in {1..10}; do
  curl -s -X POST localhost:8080/logs \
    -H 'Content-Type: application/json' \
    -d '{"clientId":"demo","ip":"10.0.0.1","route":"/api/orders","status":429}'
done
