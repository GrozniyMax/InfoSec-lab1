# lab1 — Secure REST API (Spring Boot)

Учебный проект по разработке защищённого backend-приложения (работа 1 курса InfoSec):
REST API с JWT-аутентификацией, защитой от OWASP Top 10 и интеграцией security-сканеров в CI/CD.

## Стек

- **Java 17**, **Spring Boot 4.1.1**, Maven (`./mvnw`)
- **Spring Security** + **JWT** (JJWT 0.12.x) + **BCrypt**
- **PostgreSQL 16** (локально — в Docker через `docker-compose.yml`)
- **Flyway** — миграции БД
- **H2** (in-memory) — для тестов
- CI/CD: **GitHub Actions** (SpotBugs SAST + OWASP Dependency-Check SCA)

## API

| Метод | Путь            | Доступ     | Описание                                   |
|-------|-----------------|------------|--------------------------------------------|
| POST  | `/auth/register`| открытый   | Регистрация нового пользователя            |
| POST  | `/auth/login`   | открытый   | Вход, возвращает JWT                       |
| GET   | `/api/data`     | JWT        | Список данных (только аутентифицированным) |

### Формат ошибок

Все ошибки возвращаются в едином виде: `{ "status": <код>, "error": "<идентификатор>", "message": "<описание>" }`.

### Примеры (curl)

```bash
# 1. Регистрация
curl -i -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret123"}'
# → 201 Created { "id": 3, "username": "alice" }

# 2. Вход (тестовые пользователи из сида: admin / admin123)
curl -i -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# → 200 OK { "token": "<jwt>", "tokenType": "Bearer", "expiresIn": 3600000 }

# 3. Получение данных с токеном
curl -i http://localhost:8080/api/data \
  -H "Authorization: Bearer <jwt>"

# 4. Без токена — запрещено
curl -i http://localhost:8080/api/data
# → 401 Unauthorized
```

## Запуск

### 1. PostgreSQL в контейнере

```bash
docker compose up -d
```

### 2. Приложение

```bash
./mvnw spring-boot:run
```

Приложение поднимается на `http://localhost:8080`. Секрет JWT по умолчанию задан в `application.yaml`
(dev-only); в реальном окружении переопределите его переменной `JWT_SECRET`:

```bash
JWT_SECRET="<длинный секрет, минимум 32 символа>" ./mvnw spring-boot:run
```

## Защита (OWASP Top 10)

- **SQLi** — только параметризованные запросы (Spring Data JDBC / derived queries). Никакой конкатенации SQL.
- **XSS** — экранирование HTML-символов (`< > & " '`) в JSON-ответах (см. `config/JacksonConfig.java`), а также валидация входных данных при регистрации.
- **Broken Authentication** — JWT (HS256) выдаётся при входе, middleware-фильтр (`JwtAuthenticationFilter`) проверяет токен на всех защищённых эндпоинтах; пароли хранятся только как BCrypt-хеши (`BCryptPasswordEncoder`).
- **Дополнительно** — stateless-сессии (CSRF отключён), `permitAll` только на `/auth/**`.

## Тесты

```bash
./mvnw verify
```

Интеграционные тесты (`ApiIntegrationTests`) гоняются на H2 (in-memory, Flyway применяет миграции):
register/login/data, проверка 401 без токена, отсутствие утечек `password_hash`.

## CI/CD (GitHub Actions)

- **`.github/workflows/commit.yml`** — на каждый `push`: build + тесты (H2) + **SpotBugs** (SAST).
- **`.github/workflows/ci.yml`** — на `pull_request`: build + тесты, **SpotBugs** (SAST), **OWASP Dependency-Check** (SCA) + загрузка HTML-отчёта в artifacts.

### Запуск сканеров локально

```bash
# SAST: SpotBugs (не должен найти ошибок)
./mvnw -B compile com.github.spotbugs:spotbugs-maven-plugin:check

# SCA: OWASP Dependency-Check (генерирует target/dependency-check-report.html)
./mvnw -B org.owasp:dependency-check-maven:check
```

Примечание: в `spotbugs-exclude.xml` отфильтрованы два известных ложных срабатывания для
Spring/DI-кода (`CT_CONSTRUCTOR_THROW`, `EI_EXPOSE_REP2`) — см. комментарии в файле.

Примечание: в `dependency-check` отключён анализатор **Sonatype OSS Index**
(`ossindexAnalyzerEnabled=false` в `pom.xml`), т.к. ему нужен API-ключ, а без него он
возвращает 401 и валит сборку. SCA работает по базе **NVD**, которой достаточно для
Java/Maven-зависимостей. При наличии ключа его можно включить и/или добавить `NVD_API_KEY`.

### Где брать отчёты для скриншотов

В CI отчёты сохраняются как artifacts (`spotbugs-report`, `dependency-check-report`).
Локально: SpotBugs — `target/spotbugsXml.xml`, Dependency-Check — `target/dependency-check-report.html`.