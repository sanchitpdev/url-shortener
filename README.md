# 🔗 URL Shortener — Production-Grade Microservice

![CI/CD Pipeline](https://github.com/sanchitpdev/url-shortener/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Containerised-blue?logo=docker)
![AWS](https://img.shields.io/badge/AWS-ECS%20Fargate-FF9900?logo=amazonaws)

A production-ready URL shortener built to demonstrate real-world backend engineering — containerised with Docker, tested with JUnit + Mockito, deployed to AWS ECS Fargate via a fully automated GitHub Actions CI/CD pipeline.

**Live URL:** `http://urlshortener-alb-1372942651.ap-south-1.elb.amazonaws.com`

---

## Architecture

```
                        ┌─────────────────────────────────┐
                        │         GitHub Actions           │
                        │  test → build → scan → deploy   │
                        └────────────────┬────────────────┘
                                         │
                                    push to ECR
                                         │
Internet ──► ALB ──► ECS Fargate Task ◄──┘
                         │
              ┌──────────┼──────────┐
              │          │          │
         url-service  postgres   redis
         :8080        :5432      :6379
              │          │
         REST API     Stores      Caches
         + redirect   slugs       hot slugs
```

### How it works

1. `POST /api/shorten` — client sends a long URL, gets back a 7-character slug
2. The slug + original URL is saved to **PostgreSQL**
3. The slug is also cached in **Redis** for 24 hours (cache-aside pattern)
4. `GET /:slug` — checks Redis first (cache hit = fast), falls back to Postgres (cache miss)
5. Returns HTTP `302` redirect to the original URL

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Language | Java 21 | LTS, modern features |
| Framework | Spring Boot 4.x | Industry standard for backend |
| Database | PostgreSQL 16 | Reliable, relational |
| Cache | Redis 7 | Sub-millisecond lookups |
| Containerisation | Docker + Compose | Consistent environments |
| CI/CD | GitHub Actions | Automated on every push |
| Registry | AWS ECR | Private Docker image storage |
| Deployment | AWS ECS Fargate | Serverless containers |
| Load Balancer | AWS ALB | Single public URL, health checks |
| Testing | JUnit 5 + Mockito | Fast, isolated unit tests |
| Security Scan | Trivy | Vulnerability scanning on every build |

---

## API Reference

### Shorten a URL

```http
POST /api/shorten
Content-Type: application/json

{
  "originalUrl": "https://github.com",
  "expiryDays": 7
}
```

**Response:**
```json
{
  "slug": "a3f9c12",
  "shortUrl": "http://urlshortener-alb-1372942651.ap-south-1.elb.amazonaws.com/a3f9c12",
  "originalUrl": "https://github.com"
}
```

---

### Redirect

```http
GET /:slug
```

Returns `HTTP 302` with `Location` header pointing to the original URL.

---

### Health Check

```http
GET /actuator/health
```

```json
{
  "status": "UP"
}
```

---

## CI/CD Pipeline

Every push to `main` triggers:

```
1. Spin up Ubuntu runner on GitHub
2. Install Java 21
3. Run unit tests (must pass to continue)
4. Build Docker image (multi-stage)
5. Scan image with Trivy for vulnerabilities
6. Push image to AWS ECR (tagged with commit SHA + latest)
7. Update ECS task definition with new image
8. Deploy to ECS Fargate (rolling update)
9. Wait for deployment stability
```

If tests fail at step 3, the pipeline stops — nothing broken ever reaches production.

---

## Run Locally

### Prerequisites
- Docker + Docker Compose
- Java 21
- Maven

### Start everything with one command

```bash
git clone https://github.com/sanchitpdev/url-shortener.git
cd url-shortener
docker compose up --build -d
```

This starts:
- `url-service` on `http://localhost:8080`
- `postgres` on port `5432`
- `redis` on port `6379`

### Test it

```bash
# Shorten a URL
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://github.com", "expiryDays": 7}'

# Test redirect (replace slug with value from above response)
curl -v http://localhost:8080/YOUR_SLUG

# Health check
curl http://localhost:8080/actuator/health
```

### Run tests only

```bash
cd url-service
./mvnw test
```

---

## Project Structure

```
url-shortener/
├── .github/
│   └── workflows/
│       └── ci.yml              ← GitHub Actions pipeline
├── url-service/
│   ├── Dockerfile              ← Multi-stage Docker build
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/urlshortener/urlservice/
│       │   ├── controller/     ← HTTP layer
│       │   ├── service/        ← Business logic + Redis caching
│       │   ├── repository/     ← JPA data access
│       │   ├── model/          ← UrlMapping entity
│       │   └── dto/            ← Request/Response objects
│       └── test/               ← Unit tests
└── docker-compose.yml          ← Local dev environment
```

---

## AWS Infrastructure

All infrastructure created via AWS CLI:

```
VPC (10.0.0.0/16)
├── Public Subnet ap-south-1a
├── Public Subnet ap-south-1b
├── Internet Gateway
├── Route Table
├── Security Group — ALB (port 80 open)
└── Security Group — ECS (port 8080 from ALB only)

Application Load Balancer
└── Target Group (health check: /actuator/health)
    └── ECS Fargate Cluster
        └── Task Definition (1024 CPU / 2048 MB)
            ├── url-service container
            ├── postgres container
            └── redis container

AWS ECR — private Docker image registry
AWS CloudWatch — container logs (/ecs/urlshortener)
```

---

## Key Engineering Decisions

**Multi-stage Docker build** — Stage 1 compiles with JDK (~400MB), Stage 2 runs with JRE (~180MB). Final image is lean and production-ready.

**Cache-aside pattern** — Every redirect checks Redis first. Only on a cache miss does it hit Postgres. This keeps redirect latency low even under load.

**Depends-on with health checks** — The url-service container only starts after Postgres and Redis pass their health checks. No race conditions on startup.

**Trivy security scanning** — Every Docker image is scanned for CVEs before it's pushed to ECR. High/Critical findings are reported in the pipeline output.

**IAM least privilege** — Dedicated IAM user with only the permissions needed for ECR push and ECS deployment. Root account not used for any pipeline operations.

---

## What I'd do next

- Add analytics service to track click counts per slug
- Move Postgres and Redis to RDS + ElastiCache for data persistence across deployments
- Add HTTPS with ACM certificate on the load balancer
- Implement slug expiry background job
- Add rate limiting on the shorten endpoint

---

## Author

**Sanchit Pawar**

[![GitHub](https://img.shields.io/badge/GitHub-sanchitpdev-181717?logo=github)](https://github.com/sanchitpdev)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-sanchitpawar-0A66C2?logo=linkedin)](https://www.linkedin.com/in/sanchitpawar)
