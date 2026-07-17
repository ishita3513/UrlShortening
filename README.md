# URL Shortening Service

A Spring Boot URL shortener with custom aliases, expiry, click tracking, a QR code,
and a Redis-backed lookup cache in front of an H2 database.

## Prerequisites

- Java 17+
- Maven 3.9+ (not bundled with the repo — install your own, or use Docker instead)
- Redis (required — the app will fail on cache reads/writes if it can't reach one)
- Docker (optional, only needed for the container-based run/Redis options below)

## 1. Install

Clone the repo, then resolve dependencies:

```bash
mvn clean install
```

## 2. Start Redis

The app needs a reachable Redis instance. Pick one:

**Docker (quickest):**

```bash
docker run -d --name url-shortener-redis -p 6379:6379 redis:7-alpine
```

**Local install:** start your system's Redis service on port 6379.

By default the app connects to `localhost:6379` with no auth. To point at a different
instance (e.g. a hosted Redis), set these environment variables before running:

| Variable         | Default     | Purpose                                  |
|-------------------|-------------|-------------------------------------------|
| `REDIS_HOST`      | `localhost` | Redis host                                |
| `REDIS_PORT`      | `6379`      | Redis port                                |
| `REDIS_USERNAME`  | *(empty)*   | Redis username, if ACLs are enabled       |
| `REDIS_PASSWORD`  | *(empty)*   | Redis password                            |
| `REDIS_SSL`       | `false`     | Set `true` for TLS-only providers         |

## 3. Run

```bash
mvn spring-boot:run
```

The app starts on **http://localhost:8080**. Override the port with the `PORT` env var.

The database is H2, **in-memory** — schema is created automatically on startup and all
data is lost when the app stops.

### Run with Docker instead

A `Dockerfile` is included. Build the jar first, then the image:

```bash
mvn clean package
docker build -t url-shortener .
docker run -p 8080:8080 -e REDIS_HOST=host.docker.internal url-shortener
```

## 4. Use it

- **Web UI:** open http://localhost:8080 — fill in a URL, optional custom alias, and
  expiry, then submit. You'll get back a short code, short URL (with a copy button),
  and a QR code.
- **H2 console** (inspect the database directly): http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:urlshortener`
  - User: `sa`, Password: *(blank)*

## 5. Test

There are no automated tests in this project yet. To verify things work end-to-end,
exercise the API manually:

**Create a short URL:**

```bash
curl -X POST http://localhost:8080/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com/some/long/path", "expiredInDays": 7}'
```

Expect a `201 Created` with a JSON body containing `shortCode`, `shortUrl`,
`originalUrl`, and `expiresAt`.

**Follow the short URL:**

```bash
curl -i http://localhost:8080/<shortCode>
```

Expect a `302 Found` with a `Location` header pointing back at the original URL.

**Custom alias + conflict check:**

```bash
curl -X POST http://localhost:8080/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com", "customAlias": "my-alias"}'

# Re-run the same request — expect 409 Conflict, alias already taken
```

**Validation errors** — expect `400 Bad Request`:

```bash
curl -X POST http://localhost:8080/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "not-a-url"}'
```

**Rate limiting** — the API allows 10 URL-creation requests per client per app
lifetime (not a rolling window); the 11th request returns `429 Too Many Requests`.

**Expired/unknown short codes** return `410 Gone` and `404 Not Found` respectively.

## API summary

| Method | Path            | Description                          |
|--------|-----------------|---------------------------------------|
| POST   | `/urls`     | Create a short URL                    |
| GET    | `/{shortCode}`  | Redirect to the original URL          |

### `POST /urls` request body

| Field           | Required | Rules                                              |
|------------------|----------|-----------------------------------------------------|
| `originalUrl`    | yes      | Must start with `http://` or `https://`             |
| `customAlias`    | no       | 2-50 chars, letters/numbers/hyphens only            |
| `expiredInDays`  | no       | 1-3650, defaults to 30                              |
