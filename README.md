# E-Commerce Platform (Monorepo)

A Spring Boot microservices platform for an e-commerce application, organised as a
Maven multi-module monorepo. Each service remains independently buildable and
deployable; the parent POM provides shared dependency and plugin management.

## Modules

| Module | Port | Description |
| --- | --- | --- |
| `user-service` | 8081 | Authentication, users, roles, sessions. OAuth2 Authorization Server + JWT. |
| `product-service` | 9090 | Product catalogue, categories, pricing, orders. |

> Planned modules: `common` (shared DTOs / inter-service clients), `api-gateway`
> (Spring Cloud Gateway), `order-service`. These are added as they are built and
> registered under `<modules>` in the root `pom.xml`.

## Repository layout

```
ecommerce-platform/
├── pom.xml                 # parent / aggregator POM (packaging = pom)
├── docker-compose.yml      # local infrastructure (Postgres)
├── user-service/           # imported with full git history
│   ├── pom.xml             # parent = ecommerce-platform
│   └── src/…
└── product-service/        # imported with full git history
    ├── pom.xml             # parent = ecommerce-platform
    └── src/…
```

## Prerequisites

- JDK 17+
- Maven 3.9+ (or use the bundled `./mvnw` inside each module)
- Docker (for local Postgres via `docker-compose`)

## Build

```bash
# Build every module from the repository root
mvn clean install

# Build a single module (and anything it depends on)
mvn -pl user-service -am clean package
```

## Run

```bash
# 1. Start infrastructure
docker compose up -d postgres

# 2. Run a service (from its module directory)
cd user-service && ../mvnw spring-boot:run
# in another terminal
cd product-service && ../mvnw spring-boot:run
```

Both services currently point at the same database (`ECommerceDB`). Connection
settings live in each module's `src/main/resources/application.properties`.

## History

This repository was created by merging two previously separate repositories using
`git subtree`, so the commit history of both services is preserved:

- `user-service` ← `ECommerceUserService`
- `product-service` ← `ECommerceProductService`

