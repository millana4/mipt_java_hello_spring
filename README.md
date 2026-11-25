# mipt_java_hello_spring


## Запуск в Docker

```
docker compose up --build -d
docker compose logs app
curl http://localhost:8081/api/messages
```

- `app` — приложение, образ собирается `Dockerfile`, `db` — PostgreSQL 16.
- Настройки подключения к базе передаются через переменные окружения
  (`DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`); значения задаются в файле `.env`.
- Приложение доступно на порту **8081** (`8081:8080` в `docker-compose.yml`).
- Остановка: `docker compose down` (данные базы сохраняются в томе `db-data`);
  `docker compose down -v` — остановка с удалением данных.


## Мониторинг

Actuator работает на порту `8082` (в контейнере `8081`), без префикса `/api`:

- `/actuator/health` — состояние приложения и БД
- `/actuator/info` — информация о приложении
- `/actuator/prometheus` — метрики Prometheus

Счётчик `messages.created` растёт при каждом сохранении сообщения. В Prometheus он называется `messages_total`: клиент Prometheus 1.x убирает суффикс `_created`.

Логи пишутся в JSON (`logback-spring.xml`). CI запускается на push и pull request в `main` (`.github/workflows/ci-build.yml`).
