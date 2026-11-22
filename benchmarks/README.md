# Product read-path benchmark (Phase 4)

Measures the effect of Redis caching on the Product Service read endpoints, for
the report's "performance improvement / metric optimization achieved" section.

## Prerequisites

- Product Service running on `http://localhost:9090`.
- A local Redis (only needed for the cached run):
  - `docker run -p 6379:6379 redis:7`  (or `docker compose up -d redis`), or
  - `brew install redis && redis-server`
- Some data seeded (the service seeds sample products on startup via `InitService`).
- `curl`, `sort`, `awk` (preinstalled on macOS). For "proper" load testing you can
  swap in [`hey`](https://github.com/rakyll/hey) or `wrk`, but `benchmark.sh` needs
  nothing extra.

## Procedure

Run each scenario 3 times and average. The **first** request after a restart is a
cache miss (it populates the cache); the warm-up loop in the script absorbs it.

### 1. Baseline — caching OFF

In `product-service/src/main/resources/application.properties` set:

```
spring.cache.type=none
```

Restart the service, then:

```bash
./benchmark.sh http://localhost:9090/products 1000
./benchmark.sh http://localhost:9090/products/<a-real-uuid> 2000
```

### 2. Cached — Redis ON

Set it back to:

```
spring.cache.type=redis
```

Make sure Redis is up, restart the service, then run the same two commands.

## Record the results

| Endpoint            | Scenario   | avg (ms) | p50 (ms) | p95 (ms) | p99 (ms) |
| ------------------- | ---------- | -------- | -------- | -------- | -------- |
| `/products/{id}`    | no cache   |          |          |          |          |
| `/products/{id}`    | Redis hit  |          |          |          |          |
| `/products`         | no cache   |          |          |          |          |
| `/products`         | Redis hit  |          |          |          |          |

Also note for the report: cache TTL (60s, from `spring.cache.redis.time-to-live`),
dataset size, your hardware, and that the first (miss) request is excluded from the
hit measurements by the warm-up loop.

## How to read it

- `/products/{id}` should show the clearest win — a cache hit skips the DB query and
  entity→DTO mapping entirely.
- `/products` (full list) benefits too, and is where serialization cost is highest,
  so it's a good contrast point to discuss.
- Eviction: creating, updating, or deleting a product clears both caches
  (`@CacheEvict(allEntries = true)`), so the next read reflects the change — no stale
  data. You can demonstrate this: read, update, read again.
