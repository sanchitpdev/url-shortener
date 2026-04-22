<div align="center">

<img src="https://readme-typing-svg.demolab.com?font=JetBrains+Mono&weight=700&size=32&pause=1000&color=00D4AA&center=true&vCenter=true&width=600&lines=🔗+URL+Shortener;Production-Grade+Backend;Spring+Boot+%2B+Redis+%2B+AWS" alt="Typing SVG" />

<br/>

[![CI/CD Pipeline](https://github.com/sanchitpdev/url-shortener/actions/workflows/ci.yml/badge.svg)](https://github.com/sanchitpdev/url-shortener/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerised-2496ED?style=flat&logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-ECS%20Fargate-FF9900?style=flat&logo=amazonaws&logoColor=white)

<br/>

**A production-ready URL shortener built to demonstrate real-world backend engineering.**  
Redis cache-aside · PostgreSQL persistence · AWS ECS Fargate · GitHub Actions CI/CD · Live frontend on Vercel

<br/>

[![🌐 Live Demo](https://img.shields.io/badge/🌐%20Live%20Frontend-url--shortener--version1.vercel.app-00D4AA?style=for-the-badge)](https://url-shortener-version1.vercel.app)
&nbsp;&nbsp;
[![⚙️ API](https://img.shields.io/badge/⚙️%20Live%20API-AWS%20ECS%20Fargate-FF9900?style=for-the-badge&logo=amazonaws)](http://urlshortener-alb-1372942651.ap-south-1.elb.amazonaws.com/actuator/health)

</div>

---

## 📸 Preview

<div align="center">
<img src="https://raw.githubusercontent.com/sanchitpdev/url-shortener/main/preview.png" alt="ShrinkIt Frontend Preview" width="700" />
</div>

> Dark-themed frontend live at [url-shortener-version1.vercel.app](https://url-shortener-version1.vercel.app) — paste any URL, get a 7-character slug, one-click copy.

---

## 🏗️ Architecture

```
╔═══════════════════════════════════════════════════════════════╗
║                      GitHub Actions CI/CD                     ║
║         test → build → trivy scan → push → deploy            ║
╚═══════════════════════════╦═══════════════════════════════════╝
                            ║ push image to AWS ECR
                            ▼
  Browser / Vercel  ──►  AWS ALB  (:80)
                            │
                            ▼
                   ┌─────────────────┐
                   │  ECS Fargate    │
                   │    Cluster      │
                   └────────┬────────┘
                            │
           ┌────────────────┼────────────────┐
           ▼                ▼                ▼
     ┌──────────┐    ┌──────────┐    ┌──────────┐
     │url-service│   │PostgreSQL│    │  Redis   │
     │  :8080   │   │  :5432   │    │  :6379   │
     └────┬─────┘   └────┬─────┘    └────┬─────┘
          │              │               │
     REST API +      Persists         Caches
     Redirect        URL slugs       hot slugs
                                    (24h TTL)
```

### ⚡ Request Flow

```
Client sends POST /api/shorten
        │
        ▼
  Generate 7-char slug  ──►  Save to PostgreSQL  ──►  Cache in Redis (24h TTL)
        │
        └──►  Return { slug, shortUrl, originalUrl }


Client visits GET /:slug
        │
        ▼
   Check Redis ──► HIT  ──►  302 Redirect (sub-millisecond ⚡)
        │
        └──► MISS  ──►  Query PostgreSQL  ──►  Re-cache in Redis  ──►  302 Redirect
```

---

## 🛠️ Tech Stack

| Layer | Technology | Why |
|:---:|:---:|:---|
| 🟠 Language | Java 21 | LTS, modern features (records, sealed classes) |
| 🍃 Framework | Spring Boot 3.x | Industry-standard, production-ready backend |
| 🐘 Database | PostgreSQL 16 | Reliable relational storage for slug mappings |
| 🔴 Cache | Redis 7 | Sub-millisecond lookups via cache-aside pattern |
| 🐳 Containers | Docker + Compose | Reproducible local and production environments |
| ⚙️ CI/CD | GitHub Actions | Automated test → build → scan → deploy on every push |
| 📦 Registry | AWS ECR | Private Docker image storage |
| ☁️ Hosting | AWS ECS Fargate | Serverless container deployment |
| ⚖️ Load Balancer | AWS ALB | Single public URL, health checks, traffic routing |
| 🧪 Testing | JUnit 5 + Mockito | Fast, isolated unit tests |
| 🔒 Security | Trivy | CVE scanning on every image before deployment |
| 🎨 Frontend | HTML + CSS + JS | Zero-dependency UI, deployed on Vercel |

---

## 📡 API Reference

### `POST /api/shorten` — Shorten a URL

```http
POST /api/shorten
Content-Type: application/json
```

**Request Body:**

```json
{
  "originalUrl": "https://github.com/sanchitpdev/url-shortener",
  "expiryDays": 7
}
```

| Field | Type | Required | Description |
|---|---|:---:|---|
| `originalUrl` | `string` | ✅ | The long URL to shorten |
| `expiryDays` | `integer` | ❌ | Days until the link expires (omit for permanent) |

**Response `200 OK`:**

```json
{
  "slug": "a3f9c12",
  "shortUrl": "http://urlshortener-alb-1372942651.ap-south-1.elb.amazonaws.com/a3f9c12",
  "originalUrl": "https://github.com/sanchitpdev/url-shortener"
}
```

---

### `GET /:slug` — Redirect

```http
GET /a3f9c12
```

```
HTTP/1.1 302 Found
Location: https://github.com/sanchitpdev/url-shortener
```

Checks Redis first → falls back to PostgreSQL on cache miss → returns `HTTP 302`.

---

### `GET /actuator/health` — Health Check

```http
GET /actuator/health
```

```json
{ "status": "UP" }
```

---

### 🧪 Try it with cURL

```bash
# 1. Shorten a URL
curl -X POST http://urlshortener-alb-1372942651.ap-south-1.elb.amazonaws.com/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://github.com/sanchitpdev", "expiryDays": 7}'

# 2. Follow the redirect (replace YOUR_SLUG)
curl -v http://urlshortener-alb-1372942651.ap-south-1.elb.amazonaws.com/YOUR_SLUG

# 3. Health check
curl http://urlshortener-alb-1372942651.ap-south-1.elb.amazonaws.com/actuator/health
```

---

## 🔁 CI/CD Pipeline

Every push to `main` triggers the full pipeline automatically:

```
Push to main
    │
    ▼
╔══════════════╗
║   1. Test    ║  JUnit 5 + Mockito — pipeline STOPS if any test fails
╚══════╦═══════╝
       ║ ✅ all tests pass
       ▼
╔══════════════╗
║  2. Build   ║  Multi-stage Docker image (JDK build → JRE runtime)
╚══════╦═══════╝
       ▼
╔══════════════╗
║  3. Scan    ║  Trivy CVE scan — HIGH/CRITICAL findings reported
╚══════╦═══════╝
       ▼
╔══════════════╗
║  4. Push    ║  Push to AWS ECR (tagged: commit SHA + latest)
╚══════╦═══════╝
       ▼
╔══════════════╗
║  5. Deploy  ║  Update ECS task definition → rolling deploy → wait for stability
╚══════════════╝
```

> 💡 Tests are the gate — nothing broken ever reaches production.

---

## 🚀 Run Locally

### Prerequisites

![Java](https://img.shields.io/badge/Java-21+-ED8B00?logo=openjdk&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Desktop-2496ED?logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?logo=apachemaven&logoColor=white)

### Start everything with one command

```bash
git clone https://github.com/sanchitpdev/url-shortener.git
cd url-shortener
docker compose up --build -d
```

This spins up **3 containers**:

| Container | Port | Description |
|---|---|---|
| `url-service` | `8080` | The Spring Boot API |
| `postgres` | `5432` | PostgreSQL database |
| `redis` | `6379` | Redis cache |

> ✅ The `url-service` only starts **after** Postgres and Redis pass their health checks. No race conditions.

### Run tests only

```bash
cd url-service
./mvnw test
```

---

## 📁 Project Structure

```
url-shortener/
│
├── 📂 .github/
│   └── 📂 workflows/
│       └── 📄 ci.yml                  ← GitHub Actions pipeline
│
├── 📂 url-service/
│   ├── 📄 Dockerfile                  ← Multi-stage Docker build
│   ├── 📄 pom.xml
│   └── 📂 src/
│       ├── 📂 main/java/.../urlservice/
│       │   ├── 📂 config/             ← CORS configuration
│       │   ├── 📂 controller/         ← HTTP layer (shorten + redirect)
│       │   ├── 📂 service/            ← Business logic + Redis cache-aside
│       │   ├── 📂 repository/         ← Spring Data JPA (PostgreSQL)
│       │   ├── 📂 model/              ← UrlMapping JPA entity
│       │   ├── 📂 dto/                ← ShortenRequest / ShortenResponse
│       │   └── 📂 exception/          ← Global exception handler
│       └── 📂 test/                   ← Unit tests (JUnit 5 + Mockito)
│
├── 📄 docker-compose.yml              ← Local dev (3 services with health checks)
└── 📄 task-definition.json            ← AWS ECS task definition
```

---

## ☁️ AWS Infrastructure

```
VPC (10.0.0.0/16)
├── 🌐 Public Subnet ap-south-1a
├── 🌐 Public Subnet ap-south-1b
├── 🔀 Internet Gateway
├── 🛣️  Route Table
├── 🔒 Security Group — ALB   (port 80 open to internet)
└── 🔒 Security Group — ECS   (port 8080 open from ALB only)

⚖️  Application Load Balancer
    └── 🎯 Target Group  (health: GET /actuator/health)
        └── 📦 ECS Fargate Cluster
            └── 📋 Task Definition  (1024 CPU / 2048 MB)
                ├── 🟢 url-service  (:8080)
                ├── 🐘 postgres     (:5432)
                └── 🔴 redis        (:6379)

📦 AWS ECR      → Private Docker image registry
📊 CloudWatch   → Container logs  (/ecs/urlshortener)
```

> **IAM Least Privilege** — Dedicated IAM user with only the permissions needed for ECR push and ECS deploy. Root account never used in the pipeline.

---

## 🧠 Key Engineering Decisions

<details>
<summary><b>🐳 Multi-Stage Docker Build</b></summary>
<br/>
Stage 1 compiles the application using a full JDK (~400 MB). Stage 2 runs the JAR using a lean JRE (~180 MB). The final image is significantly smaller, faster to pull, and has a smaller attack surface.
</details>

<details>
<summary><b>🔴 Cache-Aside Pattern with Redis</b></summary>
<br/>
Every redirect checks Redis first. Only on a cache miss does it fall back to PostgreSQL — and then re-caches the result for the next 24 hours. This keeps redirect latency low under high load without requiring complex cache invalidation logic.
</details>

<details>
<summary><b>✅ Health-Check-Gated Startup</b></summary>
<br/>
Docker Compose is configured so <code>url-service</code> depends on both Postgres and Redis with explicit health checks. The app container only starts once both dependencies are confirmed healthy — eliminating "connection refused" errors during startup.
</details>

<details>
<summary><b>🔒 Trivy Vulnerability Scanning</b></summary>
<br/>
Every Docker image is scanned for known CVEs before being pushed to ECR. HIGH and CRITICAL findings are surfaced in the pipeline output. Vulnerable images never reach production.
</details>

<details>
<summary><b>⏳ Slug Expiry Support</b></summary>
<br/>
The <code>UrlMapping</code> entity stores an optional <code>expiresAt</code> timestamp, enabling time-limited short links at the API level. Expiry is set per-request via the optional <code>expiryDays</code> field.
</details>

---

## 🗺️ What's Next

- [ ] 📊 Analytics microservice — track click counts and referrers per slug
- [ ] 🗄️ Move to RDS + ElastiCache — data persists across ECS task replacements
- [ ] 🔐 HTTPS via ACM certificate on the load balancer
- [ ] 🧹 Background job to clean up expired slugs from PostgreSQL
- [ ] 🚦 Rate limiting on `POST /api/shorten` to prevent abuse

---

## 👤 Author

<div align="center">

**Sanchit Pawar** — Java Backend Developer

[![GitHub](https://img.shields.io/badge/GitHub-sanchitpdev-181717?style=for-the-badge&logo=github)](https://github.com/sanchitpdev)
&nbsp;&nbsp;
[![LinkedIn](https://img.shields.io/badge/LinkedIn-sanchitpawar-0A66C2?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/sanchitpawar)
&nbsp;&nbsp;
[![Live Demo](https://img.shields.io/badge/Live%20Demo-url--shortener--version1.vercel.app-00D4AA?style=for-the-badge)](https://url-shortener-version1.vercel.app)

</div>

---

<div align="center">
  <sub>If this project helped you, consider giving it a ⭐</sub>
</div>
