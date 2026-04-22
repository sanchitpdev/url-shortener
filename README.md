🚀 Shipped a production-grade URL Shortener — from code to AWS in one push.

Not just another "Hello World" API. This one's fully containerised, auto-deployed, and live on AWS right now.

🔗 Live demo → https://url-shortener-version1.vercel.app
⚙️ GitHub → https://github.com/sanchitpdev/url-shortener

---

Here's what the stack looks like under the hood:

⚡ Spring Boot 3 + Java 21 — REST API with two clean endpoints: shorten a URL and redirect via slug

🗄️ PostgreSQL — Stores every slug-to-URL mapping with optional expiry timestamps

🔴 Redis (cache-aside pattern) — Every redirect checks Redis first. Cache hit = sub-millisecond response. Cache miss = PostgreSQL fallback + re-cache. No cache invalidation headaches.

🐳 Docker + Docker Compose — Three containers (app, Postgres, Redis) with health-check-gated startup. The app only starts after both dependencies pass their health checks. Zero race conditions.

🔁 GitHub Actions CI/CD — Every push to main triggers:
  → Unit tests (JUnit 5 + Mockito) — must pass to continue
  → Multi-stage Docker image build
  → Trivy vulnerability scan (CVE check before anything ships)
  → Push to AWS ECR (tagged with commit SHA)
  → Rolling deploy to AWS ECS Fargate
  → Wait for deployment stability

☁️ AWS ECS Fargate — Serverless containers sitting behind an Application Load Balancer, inside a custom VPC with properly scoped security groups. IAM least-privilege throughout.

🎨 Frontend — Pure HTML/CSS/JS with a dark dev aesthetic, deployed on Vercel. History persisted in localStorage, one-click copy, live API indicator.

---

The part I'm most proud of?

Nothing broken ever reaches production. Tests are the gate. If tests fail, the pipeline stops — ECR never gets a bad image, ECS never gets a bad deploy. That's the whole point of CI/CD and it's satisfying to see it work.

---

What I'd build next:
→ Click analytics per slug
→ Move Postgres + Redis to RDS + ElastiCache (data persists across ECS task replacements)
→ HTTPS via ACM on the load balancer
→ Rate limiting on the shorten endpoint

---

If you're learning backend development, the single best thing you can do is deploy something real. Not localhost. Not a screenshot. A live URL that anyone can hit.

It forces you to think about things tutorials skip — CORS, health checks, IAM permissions, container startup order, environment variables in production.

---

#Java #SpringBoot #AWS #Docker #DevOps #CICD #Redis #PostgreSQL #BackendDevelopment #SoftwareEngineering #CloudComputing #ECS #GitHubActions #SystemDesign #OpenToWork #BuildInPublic
