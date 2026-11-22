# E-Commerce Microservices Platform

A backend-focused, event-driven **e-commerce platform** built as a Spring Boot **Maven multi-module monorepo**. It demonstrates the core patterns of a modern microservices system: an API gateway, JWT-based authentication, service-to-service calls, Redis caching, asynchronous messaging over Apache Kafka, containerised deployment, and a React storefront.

> Built as the applied project for the Scaler Neovarsity — Woolf **M.Sc. Computer Science (Backend Specialization)**.

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Services](#services)
4. [Technology Stack](#technology-stack)
5. [Event-Driven Messaging](#event-driven-messaging)
6. [Repository Structure](#repository-structure)
7. [Prerequisites](#prerequisites)
8. [Build](#build)
9. [Run Locally](#run-locally)
10. [Configuration](#configuration)
11. [API Reference](#api-reference)
12. [Authentication Flow](#authentication-flow)
13. [Caching](#caching)
14. [Order → Payment Choreography](#order--payment-choreography)
15. [Frontend](#frontend)
16. [Deployment (AWS)](#deployment-aws)
17. [Project Report](#project-report)

---

## Overview

The platform decomposes a monolithic storefront into **seven independently deployable services** coordinated by an API gateway. Clients never talk to a service directly for the core flows — they go through the gateway, which handles routing and path rewriting. Services communicate **synchronously** over REST for request/response needs (e.g. price lookups, token validation) and **asynchronously** over Kafka for side effects (notifications, stock updates, payment settlement), keeping the services loosely coupled.

Key capabilities:

- **Stateless JWT authentication** issued by the User Service and validated at the edge of every protected service.
- **Catalogue browsing** with Redis **cache-aside** to keep product reads fast.
- **Order placement** that validates items, resolves prices from the Product Service, and emits an `order.placed` event.
- **Event choreography**: inventory, notification, and product services react to orders; a mock payment settles the order and flips it to `PAID`.
- **Containerised** with Docker and a single `docker-compose.yml` for one-command local infrastructure.

## Architecture

```
                         ┌─────────────────────────┐
                         │   React Storefront (SPA) │
                         └────────────┬────────────┘
                                      │ HTTPS  /api/**
                         ┌────────────▼────────────┐
                         │   API Gateway  :8080     │   Spring Cloud Gateway
                         │  routes + RewritePath    │
                         └───┬─────────┬─────────┬──┘
             /api/auth,users │         │ /api/products   │ /api/orders
             /api/roles      │         │                 │
                   ┌─────────▼──┐  ┌───▼────────┐   ┌────▼────────┐
                   │ User  :8081│  │Product:9090│   │ Order :9091 │
                   └────┬───────┘  └────┬───────┘   └────┬────────┘
                        │               │ cache          │ price lookup (REST)
                        │          ┌────▼────┐           │
                        │          │  Redis  │           │
                        │          └─────────┘           │
         user.signup    │        order.placed            │ order.placed
                        ▼               ▼                 ▼
                 ┌───────────────────────────────────────────────┐
                 │               Apache Kafka (event bus)         │
                 └───┬───────────────┬───────────────┬───────────┘
      payment.completed │   order.placed │   order.placed │
                   ┌────▼─────┐   ┌──────▼─────┐   ┌──────▼────────┐
                   │Order(PAID)│   │Inventory   │   │ Notification  │
                   │           │   │   :9094    │   │    :9093      │
                   └───────────┘   └────────────┘   └───────────────┘

              Payment :9095  ──(POST /payments, direct)──▶ publishes payment.completed
```

All services share a **PostgreSQL** instance (one logical schema per service — database-per-service in production).

## Services

| Service | Port | Responsibility | Key Tech |
|---|---|---|---|
| **api-gateway** | 8080 | Single entry point; routes `/api/**` to services and rewrites paths | Spring Cloud Gateway (reactive) |
| **user-service** | 8081 | Signup, login, JWT issuance, sessions, roles, token validation | Spring Security, JJWT, JPA |
| **product-service** | 9090 | Product catalogue, categories, prices; Redis cache-aside | JPA, Redis, Kafka consumer |
| **order-service** | 9091 | Order creation, price resolution, order state machine | JPA, RestClient, Kafka |
| **payment-service** | 9095 | Mock payment settlement; emits `payment.completed` | JPA, Kafka producer |
| **inventory-service** | 9094 | Stock levels per product; reacts to orders | JPA, Kafka consumer |
| **notification-service** | 9093 | Logs notifications for signups and orders | Kafka consumer |
| **common** | — | Shared token-validation client & filter (library module) | plain Java + spring-web |

## Technology Stack

- **Language / Runtime:** Java 17 (build with JDK 21 when using a very recent toolchain, see [Build](#build))
- **Framework:** Spring Boot 3.2.2, Spring Cloud 2023.0.1
- **API Gateway:** Spring Cloud Gateway (reactive)
- **Security:** Spring Security + JSON Web Tokens (JJWT 0.12.3, HMAC-SHA)
- **Persistence:** Spring Data JPA + PostgreSQL
- **Cache:** Redis (Spring Cache abstraction, cache-aside)
- **Messaging:** Apache Kafka (KRaft mode) via Spring for Apache Kafka
- **Build:** Maven multi-module (parent aggregator POM)
- **Containers:** Docker (multi-stage) + Docker Compose
- **Frontend:** React 18, Vite 5, React Router 6, Axios

## Event-Driven Messaging

Topics are plain JSON string payloads keyed by the relevant entity id.

| Topic | Producer | Consumers | Purpose |
|---|---|---|---|
| `user.signup` | user-service | notification-service | Welcome / onboarding notification |
| `order.placed` | order-service | product-service, inventory-service, notification-service | Reserve stock, notify, project catalogue views |
| `payment.completed` | payment-service | order-service | Settle the order → status `PAID` |

## Repository Structure

```
ecommerce-platform/
├── pom.xml                  # parent aggregator POM (8 modules)
├── docker-compose.yml       # postgres, redis, kafka + service containers
├── common/                  # shared token-validation library
├── api-gateway/
├── user-service/
├── product-service/
├── order-service/
├── payment-service/
├── inventory-service/
├── notification-service/
├── frontend/                # React + Vite storefront
├── benchmarks/              # cache latency benchmark harness
└── docs/                    # deployment design
```

## Prerequisites

- **JDK 17+** (JDK 21 recommended — see note under Build)
- **Maven 3.9+** (or use the bundled toolchain)
- **Docker** & **Docker Compose** (for Postgres, Redis, Kafka)
- **Node.js 18+** (only to run the frontend)

## Build

From the repository root:

```bash
mvn -DskipTests clean install
```

This compiles and packages all eight modules. No running infrastructure is required for a build.

> **JDK note:** the project targets Java 17, but Lombok 1.18.30 does not run under the newest JDKs. If your default `java` is very recent (e.g. JDK 25+), build with JDK 21:
>
> ```bash
> export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
> mvn -DskipTests clean install
> ```

## Run Locally

### Option A — Docker Compose (recommended)

Brings up Postgres, Redis, Kafka and the core service containers:

```bash
docker compose up --build
```

### Option B — Infrastructure in Docker, services from your IDE

```bash
docker compose up postgres redis kafka
```

Then run each `*-service` from your IDE or with `mvn spring-boot:run -pl <module>`. Start the gateway last.

Default service URLs once running: gateway `http://localhost:8080`, and services on the ports listed in [Services](#services).

## Configuration

Each service reads configuration from its `application.properties`. The Docker profile (`SPRING_PROFILES_ACTIVE=docker`) points services at the compose network hostnames (`postgres`, `redis`, `kafka`); the default profile points at `localhost`.

| Setting | Default (local) | Notes |
|---|---|---|
| PostgreSQL | `localhost:5432` | shared instance, per-service schema |
| Redis | `localhost:6379` | product-service cache |
| Kafka | `localhost:9092` | dual-listener (host + in-container `kafka:29092`) |
| `jwt.secret` | configured value | HMAC signing key for JWTs |

Gateway routes are parameterised with environment variables (e.g. `USERSERVICE_URI`, `PRODUCTSERVICE_URI`, `ORDERSERVICE_URI`) so they can be overridden per environment.

## API Reference

All client traffic goes through the gateway at `http://localhost:8080` using the `/api` prefix (the gateway strips `/api` before forwarding).

### User Service — `/api/auth`, `/api/users`, `/api/roles`
| Method | Path | Description |
|---|---|---|
| POST | `/api/users/signup` | Register a new user |
| POST | `/api/auth/login` | Authenticate; returns a JWT (body + cookie) |
| GET | `/api/auth/validate` | Validate a bearer token (used by services) |
| GET | `/api/auth/session` | Current session details |
| POST | `/api/users/{id}/roles` | Assign a role to a user |

### Product Service — `/api/products`
| Method | Path | Description |
|---|---|---|
| GET | `/api/products` | List products (cached) |
| GET | `/api/products/{id}` | Get a product by UUID (cached) |
| GET | `/api/products/title/{title}` | Find by title |
| POST | `/api/products` | Create a product |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |

### Order Service — `/api/orders`
| Method | Path | Description |
|---|---|---|
| POST | `/api/orders` | Place an order (validates items, resolves prices, emits `order.placed`) |
| GET | `/api/orders` | List the authenticated user's orders |

### Payment Service — `/payments` (called directly on `:9095`)
| Method | Path | Description |
|---|---|---|
| POST | `/payments` | Settle a payment for an order; emits `payment.completed` |

### Inventory Service — `/inventory` (called directly on `:9094`)
| Method | Path | Description |
|---|---|---|
| GET | `/inventory/{productId}` | Current stock level |
| PUT | `/inventory/{productId}` | Set stock level |

## Authentication Flow

1. The client calls `POST /api/users/signup`, then `POST /api/auth/login`.
2. The User Service verifies credentials and issues a **stateless JWT** (HMAC-signed, with a stable key from configuration), returned both in the response body and as a cookie.
3. For protected calls, the client sends `Authorization: Bearer <token>`.
4. Downstream services validate the token via the shared **`common`** module (`TokenValidationFilter` → `TokenValidationClient` → User Service `/auth/validate`) and populate an auth context with the caller's user id.

## Caching

The Product Service uses **cache-aside** with Redis:

- Reads (`getProduct`, `getAllProducts`) are annotated with `@Cacheable` (`products`, `productList`).
- Writes (`create`, `update`, `delete`) evict the relevant entries with `@CacheEvict`.

On a cache miss the product is loaded from PostgreSQL and written back to Redis; subsequent reads are served from cache, removing the database round-trip. A benchmark harness under `benchmarks/` measures the before/after read latency.

## Order → Payment Choreography

1. `POST /api/orders` → Order Service validates the items, looks up each price from the Product Service (REST), persists the order as `CREATED`, and publishes **`order.placed`**.
2. `order.placed` is consumed by **product**, **inventory**, and **notification** services.
3. A payment is submitted explicitly via `POST /payments` (mock gateway). The Payment Service records the payment as `COMPLETED` and publishes **`payment.completed`**.
4. The Order Service consumes `payment.completed` and transitions the order to **`PAID`**.

## Frontend

A React + Vite single-page storefront lives in `frontend/`:

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` to the gateway at `http://localhost:8080` (avoiding CORS). It includes signup/login, catalogue browsing, a cart, and an orders view, using the JWT returned by the login endpoint.

## Deployment (AWS)

The target topology (see `docs/`) places the services behind an **Application Load Balancer** in a VPC: the app tier (gateway + services) runs in private subnets on EC2/ECS Auto Scaling groups, with managed data services — **Amazon RDS (PostgreSQL, Multi-AZ)**, **Amazon ElastiCache (Redis)**, and **Amazon MSK (Kafka)** — in isolated data subnets. Security groups restrict traffic to the minimum required between tiers.

## Project Report

A detailed applied-project report (architecture, class diagrams, database schema, feature development, benchmarking, deployment, and technologies) accompanies this repository as part of the Scaler Neovarsity M.Sc. submission.

---

*Author: Mehul Manhas · Scaler Neovarsity — Woolf, M.Sc. Computer Science (Backend Specialization).*
