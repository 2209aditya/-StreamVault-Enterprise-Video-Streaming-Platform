# 🎬 StreamVault — Enterprise Video Streaming Platform

<div align="center">

![StreamVault](https://img.shields.io/badge/StreamVault-Enterprise-blue?style=for-the-badge)
![Scale](https://img.shields.io/badge/Scale-10M%20Concurrent%20Viewers-success?style=for-the-badge)
![Cloud](https://img.shields.io/badge/Cloud-Microsoft%20Azure-0078D4?style=for-the-badge&logo=microsoft-azure)
![License](https://img.shields.io/badge/License-Proprietary-red?style=for-the-badge)

**Cloud-Native · Microservices · 10 Million Concurrent Viewers · Enterprise-Grade**

[Architecture](#-architecture-overview) · [Environments](#-environments) · [CI/CD](#-cicd-pipeline) · [Infra & DR](#-disaster-recovery) · [Monitoring](#-monitoring--observability) · [Testing](#-testing-strategy) · [Security](#-authentication--security) · [Getting Started](#-getting-started)

</div>

---

## 📋 Table of Contents

1. [Project Overview](#-project-overview)
2. [Architecture Overview](#-architecture-overview)
3. [High-Level Design (HLD)](#-high-level-design-hld)
4. [Low-Level Design (LLD)](#-low-level-design-lld)
5. [Environments](#-environments)
6. [Azure Infrastructure](#-azure-infrastructure)
7. [Database Design](#-database-design)
8. [Authentication & Security](#-authentication--security)
9. [RBAC & IAM Rules](#-rbac--iam-rules)
10. [CI/CD Pipeline](#-cicd-pipeline)
11. [Blue-Green Deployment](#-blue-green-deployment)
12. [ArgoCD & GitOps](#-argocd--gitops)
13. [Helm Charts](#-helm-charts)
14. [Disaster Recovery (DR)](#-disaster-recovery)
15. [Scalability](#-scalability)
16. [Fault Tolerance](#-fault-tolerance)
17. [Monitoring & Observability](#-monitoring--observability)
18. [Testing Strategy](#-testing-strategy)
19. [App Availability & SLA](#-app-availability--sla)
20. [Project Structure](#-project-structure)
21. [Getting Started](#-getting-started)
22. [Environment Variables](#-environment-variables)
23. [API Documentation](#-api-documentation)
24. [Contributing](#-contributing)

---

## 🚀 Project Overview

StreamVault is a **cloud-native, enterprise-grade video streaming platform** engineered to handle **10 million concurrent viewers**. Built on Microsoft Azure, it leverages modern microservices architecture, CDN-backed video delivery, real-time analytics, and enterprise-grade security.

| Attribute | Details |
|---|---|
| **Scale Target** | 10 million concurrent viewers |
| **Cloud Provider** | Microsoft Azure |
| **Frontend** | Angular 17+ (SPA) |
| **Backend** | Spring Boot 3.x (Java 21) |
| **Database** | PostgreSQL 15 (Azure Database for PostgreSQL Flexible Server) |
| **Cache** | Azure Cache for Redis |
| **CDN** | Azure Front Door + Azure CDN |
| **Auth** | Azure Active Directory B2C + SSO (OIDC/SAML 2.0) |
| **Monitoring** | Dynatrace APM |
| **Container Orchestration** | Azure Kubernetes Service (AKS) |
| **GitOps** | ArgoCD |

---

## 🏗 Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                           USERS (10 Million Concurrent)                          │
└────────────────────────────────────┬─────────────────────────────────────────────┘
                                     │
                          ┌──────────▼──────────┐
                          │  Azure Front Door    │  ← Global Load Balancer + WAF
                          │  (CDN + WAF Layer)   │  ← DDoS Protection
                          └──────────┬──────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
    ┌─────────▼──────┐    ┌─────────▼──────┐    ┌─────────▼──────┐
    │  Static Assets  │    │  API Gateway   │    │  Video Streams │
    │  (Azure CDN)   │    │ (APIM + NGINX) │    │  (Azure CDN)   │
    └─────────────────┘    └───────┬────────┘    └────────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
           ┌────────▼───┐  ┌──────▼────┐  ┌─────▼──────┐
           │  Auth Svc  │  │  User Svc │  │ Video Svc  │
           │ (AAD B2C)  │  │(Spring)   │  │ (Spring)   │
           └────────────┘  └──────┬────┘  └─────┬──────┘
                                  │              │
                    ┌─────────────┴──────────────┘
                    │
          ┌─────────▼──────────┐
          │  Azure Service Bus │  ← Async Event Streaming
          │  (Message Queue)   │
          └─────────┬──────────┘
                    │
     ┌──────────────┼──────────────┐
     │              │              │
┌────▼────┐  ┌──────▼────┐  ┌─────▼─────┐
│ Encoder │  │Analytics  │  │Notification│
│ Service │  │  Service  │  │  Service  │
└────┬────┘  └───────────┘  └───────────┘
     │
┌────▼────────────────┐
│ Azure Blob Storage  │  ← HLS/DASH Video Segments
│ (Video Assets)      │
└─────────────────────┘
```

---

## 📐 High-Level Design (HLD)

### 1. Core Services

| Service | Technology | Purpose |
|---|---|---|
| API Gateway | Azure API Management + NGINX Ingress | Rate limiting, routing, throttling |
| Auth Service | Azure AD B2C + Spring Security | SSO, JWT, OAuth 2.0 |
| User Service | Spring Boot | User profiles, subscriptions, preferences |
| Video Service | Spring Boot | Video metadata, catalog, search |
| Streaming Service | Spring Boot + Azure Media Services | HLS/DASH adaptive bitrate |
| Encoder Service | Azure Media Services | Transcoding to multiple resolutions |
| Analytics Service | Spring Boot + Azure Stream Analytics | Real-time viewing metrics |
| Notification Service | Spring Boot + Azure Communication Services | Email, push, SMS |
| Recommendation Service | Spring Boot + Azure ML | AI-powered content recommendations |

### 2. Azure Networking Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 Azure Virtual Network (VNet)             │
│                   10.0.0.0/8                            │
│                                                          │
│  ┌─────────────────────────────────────────────────┐    │
│  │           Public Subnet (10.1.0.0/16)           │    │
│  │   Azure Front Door → Application Gateway        │    │
│  └─────────────────────────────────────────────────┘    │
│                          │                              │
│  ┌─────────────────────────────────────────────────┐    │
│  │           AKS Subnet (10.2.0.0/16)              │    │
│  │   Pods: 10.2.1.0/24  |  Services: 10.2.2.0/24   │    │
│  └─────────────────────────────────────────────────┘    │
│                          │                              │
│  ┌─────────────────────────────────────────────────┐    │
│  │           Data Subnet (10.3.0.0/16)             │    │
│  │   PostgreSQL | Redis | Storage (Private Endpoint)│    │
│  └─────────────────────────────────────────────────┘    │
│                          │                              │
│  ┌─────────────────────────────────────────────────┐    │
│  │          Private Subnet (10.4.0.0/16)           │    │
│  │   Azure Functions | Key Vault | App Config       │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

### 3. Caching Strategy (Multi-Layer)

```
Request Flow:
Browser → Azure Front Door (Edge Cache) → Azure Redis Cache → PostgreSQL

Layer 1: Browser Cache         → Static assets (images, JS, CSS) — TTL: 7 days
Layer 2: Azure CDN Edge Cache  → Video segments, thumbnails       — TTL: 24 hours
Layer 3: Azure Redis Cache     → API responses, user sessions     — TTL: 15 min
Layer 4: Spring Boot L2 Cache  → Hibernate 2nd level cache        — TTL: 5 min
Layer 5: PostgreSQL            → Hot data (materialized views)
```

**Redis Cache Namespaces:**

| Key Pattern | Content | TTL |
|---|---|---|
| `session:{userId}` | User session token | 24h |
| `video:meta:{videoId}` | Video metadata | 30 min |
| `user:profile:{userId}` | User profile data | 15 min |
| `catalog:trending` | Trending videos list | 5 min |
| `stream:token:{videoId}` | Streaming token | 4h |
| `ratelimit:{ip}` | Rate limit counter | 1 min |

### 4. Video Delivery Pipeline

```
Upload → Azure Blob (Raw) → Azure Media Services (Encoder)
       → Multiple Resolutions (4K/1080p/720p/480p/360p)
       → HLS/DASH Manifests
       → Azure CDN Distribution
       → Viewer (Adaptive Bitrate Streaming)
```

---

## 🔩 Low-Level Design (LLD)

### 1. Microservice Communication

```
Synchronous:   REST (HTTP/2) via APIM for client-facing APIs
Asynchronous:  Azure Service Bus (topics/subscriptions) for events
Event Stream:  Azure Event Hubs for analytics (millions of events/sec)
Internal:      gRPC for inter-service calls (low latency)
```

### 2. User Service — Class Design

```java
// Domain Model
User {
  UUID id
  String email
  String displayName
  SubscriptionTier tier         // FREE, BASIC, PREMIUM, ENTERPRISE
  LocalDateTime createdAt
  OAuthProvider oauthProvider   // AAD, GOOGLE, GITHUB
  String externalId             // SSO external ID
  boolean active
}

// Repository
UserRepository extends JpaRepository<User, UUID>
  findByEmail(email): Optional<User>
  findByExternalId(externalId, provider): Optional<User>

// Service Layer
UserService {
  createOrUpdateFromSSO(OidcUser): User
  updateSubscription(userId, tier): User
  getWatchHistory(userId, pageable): Page<WatchEvent>
}
```

### 3. Video Streaming — Adaptive Bitrate

```java
// Streaming Token Generation
StreamingToken {
  UUID videoId
  UUID userId
  String signedUrl       // Azure SAS token
  Instant expiresAt      // 4-hour window
  Set<Resolution> allowed
  GeoRestriction geo
}

// HLS Manifest Structure
/stream/{videoId}/master.m3u8
  ├── /stream/{videoId}/1080p/index.m3u8
  ├── /stream/{videoId}/720p/index.m3u8
  └── /stream/{videoId}/480p/index.m3u8
```

### 4. Azure Functions

| Function Name | Trigger | Purpose |
|---|---|---|
| `VideoEncoderFunction` | Blob trigger (upload) | Kick off encoding job |
| `StreamTokenFunction` | HTTP trigger | Generate short-lived SAS streaming URL |
| `WatchEventFunction` | Service Bus trigger | Process watch events → PostgreSQL |
| `ThumbnailGenFunction` | Blob trigger | Auto-generate video thumbnails |
| `PurgeExpiredTokensFunction` | Timer trigger (hourly) | Clean up expired stream tokens |
| `UsageMetricsFunction` | Timer trigger (5 min) | Aggregate viewer metrics to Redis |
| `RecommendationRefreshFunction` | Timer trigger (daily) | Refresh ML recommendations |

---

## 🌍 Environments

StreamVault operates across **five isolated environments** — Development, UAT, Staging (Pre-Prod), Production, and Disaster Recovery — each with dedicated Azure subscriptions and Kubernetes namespaces.

### Environment Overview

| Environment | Purpose | Azure Subscription | AKS Namespace | URL |
|---|---|---|---|---|
| **DEV** | Active development, feature integration | `StreamVault-Dev-Sub` | `development` | `https://dev.streamvault.internal` |
| **UAT** | User acceptance testing, stakeholder sign-off | `StreamVault-UAT-Sub` | `uat` | `https://uat.streamvault.com` |
| **STAGING** | Pre-production, load & regression testing | `StreamVault-Staging-Sub` | `staging` | `https://staging.streamvault.com` |
| **PRODUCTION** | Live traffic, 10M concurrent users | `StreamVault-Prod-Sub` | `production` | `https://streamvault.com` |
| **DR** | Disaster recovery, standby failover | `StreamVault-DR-Sub` | `dr-production` | `https://dr.streamvault.com` |

### Environment Configuration Matrix

| Config | DEV | UAT | STAGING | PROD | DR |
|---|---|---|---|---|---|
| AKS Node Count | 3–10 | 5–20 | 10–40 | 10–100 | 10–80 |
| PostgreSQL SKU | General Purpose, 4 vCores | General Purpose, 8 vCores | Business Critical, 16 vCores | Business Critical, 32 vCores | Business Critical, 32 vCores |
| Redis SKU | C2 Basic | C3 Standard | P2 Premium | P3 Premium | P3 Premium |
| Replicas (backend) | 2 | 3 | 5 | 10–100 | 10–80 |
| Auto-scaling | ✅ | ✅ | ✅ | ✅ | ✅ |
| Blue-Green Deploy | ❌ | ❌ | ✅ | ✅ | ✅ |
| Geo-Redundancy | ❌ | ❌ | ❌ | ✅ | ✅ |
| CDN | ❌ | ✅ | ✅ | ✅ | ✅ |
| WAF | ❌ | ✅ | ✅ | ✅ | ✅ |
| Dynatrace APM | Basic | Standard | Full | Full | Full |
| Data Masking (PII) | ✅ | ✅ | ✅ | ✅ | ✅ |

### Environment Promotion Flow

```
Feature Branch → [PR + CI Checks]
      │
      ▼
    DEV ──────── (auto-deploy on merge to main)
      │
      │  Auto after DEV green
      ▼
    UAT ──────── (auto-deploy + stakeholder sign-off gate)
      │
      │  Manual approval (QA Lead + Product Owner)
      ▼
  STAGING ────── (load tests + regression + E2E)
      │
      │  Manual approval (2 approvers: Tech Lead + DevOps)
      ▼
PRODUCTION ───── (blue-green deploy + canary rollout)
      │
      │  Automated failover trigger / manual DR drill
      ▼
     DR  ──────── (warm standby, RTO 15 min, RPO 5 min)
```

---

## ☁️ Azure Infrastructure

### Infrastructure as Code (Terraform)

```
infrastructure/
├── main.tf
├── variables.tf
├── outputs.tf
├── environments/
│   ├── dev/
│   │   ├── main.tf
│   │   └── terraform.tfvars
│   ├── uat/
│   │   ├── main.tf
│   │   └── terraform.tfvars
│   ├── staging/
│   │   ├── main.tf
│   │   └── terraform.tfvars
│   ├── production/
│   │   ├── main.tf
│   │   └── terraform.tfvars
│   └── dr/
│       ├── main.tf
│       └── terraform.tfvars
└── modules/
    ├── networking/          # VNet, NSG, Private DNS
    ├── aks/                 # AKS cluster, node pools
    ├── database/            # PostgreSQL Flexible Server
    ├── cache/               # Azure Cache for Redis
    ├── storage/             # Blob Storage, Azure CDN
    ├── security/            # Key Vault, Managed Identity
    ├── apim/                # API Management
    ├── functions/           # Azure Functions App Plan
    ├── monitoring/          # Log Analytics, App Insights
    └── frontdoor/           # Azure Front Door + WAF
```

### AKS Node Pools

| Pool Name | VM SKU | Min | Max | Purpose |
|---|---|---|---|---|
| `system` | Standard_D4s_v3 | 3 | 3 | System pods, CoreDNS |
| `backend` | Standard_D8s_v3 | 5 | 50 | Spring Boot services |
| `frontend` | Standard_D4s_v3 | 2 | 20 | Angular SSR (optional) |
| `functions` | Standard_D4s_v3 | 2 | 10 | Azure Functions KEDA |

### Azure Services Summary

| Service | SKU | Purpose |
|---|---|---|
| Azure Front Door | Premium | Global load balancer, WAF, CDN |
| Azure Kubernetes Service | Standard (100 nodes max) | Container orchestration |
| Azure Database for PostgreSQL | Business Critical, 32 vCores | Primary datastore |
| Azure Cache for Redis | Premium P3 (26GB) | Session & data caching |
| Azure Blob Storage | Hot + Cool tiers | Video assets storage |
| Azure CDN | Standard Microsoft | Video segment delivery |
| Azure API Management | Premium (2 units) | API gateway |
| Azure Service Bus | Premium (1 messaging unit) | Event queuing |
| Azure Event Hubs | Premium (10 PU) | Analytics streaming |
| Azure Functions | Premium EP2 | Serverless processing |
| Azure Media Services | S3 (10 reserved units) | Video transcoding |
| Azure Key Vault | Standard | Secret management |
| Azure App Configuration | Standard | Feature flags & config |
| Azure Active Directory B2C | P2 | Identity & SSO |
| Azure Monitor | Standard | Logs & metrics |

### Azure App Configuration (Feature Flags)

```yaml
# Feature Flags (per environment)
feature.4k-streaming.enabled    = true       # prod, staging
feature.recommendations.enabled = true
feature.live-streaming.enabled  = false      # Beta — dev only

# App Settings
app.video.max-upload-size-gb    = 50
app.streaming.token-ttl-hours   = 4
app.analytics.batch-size        = 1000
app.cache.video-meta-ttl-minutes = 30
```

### Azure Key Vault (Secret Management)

> ⚠️ **NEVER commit secrets to Git.** All secrets are managed exclusively through Azure Key Vault. Managed Identity on each pod is granted `Key Vault Secrets User` role at deploy time.

| Secret Name | Description |
|---|---|
| `postgres-connection-string` | Full JDBC connection string |
| `redis-connection-string` | Redis primary connection key |
| `aad-client-secret` | Azure AD B2C client secret |
| `jwt-signing-key` | RS256 private key (PEM) |
| `azure-storage-account-key` | Blob storage key |
| `dynatrace-api-token` | Dynatrace API token |
| `service-bus-connection-string` | Azure Service Bus key |

---

## 🗄 Database Design

### PostgreSQL Schema

```sql
-- ─────────────────────────────────────────
--  USERS & AUTHENTICATION
-- ─────────────────────────────────────────
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) UNIQUE NOT NULL,
    display_name        VARCHAR(100),
    avatar_url          TEXT,
    subscription_tier   VARCHAR(20) DEFAULT 'FREE',
    oauth_provider      VARCHAR(30),
    external_id         VARCHAR(255),
    is_active           BOOLEAN DEFAULT TRUE,
    last_login_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ DEFAULT now(),
    updated_at          TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_users_email       ON users(email);
CREATE INDEX idx_users_external_id ON users(external_id, oauth_provider);

-- ─────────────────────────────────────────
--  VIDEO CATALOG
-- ─────────────────────────────────────────
CREATE TABLE videos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               VARCHAR(500) NOT NULL,
    description         TEXT,
    duration_seconds    INTEGER,
    status              VARCHAR(30) DEFAULT 'PROCESSING',
    visibility          VARCHAR(20) DEFAULT 'PUBLIC',
    thumbnail_url       TEXT,
    hls_manifest_url    TEXT,
    dash_manifest_url   TEXT,
    resolutions         JSONB,
    metadata            JSONB,
    view_count          BIGINT DEFAULT 0,
    published_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_videos_fts ON videos USING gin(
    to_tsvector('english', title || ' ' || coalesce(description, ''))
);

-- ─────────────────────────────────────────
--  STREAMING & ANALYTICS (Partitioned)
-- ─────────────────────────────────────────
CREATE TABLE watch_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    video_id        UUID REFERENCES videos(id) ON DELETE CASCADE,
    watch_duration  INTEGER,
    completion_pct  SMALLINT,
    quality         VARCHAR(10),
    device_type     VARCHAR(20),
    country_code    CHAR(2),
    started_at      TIMESTAMPTZ DEFAULT now()
) PARTITION BY RANGE (started_at);

-- Monthly partitions (auto-create via pg_partman)
CREATE TABLE watch_events_2025_01 PARTITION OF watch_events
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

-- ─────────────────────────────────────────
--  MATERIALIZED VIEWS (refreshed every 5 min)
-- ─────────────────────────────────────────
CREATE MATERIALIZED VIEW mv_trending_videos AS
SELECT
    v.id, v.title, v.thumbnail_url,
    COUNT(we.id)            AS watch_count_24h,
    SUM(we.watch_duration)  AS total_watch_seconds_24h
FROM videos v
JOIN watch_events we ON we.video_id = v.id
WHERE we.started_at > now() - INTERVAL '24 hours'
  AND v.status = 'ACTIVE'
GROUP BY v.id, v.title, v.thumbnail_url
ORDER BY watch_count_24h DESC
LIMIT 100;
CREATE UNIQUE INDEX ON mv_trending_videos(id);
```

### Connection Pooling (PgBouncer)

```ini
# pgbouncer sidecar in AKS
pool_mode         = transaction
max_client_conn   = 5000
default_pool_size = 100
min_pool_size     = 10
reserve_pool_size = 20
```

---

## 🔐 Authentication & Security

### Azure AD B2C — SSO Flow (OIDC / SAML 2.0)

```
1. User clicks "Sign In" on Angular frontend
2. Angular redirects to Azure AD B2C /authorize endpoint
3. User authenticates via Identity Provider:
   - Microsoft (work/school accounts) — OIDC
   - Google — OIDC Federation
   - GitHub — OIDC Federation
   - Custom SAML 2.0 enterprise IdP
4. AAD B2C issues id_token + access_token (JWT)
5. Angular stores tokens in memory (NOT localStorage)
6. Angular sends Bearer token on all API requests
7. Spring Security validates JWT via JWKS endpoint
8. User principal created / updated in PostgreSQL
```

### Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter()))
            )
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/content/**").hasAnyRole("CONTENT_MANAGER","ADMIN")
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

### Angular Auth Guard (MSAL)

```typescript
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  canActivate(): Observable<boolean> {
    return this.msalService.acquireTokenSilent(request).pipe(
      map(result => !!result.accessToken),
      catchError(() => { this.msalService.loginRedirect(); return of(false); })
    );
  }
}
```

---

## 👮 RBAC & IAM Rules

### Application Roles (Azure AD App Roles)

| Role | Description | Permissions |
|---|---|---|
| `ROLE_VIEWER` | Regular subscriber | Watch videos, manage own profile |
| `ROLE_CONTENT_MANAGER` | Content team | Upload, edit, publish videos |
| `ROLE_ANALYST` | Analytics team | Read-only dashboards & metrics |
| `ROLE_DEVELOPER` | Engineering | Logs, trace, non-prod envs |
| `ROLE_DEVOPS` | DevOps engineer | AKS, pipeline, infra (non-prod) |
| `ROLE_DEVOPS_PROD` | Senior DevOps | Production deployment approval |
| `ROLE_TESTER` | QA team | Test environments & test data |
| `ROLE_ADMIN` | Platform admin | Full system access |

### Azure IAM Role Assignments

```yaml
# Developers
StreamVault-Dev-Sub:   Contributor (full dev access)
StreamVault-Prod-Sub:  Reader      (read-only prod)
StreamVault-UAT-Sub:   Contributor (full UAT access)
AKS Production:        Azure Kubernetes Service Cluster User Role

# DevOps Engineers
StreamVault-Dev-Sub:     Owner
StreamVault-UAT-Sub:     Owner
StreamVault-Staging-Sub: Contributor
StreamVault-Prod-Sub:    Contributor (minus role assignment)
StreamVault-DR-Sub:      Contributor
Key Vault (Prod):        Key Vault Secrets User (read-only)

# Testers
StreamVault-Dev-Sub: Contributor (rg-streamvault-test scope only)
StreamVault-UAT-Sub: Contributor (rg-streamvault-uat scope only)

# CI/CD Service Principal
All Subscriptions: Contributor (Resource Group level)
AKS:               Azure Kubernetes Service RBAC Writer
ACR:               AcrPush
```

### Kubernetes RBAC

```yaml
# Developer — read-only prod, full dev/uat
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: developer-role
rules:
  - apiGroups: [""]
    resources: ["pods", "services", "configmaps", "events"]
    verbs: ["get", "list", "watch"]
  - apiGroups: ["apps"]
    resources: ["deployments", "replicasets"]
    verbs: ["get", "list", "watch"]
  - apiGroups: [""]
    resources: ["pods/log"]
    verbs: ["get", "list"]
---
# DevOps — full access all envs
kind: ClusterRole
metadata:
  name: devops-role
rules:
  - apiGroups: ["*"]
    resources: ["*"]
    verbs: ["*"]
---
# Tester — test & UAT namespace only
kind: Role
metadata:
  namespace: uat
  name: tester-role
rules:
  - apiGroups: [""]
    resources: ["pods", "services", "configmaps"]
    verbs: ["get", "list", "watch", "create", "delete"]
  - apiGroups: ["apps"]
    resources: ["deployments"]
    verbs: ["get", "list", "watch", "patch"]
```

---

## 🔄 CI/CD Pipeline

### Full Pipeline Flow (All Environments)

```
Developer Push → Feature Branch
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│  PR Validation Pipeline (azure-pipelines-pr.yml)                │
│  ├── Lint (ESLint / Checkstyle)                                  │
│  ├── Unit Tests (Jest / JUnit) + Coverage Gate (≥80%)           │
│  ├── SAST Scan (SonarQube — Quality Gate must pass)             │
│  ├── Dependency Vulnerability Scan (OWASP, Snyk)                │
│  └── Build Validation (no merge if fail)                        │
└─────────────────────────────────────────────────────────────────┘
       │ PR Approved → Merge to main
       ▼
┌─────────────────────────────────────────────────────────────────┐
│  Stage 1: BUILD                                                  │
│  ├── Angular: npm ci → ng build --configuration=prod            │
│  ├── Spring Boot: mvn clean package -DskipTests                 │
│  ├── Docker multi-stage build → push to Azure Container Registry│
│  └── Tag: {service}-{branch}-{commit_sha}                       │
├─────────────────────────────────────────────────────────────────┤
│  Stage 2: TEST                                                   │
│  ├── Unit Tests + Coverage Report (JUnit / Jest)                │
│  ├── Integration Tests (Testcontainers + PostgreSQL)            │
│  ├── Contract Tests (Spring Cloud Contract)                      │
│  ├── Performance Smoke Tests (k6 — 50 VUs, 2 min)              │
│  ├── NFT — Non-Functional Tests (SLAs, memory, latency)         │
│  └── Regression Test Suite                                       │
├─────────────────────────────────────────────────────────────────┤
│  Stage 3: SECURITY SCAN                                          │
│  ├── SAST: SonarQube full analysis + Quality Gate               │
│  ├── DAST: OWASP ZAP scan (automated against dev endpoint)      │
│  ├── Container Image Scan: Trivy (fail on CRITICAL/HIGH)        │
│  └── Secret Detection: GitGuardian                              │
├─────────────────────────────────────────────────────────────────┤
│  Stage 4: DEPLOY → DEV (auto on main merge)                     │
│  ├── Update GitOps repo image tag                               │
│  └── ArgoCD auto-syncs → development namespace                  │
├─────────────────────────────────────────────────────────────────┤
│  Stage 5: DEPLOY → UAT                                           │
│  ├── Auto-deploy after DEV is green (30 min soak)               │
│  ├── Smoke tests against UAT endpoint                           │
│  ├── Full regression suite                                       │
│  ├── DAST scan against UAT                                      │
│  └── Stakeholder sign-off gate (Product Owner approval)         │
├─────────────────────────────────────────────────────────────────┤
│  Stage 6: DEPLOY → STAGING                                       │
│  ├── Manual approval gate (QA Lead + DevOps)                    │
│  ├── Full E2E Tests (Playwright/Cypress)                        │
│  ├── Load Tests (k6 — 5,000 VUs, 30 min sustained)             │
│  ├── NFT Tests (throughput, latency p99, error rate SLAs)       │
│  ├── Performance baseline comparison (vs last release)          │
│  └── ArgoCD syncs staging namespace                             │
├─────────────────────────────────────────────────────────────────┤
│  Stage 7: DEPLOY → PRODUCTION                                    │
│  ├── Manual approval gate (2 approvers: Tech Lead + DevOps Lead)│
│  ├── Blue-Green deploy → green slot                             │
│  ├── Canary: 5% → 25% → 50% → 100% traffic shift              │
│  ├── Automated Dynatrace SLO monitoring during rollout          │
│  ├── Auto-rollback on SLO breach                                │
│  └── ArgoCD syncs production namespace                          │
└─────────────────────────────────────────────────────────────────┘
```

### Environment-Specific Pipeline Triggers

| Environment | Trigger | Approval | Auto-Rollback |
|---|---|---|---|
| **DEV** | Auto on merge to `main` | None | ✅ on health check fail |
| **UAT** | Auto after DEV green + 30 min soak | Product Owner | ✅ on smoke test fail |
| **STAGING** | Manual trigger | QA Lead + DevOps | ✅ on load test fail |
| **PRODUCTION** | Manual trigger | Tech Lead + DevOps Lead (2 approvals) | ✅ on SLO breach |
| **DR** | Manual trigger / Automated failover | DR Lead | ✅ on health check fail |

### azure-pipelines.yml (Excerpt)

```yaml
trigger:
  branches:
    include: [main, release/*]

variables:
  ACR_NAME:       streamvaultacr
  AKS_CLUSTER:    aks-streamvault-prod
  RESOURCE_GROUP: rg-streamvault-prod
  IMAGE_TAG:      $(Build.SourceBranchName)-$(Build.SourceVersion)

stages:
  - stage: Build
    jobs:
      - job: BuildBackend
        pool:
          vmImage: ubuntu-22.04
        steps:
          - task: JavaToolInstaller@0
            inputs:
              versionSpec: '21'
          - script: mvn clean package -DskipTests --batch-mode
          - task: Docker@2
            inputs:
              containerRegistry: acr-service-connection
              repository: streamvault/backend
              command: buildAndPush
              tags: $(IMAGE_TAG)

  - stage: SecurityScan
    dependsOn: Build
    jobs:
      - job: SAST
        steps:
          - task: SonarQubePrepare@5
          - script: mvn verify sonar:sonar
          - task: SonarQubePublish@5
            inputs:
              pollingTimeoutSec: '300'
      - job: ContainerScan
        steps:
          - script: |
              trivy image --exit-code 1 \
                --severity CRITICAL,HIGH \
                $(ACR_NAME).azurecr.io/streamvault/backend:$(IMAGE_TAG)
      - job: DAST
        steps:
          - script: |
              docker run -t owasp/zap2docker-stable zap-baseline.py \
                -t https://dev.streamvault.internal/api \
                -r zap_report.html
          - task: PublishBuildArtifacts@1
            inputs:
              pathToPublish: zap_report.html
              artifactName: dast-report

  - stage: DeployUAT
    dependsOn: [Test, SecurityScan]
    condition: succeeded()
    jobs:
      - deployment: DeployToUAT
        environment: uat
        strategy:
          runOnce:
            deploy:
              steps:
                - script: |
                    cd gitops-repo/environments/uat
                    sed -i "s/tag:.*/tag: $(IMAGE_TAG)/" values.yaml
                    git commit -am "chore: deploy $(IMAGE_TAG) to uat"
                    git push

  - stage: DeployProduction
    dependsOn: DeployStaging
    condition: succeeded()
    jobs:
      - deployment: DeployToProduction
        environment: production
        strategy:
          runOnce:
            deploy:
              steps:
                - script: ./scripts/blue-green-switch.sh $(IMAGE_TAG)
```

---

## 🔵🟢 Blue-Green Deployment

### Strategy

```
Production Traffic (100%)
         │
         ▼
  Azure Front Door
         │
    ┌────┴────┐
    │         │
 Blue Env   Green Env
(current)  (new version)
    │         │
 Active    Standby → smoke tested → traffic shifted
```

### Traffic Switching Script

```bash
# Step 1: Deploy new version to green slot
kubectl apply -f k8s/green/deployment.yaml --namespace=production-green

# Step 2: Smoke test green slot
./scripts/smoke-test.sh https://green.streamvault.internal

# Step 3: Canary — shift 5% of traffic to green
az afd origin update --origin-name green-origin --weight 50
az afd origin update --origin-name blue-origin  --weight 950

# Step 4: Monitor Dynatrace SLOs for 5 minutes
sleep 300 && ./scripts/check-slos.sh || ./scripts/rollback.sh

# Step 5: Shift 25% → 50% → 100%
az afd origin update --origin-name green-origin --weight 250
sleep 300

az afd origin update --origin-name green-origin --weight 1000
az afd origin update --origin-name blue-origin  --weight 0

# Step 6: Decommission blue (keep for 1 hour for instant rollback)
```

### Helm Blue-Green Values

```yaml
# values-blue.yaml
deployment:
  slot: blue
  replicaCount: 20
  image:
    tag: "v2.1.0"

# values-green.yaml
deployment:
  slot: green
  replicaCount: 20
  image:
    tag: "v2.2.0"
```

---

## 🔄 ArgoCD & GitOps

### Repository Structure

```
gitops-repo/
├── applications/
│   ├── backend.yaml
│   ├── frontend.yaml
│   └── functions.yaml
├── environments/
│   ├── dev/
│   │   ├── values.yaml
│   │   └── kustomization.yaml
│   ├── uat/
│   │   ├── values.yaml
│   │   └── kustomization.yaml
│   ├── staging/
│   │   ├── values.yaml
│   │   └── kustomization.yaml
│   ├── production/
│   │   ├── values-blue.yaml
│   │   ├── values-green.yaml
│   │   └── kustomization.yaml
│   └── dr/
│       ├── values.yaml
│       └── kustomization.yaml
└── base/
    └── streamvault/
```

### ArgoCD Application CRD

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: streamvault-backend
  namespace: argocd
spec:
  project: streamvault
  source:
    repoURL: https://github.com/org/streamvault-gitops
    targetRevision: HEAD
    path: environments/production
    helm:
      valueFiles:
        - values-blue.yaml
  destination:
    server: https://kubernetes.default.svc
    namespace: production
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=true
      - PrunePropagationPolicy=foreground
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
  revisionHistoryLimit: 10
```

### ArgoCD RBAC

```ini
# argocd-rbac-cm
p, role:developer, applications, get,  dev/*, allow
p, role:developer, applications, sync, dev/*, allow
p, role:developer, applications, get,  uat/*, allow
p, role:developer, applications, get,  production/*, allow
p, role:developer, applications, sync, production/*, deny

p, role:devops, applications, *, */*, allow
p, role:devops, clusters,     get, *, allow

p, role:tester, applications, get,  dev/*, allow
p, role:tester, applications, get,  uat/*, allow
p, role:tester, applications, sync, uat/*, allow

g, devops-group,    role:devops
g, developer-group, role:developer
g, tester-group,    role:tester
```

---

## ⛵ Helm Charts

### Chart Structure

```
charts/
└── streamvault/
    ├── Chart.yaml
    ├── values.yaml
    ├── values-dev.yaml
    ├── values-uat.yaml
    ├── values-staging.yaml
    ├── values-prod.yaml
    ├── values-dr.yaml
    └── templates/
        ├── deployment.yaml
        ├── service.yaml
        ├── ingress.yaml
        ├── hpa.yaml           # Horizontal Pod Autoscaler
        ├── pdb.yaml           # Pod Disruption Budget
        ├── configmap.yaml
        ├── networkpolicy.yaml
        └── serviceaccount.yaml
```

### values.yaml (Excerpt)

```yaml
global:
  imageRegistry: streamvaultacr.azurecr.io
  imageTag: latest
  environment: production

backend:
  replicaCount: 10
  resources:
    requests:
      memory: "1Gi"
      cpu: "500m"
    limits:
      memory: "2Gi"
      cpu: "2000m"
  autoscaling:
    enabled: true
    minReplicas: 10
    maxReplicas: 100
    targetCPUUtilizationPercentage: 65
    targetMemoryUtilizationPercentage: 75
  podDisruptionBudget:
    minAvailable: 5
  livenessProbe:
    httpGet:
      path: /actuator/health/liveness
      port: 8080
    initialDelaySeconds: 30
  readinessProbe:
    httpGet:
      path: /actuator/health/readiness
      port: 8080
    initialDelaySeconds: 20
```

---

## 🆘 Disaster Recovery

### DR Architecture

StreamVault implements a **Warm Standby** DR strategy across **two Azure regions** (Primary: East US, DR: West Europe), with automated failover via Azure Traffic Manager and Azure Front Door.

```
PRIMARY REGION (East US — Active)
┌────────────────────────────────────────────────────────┐
│  AKS (aks-streamvault-prod)                            │
│  PostgreSQL Flexible (Business Critical, 32 vCores)    │
│  Redis Premium P3                                      │
│  Azure Blob (Hot tier — video assets)                  │
│  Azure CDN origin                                      │
└────────────────────┬───────────────────────────────────┘
                     │  Continuous Replication
                     │  ├── PostgreSQL geo-redundant backup + read replica
                     │  ├── Redis active geo-replication
                     │  └── Blob Storage GRS (Geo-Redundant Storage)
                     ▼
DR REGION (West Europe — Warm Standby)
┌────────────────────────────────────────────────────────┐
│  AKS (aks-streamvault-dr) — scaled to 50% capacity    │
│  PostgreSQL Flexible (replica — promoted on failover)  │
│  Redis Premium P3 (geo-replica)                        │
│  Azure Blob (GRS replica — auto-synced)                │
│  Azure CDN origin (secondary)                          │
└────────────────────────────────────────────────────────┘
                     │
          Azure Front Door
     (health probes every 30 sec)
     → auto-reroutes on primary failure
```

### DR Infra Setup

```hcl
# infrastructure/environments/dr/main.tf

module "dr_networking" {
  source              = "../../modules/networking"
  location            = "westeurope"
  vnet_address_space  = ["10.10.0.0/8"]
  environment         = "dr"
}

module "dr_aks" {
  source         = "../../modules/aks"
  location       = "westeurope"
  cluster_name   = "aks-streamvault-dr"
  node_count_min = 10
  node_count_max = 80
  vm_sku         = "Standard_D8s_v3"
  environment    = "dr"
}

module "dr_database" {
  source            = "../../modules/database"
  location          = "westeurope"
  sku               = "BusinessCritical_Standard_D32ds_v4"
  ha_mode           = "ZoneRedundant"
  geo_backup        = true
  primary_server_id = module.prod_database.server_id   # geo-replica
  environment       = "dr"
}

module "dr_redis" {
  source              = "../../modules/cache"
  location            = "westeurope"
  sku                 = "Premium"
  capacity            = 3
  geo_replication     = true
  primary_cache_id    = module.prod_redis.cache_id
  environment         = "dr"
}

module "dr_storage" {
  source              = "../../modules/storage"
  location            = "westeurope"
  replication_type    = "GRS"    # Geo-Redundant Storage
  environment         = "dr"
}
```

### DR Strategy — RTO & RPO Targets

| Tier | Scenario | RTO Target | RPO Target | Strategy |
|---|---|---|---|---|
| **Tier 1** | AZ failure (zone-down) | < 2 min | 0 (zero data loss) | Zone-redundant AKS + PostgreSQL ZoneRedundant HA |
| **Tier 2** | Region outage | < 15 min | < 5 min | Warm standby DR region + automated failover |
| **Tier 3** | Data corruption / ransomware | < 4 hours | < 1 hour | PITR (Point-in-Time Restore) + geo backups |
| **Tier 4** | Full region + DR region failure | < 24 hours | < 6 hours | Backup restore from GRS storage |

### Automated Failover Flow

```
Azure Front Door Health Probe detects primary unhealthy (3 consecutive failures)
    │
    ▼
Azure Traffic Manager switches DNS → DR endpoint (TTL: 30 sec)
    │
    ▼
DR AKS cluster auto-scales from 50% → 100% capacity (KEDA + HPA triggers)
    │
    ▼
DR PostgreSQL replica promoted to primary (< 60 sec)
    │
    ▼
DR Redis geo-replica activated as primary
    │
    ▼
PagerDuty alert → On-call engineer notified
    │
    ▼
Dynatrace DR dashboard activated (separate monitoring workspace)
    │
    ▼
Incident response runbook: docs/runbooks/dr-failover.md
```

### DR Runbook Commands

```bash
# Manual DR failover (when automated failover doesn't trigger)
./scripts/dr-failover.sh --target westeurope --mode full

# Promote PostgreSQL DR replica
az postgres flexible-server replica promote \
  --resource-group rg-streamvault-dr \
  --name psql-streamvault-dr

# Scale DR AKS to full capacity
kubectl scale deployment --all --replicas=10 --namespace=dr-production

# Verify DR health
./scripts/dr-health-check.sh --env dr

# Failback to primary (after primary recovery)
./scripts/dr-failback.sh --confirm
```

### DR Drills Schedule

| Drill Type | Frequency | Duration | Owned By |
|---|---|---|---|
| Tabletop exercise | Monthly | 2 hours | DevOps Lead + Architects |
| Failover drill (simulated) | Quarterly | 4 hours | DevOps Team |
| Full DR cutover test | Bi-annually | 8 hours | All Engineering Teams |
| Chaos Engineering (AZ kill) | Monthly | 1 hour | SRE Team |
| Data restore test | Monthly | 2 hours | DBA + DevOps |

---

## 📈 Scalability

### Horizontal Pod Autoscaling (HPA)

StreamVault is designed to scale from 100 to 10 million concurrent viewers using a combination of **HPA, KEDA event-driven autoscaling, Azure VMSS node autoscaling, and CDN offloading**.

```yaml
# hpa.yaml — Backend services
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: streamvault-backend-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: streamvault-backend
  minReplicas: 10
  maxReplicas: 100
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 65
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 75
    - type: External
      external:
        metric:
          name: azure_servicebus_active_messages
          selector:
            matchLabels:
              queue: watch-events
        target:
          type: AverageValue
          averageValue: "1000"
```

### KEDA — Event-Driven Scaling (Azure Functions)

```yaml
# KEDA ScaledObject for watch-event processor
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: watch-event-scaler
spec:
  scaleTargetRef:
    name: watch-event-function
  minReplicaCount: 2
  maxReplicaCount: 50
  triggers:
    - type: azure-servicebus
      metadata:
        queueName: watch-events
        messageCount: "100"
        connectionFromEnv: SERVICE_BUS_CONNECTION_STRING
```

### AKS Cluster Autoscaler

```json
{
  "nodePoolProfiles": [
    {
      "name": "backend",
      "minCount": 5,
      "maxCount": 50,
      "enableAutoScaling": true,
      "scaleDownDelayAfterAdd": "10m",
      "scaleDownUnneededTime": "5m"
    }
  ]
}
```

### Scalability Design Principles

| Concern | Solution |
|---|---|
| **Video delivery at scale** | Azure CDN edge caching — 95%+ of video traffic never hits origin |
| **Database connection overload** | PgBouncer (5,000 client conn → 100 pool) + Read replicas for analytics |
| **Session management** | Redis cluster (no sticky sessions, stateless JWT) |
| **API rate limiting** | Azure APIM — 1,000 req/min per user, 10,000 req/min per IP |
| **Message queue backpressure** | Azure Service Bus + KEDA auto-scaling consumers |
| **DNS / Global routing** | Azure Front Door Anycast — routes to nearest healthy region |
| **Stream token issuance** | Azure Functions — 50 instances, stateless, event-driven |
| **Catalog search at scale** | PostgreSQL full-text search (GIN index) + Redis cache on hot queries |

---

## 🛡 Fault Tolerance

### Multi-Layer Resilience Design

```
Layer 1 — Network:     Azure Front Door (WAF, DDoS, global failover)
Layer 2 — Ingress:     NGINX Ingress with circuit breaker + rate limiting
Layer 3 — Application: Spring Boot Resilience4j (circuit breaker, retry, bulkhead)
Layer 4 — Messaging:   Azure Service Bus dead-letter queues + retry policies
Layer 5 — Data:        PostgreSQL HA (Zone Redundant) + read replicas
Layer 6 — Cache:       Redis clustering + geo-replication
Layer 7 — CDN:         Multi-origin Azure Front Door with automatic failover
```

### Spring Boot Resilience4j Configuration

```java
// Circuit Breaker — Video Service
@CircuitBreaker(name = "videoService", fallbackMethod = "videoServiceFallback")
@Retry(name = "videoService")
@Bulkhead(name = "videoService")
public VideoMetadata getVideoMetadata(UUID videoId) {
    return videoServiceClient.getMetadata(videoId);
}

// Fallback — serve cached data
public VideoMetadata videoServiceFallback(UUID videoId, Exception e) {
    return redisCache.get("video:meta:" + videoId)
        .orElse(VideoMetadata.placeholder());
}
```

```yaml
# application.yml — Resilience4j config
resilience4j:
  circuitbreaker:
    instances:
      videoService:
        slidingWindowSize: 20
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 5
  retry:
    instances:
      videoService:
        maxAttempts: 3
        waitDuration: 500ms
        exponentialBackoffMultiplier: 2
  bulkhead:
    instances:
      videoService:
        maxConcurrentCalls: 200
        maxWaitDuration: 1s
```

### Kubernetes Fault Tolerance Settings

```yaml
# Pod Disruption Budget — never take down more than 50%
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: streamvault-backend-pdb
spec:
  minAvailable: 5    # Always keep at least 5 pods running
  selector:
    matchLabels:
      app: streamvault-backend
---
# Topology Spread — spread pods across zones
spec:
  topologySpreadConstraints:
    - maxSkew: 1
      topologyKey: topology.kubernetes.io/zone
      whenUnsatisfiable: DoNotSchedule
      labelSelector:
        matchLabels:
          app: streamvault-backend
```

### Database Fault Tolerance

```
PostgreSQL HA Setup (Zone Redundant):
├── Primary (Zone 1)     — read/write
├── Standby (Zone 2)     — hot standby, automatic failover < 120s
└── Read Replica (Zone 3) — analytics read traffic offloading

Failover trigger: Primary unavailable for > 30 seconds
Automated promotion: Standby → Primary (no manual intervention)
Connection string: uses Azure FQDN (auto-routes post-failover)
```

---

## 📊 Monitoring & Observability

### Monitoring Coverage — All Environments

| Environment | Dynatrace Tier | Log Retention | Alerting | Dashboard |
|---|---|---|---|---|
| **DEV** | Infrastructure Monitoring | 7 days | Slack only | Dev dashboard |
| **UAT** | Full Stack Monitoring | 14 days | Slack + Email | UAT dashboard |
| **STAGING** | Full Stack + RUM | 30 days | PagerDuty (P3) | Staging dashboard |
| **PRODUCTION** | Full Stack + RUM + BizEvents | 90 days | PagerDuty (P0–P2) | Production NOC |
| **DR** | Full Stack Monitoring | 30 days | PagerDuty (P1) | DR dashboard |

### What is Monitored

| Category | Metrics |
|---|---|
| **Application Performance** | Response time (p50/p95/p99), error rate, throughput per service |
| **Streaming Quality** | Buffering ratio, bitrate switches, startup latency, stall events |
| **Infrastructure** | AKS node CPU/memory, pod restarts, PVC usage, network I/O |
| **Database** | Query latency, connection pool saturation, deadlocks, replication lag |
| **User Experience** | Real User Monitoring (RUM), Apdex score, Web Vitals (LCP/FID/CLS) |
| **Business KPIs** | Active viewers, concurrent streams, subscription conversions |
| **Security** | Failed auth attempts, WAF blocks, anomalous traffic patterns |
| **DR Health** | Replication lag, DR pod readiness, failover readiness score |

### Dynatrace OneAgent Deployment

```yaml
# charts/dynatrace/values.yaml
oneAgent:
  apiUrl: https://ENVIRONMENT_ID.live.dynatrace.com/api
  apiToken: $(DYNATRACE_API_TOKEN)
  hostGroup: streamvault-production
  networkZone: azure-eastus

dynakube:
  oneAgent:
    classicFullStack:
      tolerations:
        - effect: NoSchedule
          key: node-role.kubernetes.io/control-plane
```

### Alerting Rules

```yaml
# Production Alerting Profile
alerts:
  - name: "Streaming Error Rate > 1%"
    condition: error_rate > 1%
    window: 5m
    severity: CRITICAL
    notify: [pagerduty, teams-channel]

  - name: "API P99 Latency > 500ms"
    condition: response_time_p99 > 500ms
    window: 3m
    severity: HIGH
    notify: [pagerduty]

  - name: "Concurrent Viewers Drop > 20%"
    condition: active_streams_delta < -20%
    window: 2m
    severity: CRITICAL
    notify: [pagerduty]

  - name: "AKS Pod Restart Loop"
    condition: pod_restarts > 3
    window: 10m
    severity: HIGH
    notify: [teams-channel]

  - name: "Database Connection Pool > 90%"
    condition: db_pool_utilization > 90%
    window: 2m
    severity: CRITICAL
    notify: [pagerduty]

  - name: "DR Replication Lag > 30s"
    condition: dr_replication_lag_seconds > 30
    window: 5m
    severity: HIGH
    notify: [pagerduty, dr-team-channel]

  - name: "Redis Memory > 85%"
    condition: redis_memory_usage_pct > 85
    window: 5m
    severity: HIGH
    notify: [teams-channel]
```

### Spring Boot Metrics Export to Dynatrace

```yaml
# application.yml
management:
  dynatrace:
    metrics:
      export:
        uri: ${DYNATRACE_METRICS_INGEST_URL}
        api-token: ${DYNATRACE_API_TOKEN}
        v2:
          feature-metrics:
            enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    tags:
      application: streamvault-backend
      environment: ${SPRING_PROFILES_ACTIVE}
```

### Log Aggregation Stack

```
Application Logs (JSON structured)
    │
    ▼ Fluentd DaemonSet (AKS)
    │
    ├── Azure Log Analytics Workspace (all envs)
    │     ├── KQL queries for dashboards
    │     └── Log-based alerts
    │
    └── Dynatrace Log Management
          └── AI-powered anomaly detection (Davis AI)
```

---

## 🧪 Testing Strategy

StreamVault enforces a **comprehensive, multi-layer testing strategy** across all environments, covering unit, integration, contract, security, performance, NFT, and regression testing. All test reports are published as pipeline artifacts and gated in CI.

### Testing Pyramid

```
                    ┌─────────────────┐
                    │   E2E / UAT     │  ← Playwright / Cypress
                    │  (Staging/UAT)  │
                  ┌─┴─────────────────┴─┐
                  │  Integration Tests   │  ← Testcontainers
                  │     (DEV/CI)        │
                ┌─┴─────────────────────┴─┐
                │      Unit Tests          │  ← JUnit 5 / Jest
                │      (All Envs / CI)     │
              ┌─┴─────────────────────────┴─┐
              │   Contract Tests (CDC)       │  ← Spring Cloud Contract
              └───────────────────────────────┘
```

### Test Types & Coverage

#### 1. Unit Tests
- **Backend:** JUnit 5 + Mockito — all service & repository layers
- **Frontend:** Jest + Angular Testing Library — all components & services
- **Coverage Gate:** ≥ 80% line coverage enforced in CI (fail build if below)
- **Reports:** JUnit XML + Jacoco HTML → published as pipeline artifacts

#### 2. Integration Tests
```bash
# Testcontainers — spins up real PostgreSQL + Redis for integration tests
mvn verify -Pintegration-tests
```
- Full Spring context loaded
- Real PostgreSQL 15 container (matches production version)
- Real Redis container
- Tests cover: repository, service-to-DB, service-to-cache interactions

#### 3. Contract Tests (Consumer-Driven Contracts)
```bash
# Spring Cloud Contract — prevents breaking API changes
mvn verify -Pcontract-tests
```
- Provider-side: Spring Boot publishes contracts to Pact Broker
- Consumer-side: Angular + downstream services verify contracts
- Blocks merge if any consumer contract is broken

#### 4. SAST — Static Application Security Testing

| Tool | Scope | Gate |
|---|---|---|
| **SonarQube** | Code quality, bugs, vulnerabilities, code smells | Quality Gate must pass (0 new Critical/Blocker) |
| **OWASP Dependency Check** | Known CVEs in Maven + npm dependencies | Fail on CVSS ≥ 7.0 |
| **Snyk** | Container + dependency vulnerabilities | Fail on Critical |
| **Checkmarx** | Deep SAST for OWASP Top 10 | Fail on High+ |

```bash
# SonarQube analysis
mvn verify sonar:sonar \
  -Dsonar.projectKey=streamvault-backend \
  -Dsonar.host.url=${SONAR_URL} \
  -Dsonar.login=${SONAR_TOKEN}

# Publish Sonar report
# Reports: sonar-report.html → pipeline artifacts
```

#### 5. DAST — Dynamic Application Security Testing

```bash
# OWASP ZAP — automated DAST scan against running endpoints
docker run -t owasp/zap2docker-stable \
  zap-full-scan.py \
  -t https://uat.streamvault.com \
  -r zap_report.html \
  -w zap_report.md \
  --hook=/zap/auth_hook.py   # handles JWT auth

# DAST runs against: DEV (every build), UAT (every deploy), STAGING (every deploy)
# Reports published: zap_report.html → pipeline artifacts + email to security team
```

#### 6. Performance Testing

| Test | Tool | Environment | Trigger | Threshold |
|---|---|---|---|---|
| **Smoke** | k6 | DEV (every build) | Auto on CI | < 200ms p95, 0% errors |
| **Load** | k6 | STAGING | Pre-production deploy | < 500ms p99, < 0.1% errors at 5,000 VUs |
| **Stress** | k6 | STAGING | Monthly | System recovers gracefully at 150% load |
| **Soak** | k6 | STAGING | Before major release | No memory leaks over 4-hour run |
| **Spike** | k6 | STAGING | Quarterly | Handles 10x sudden traffic spike |

```javascript
// k6 load test — scripts/load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '5m',  target: 1000  },  // ramp-up
    { duration: '20m', target: 5000  },  // sustained load
    { duration: '5m',  target: 10000 },  // peak
    { duration: '5m',  target: 0     },  // ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(99)<500'],   // 99th percentile < 500ms
    http_req_failed:   ['rate<0.001'],  // error rate < 0.1%
  },
};

export default function () {
  const res = http.get('https://staging.streamvault.com/api/v1/videos', {
    headers: { Authorization: `Bearer ${__ENV.TEST_TOKEN}` },
  });
  check(res, {
    'status is 200':     (r) => r.status === 200,
    'response < 500ms':  (r) => r.timings.duration < 500,
  });
  sleep(1);
}
```

#### 7. NFT — Non-Functional Testing

NFT verifies that the platform meets **SLA and NFR targets** before any production deploy.

| NFR | Target | Test Method | Environment |
|---|---|---|---|
| **API Throughput** | ≥ 50,000 req/sec | k6 ramp test | STAGING |
| **API Latency p99** | < 500ms | k6 + Dynatrace | STAGING/PROD |
| **Video Startup Time** | < 3 seconds | Synthetic monitoring | UAT/STAGING/PROD |
| **Buffering Ratio** | < 0.5% | Real User Monitoring | PROD |
| **Availability** | ≥ 99.95% | Azure Monitor uptime | PROD |
| **Max Concurrent Streams** | 10 million | Capacity model + load test | STAGING |
| **DB Query p95** | < 100ms | pg_stat_statements | All envs |
| **Memory Leak** | 0 after 4hr soak | JVM heap + k6 soak | STAGING |
| **Pod Restart Rate** | 0 in 24hr window | Kubernetes metrics | All envs |
| **CDN Cache Hit Rate** | ≥ 90% | Azure Front Door metrics | PROD |

```bash
# NFT reporting
./scripts/nft-report.sh \
  --env staging \
  --duration 4h \
  --report-format html \
  --output nft-report.html

# Published to pipeline artifacts and Confluence
```

#### 8. Regression Testing

```bash
# Full regression suite — runs on every UAT and STAGING deploy
npx playwright test \
  --config=playwright.config.ts \
  --project=regression \
  --reporter=html

# Regression scope covers:
# - All critical user journeys (sign-in, browse, stream, subscribe)
# - All API endpoints (contract + response validation)
# - Cross-browser: Chrome, Firefox, Safari, Edge
# - Mobile viewports: iOS Safari, Android Chrome
# - Reports: playwright-report/ → pipeline artifacts + Slack notification
```

### Test Reports & Artifacts

All test reports are published as **Azure DevOps pipeline artifacts** and retained for 30 days:

| Report | File | Published When |
|---|---|---|
| Unit test results | `junit-results.xml` | Every build |
| Code coverage | `jacoco/index.html` | Every build |
| SonarQube report | `sonar-report.html` | Every build |
| OWASP ZAP DAST | `zap_report.html` | Every env deploy |
| Trivy container scan | `trivy-report.json` | Every build |
| k6 performance | `k6-results.html` | Staging deploy |
| NFT report | `nft-report.html` | Staging + major releases |
| Playwright regression | `playwright-report/index.html` | UAT + Staging deploy |

---

## ⚡ App Availability & SLA

### Availability Targets

| Environment | Availability SLA | Measurement Window | Max Downtime/Month |
|---|---|---|---|
| **PRODUCTION** | 99.95% | Monthly | 21.9 minutes |
| **DR** | 99.9% | Monthly | 43.8 minutes |
| **STAGING** | 99.5% | Monthly | 3.65 hours |
| **UAT** | 99.0% | Monthly | 7.3 hours |
| **DEV** | Best effort | — | — |

### Availability Architecture

```
99.95% Production Availability achieved via:

├── Azure Front Door (99.99% SLA) — global anycast, multi-region
├── AKS Zone-Redundant (3 AZs) — no single AZ failure brings down service
├── PostgreSQL Zone-Redundant HA — automatic failover < 120 sec
├── Redis Geo-Replication — zero data loss on zone failure
├── PodDisruptionBudget — rolling updates never kill more than 50% pods
├── Health checks (liveness + readiness probes) — auto pod replacement
├── Blue-Green + Canary — zero-downtime deployments
└── DR warm standby — RTO 15 min for full region outage
```

### Health Check Endpoints

```
GET /actuator/health/liveness   → JVM alive? (restart if fails)
GET /actuator/health/readiness  → Dependencies ready? (remove from LB if fails)
GET /actuator/health            → Full health (DB, Redis, Storage, downstream)
GET /actuator/info              → Version, build info, environment
```

### SLO Dashboard (Dynatrace)

| SLO | Target | Measurement |
|---|---|---|
| API Availability | 99.95% | Synthetic + RUM |
| API Latency p99 | < 500ms | Dynatrace APM |
| Streaming Availability | 99.95% | Synthetic checks every 60s |
| Video Startup Time | < 3s | RUM — client-side metric |
| Error Budget (monthly) | 21.9 min | Burn rate alert at 50% consumed |

### Incident Response SLA

| Severity | Condition | Response Time | Resolution Time | Channel |
|---|---|---|---|---|
| **P0** | Service completely down | 15 min | 1 hour | PagerDuty → On-call |
| **P1** | Major feature degraded (>20% users affected) | 30 min | 4 hours | PagerDuty → Team Lead |
| **P2** | Minor degradation / single region | 2 hours | 8 hours | Teams #incidents |
| **P3** | Enhancement / cosmetic | Next sprint | Next sprint | JIRA board |

---

## 📁 Project Structure

```
streamvault/
├── README.md
├── .github/
│   └── CODEOWNERS
├── docs/
│   ├── architecture/
│   │   ├── HLD.md
│   │   ├── LLD.md
│   │   └── diagrams/
│   ├── api/
│   │   └── openapi.yaml
│   └── runbooks/
│       ├── incident-response.md
│       ├── dr-failover.md
│       └── on-call.md
│
├── frontend/                          # Angular 17+ Application
│   ├── src/app/
│   │   ├── core/
│   │   │   ├── auth/                  # MSAL, guards, interceptors
│   │   │   └── interceptors/          # JWT, error, retry
│   │   ├── features/
│   │   │   ├── video-player/          # HLS.js player
│   │   │   ├── catalog/               # Browse & search
│   │   │   └── admin/                 # Admin panel
│   │   └── shared/
│   ├── Dockerfile
│   └── package.json
│
├── backend/                           # Spring Boot Microservices
│   ├── user-service/
│   ├── video-service/
│   ├── streaming-service/
│   ├── analytics-service/
│   ├── notification-service/
│   └── api-gateway/                   # Spring Cloud Gateway
│
├── functions/                         # Azure Functions
│   ├── video-encoder-function/
│   ├── stream-token-function/
│   ├── watch-event-function/
│   └── recommendation-refresh-function/
│
├── infrastructure/                    # Terraform IaC
│   ├── environments/
│   │   ├── dev/
│   │   ├── uat/
│   │   ├── staging/
│   │   ├── production/
│   │   └── dr/
│   └── modules/
│
├── charts/                            # Helm Charts
│   └── streamvault/
│       ├── values-dev.yaml
│       ├── values-uat.yaml
│       ├── values-staging.yaml
│       ├── values-prod.yaml
│       └── values-dr.yaml
│
├── gitops/                            # ArgoCD GitOps
│   ├── applications/
│   └── environments/
│       ├── dev/ uat/ staging/ production/ dr/
│
├── pipelines/                         # Azure DevOps Pipelines
│   ├── azure-pipelines.yml
│   ├── azure-pipelines-pr.yml
│   └── templates/
│       ├── build-backend.yml
│       ├── build-frontend.yml
│       ├── security-scan.yml
│       ├── test.yml
│       ├── performance-test.yml
│       └── deploy.yml
│
└── scripts/
    ├── blue-green-switch.sh
    ├── dr-failover.sh
    ├── dr-failback.sh
    ├── dr-health-check.sh
    ├── smoke-test.sh
    ├── nft-report.sh
    ├── db-migration.sh
    └── load-test.js
```

---

## 🚀 Getting Started

### Prerequisites

```bash
az --version          # Azure CLI 2.55+
kubectl version       # 1.28+
helm version          # 3.13+
java --version        # Java 21
node --version        # Node 20+
terraform --version   # 1.6+
argocd version        # 2.9+
k6 version            # 0.47+
```

### 1. Clone & Setup

```bash
git clone https://github.com/your-org/streamvault.git
cd streamvault

# Login to Azure
az login
az account set --subscription "StreamVault-Dev"

# Connect to AKS (dev)
az aks get-credentials \
  --resource-group rg-streamvault-dev \
  --name aks-streamvault-dev
```

### 2. Provision Infrastructure

```bash
# Dev environment
cd infrastructure/environments/dev
terraform init
terraform plan -out=tfplan
terraform apply tfplan

# DR environment (separate subscription)
cd infrastructure/environments/dr
az account set --subscription "StreamVault-DR-Sub"
terraform init
terraform plan -out=tfplan-dr
terraform apply tfplan-dr
```

### 3. Deploy via Helm

```bash
# Install backend (dev)
helm upgrade --install streamvault-backend ./charts/streamvault \
  --namespace development \
  --create-namespace \
  --values charts/streamvault/values-dev.yaml \
  --set backend.image.tag=$(git rev-parse --short HEAD)

# Install backend (UAT)
helm upgrade --install streamvault-backend ./charts/streamvault \
  --namespace uat \
  --values charts/streamvault/values-uat.yaml \
  --set backend.image.tag=$(git rev-parse --short HEAD)
```

### 4. Run Locally

```bash
# Backend
cd backend/user-service
export SPRING_PROFILES_ACTIVE=local
export AZURE_KEYVAULT_URI=https://streamvault-dev-kv.vault.azure.net/
mvn spring-boot:run

# Frontend
cd frontend
npm ci
ng serve --proxy-config proxy.conf.json
```

### 5. Run Tests

```bash
# Unit tests
cd backend/user-service && mvn test
cd frontend && npm test

# Integration tests (requires Docker)
mvn verify -Pintegration-tests

# E2E tests
cd frontend && npx playwright test

# Performance / load test
k6 run scripts/load-test.js --vus 100 --duration 60s

# Security — SAST
mvn verify sonar:sonar -Dsonar.login=${SONAR_TOKEN}

# Security — Container scan
trivy image streamvaultacr.azurecr.io/streamvault/backend:latest
```

---

## 🔧 Environment Variables

> All secrets are stored in **Azure Key Vault**. Non-secret config in **Azure App Configuration**.

| Variable | Source | Description |
|---|---|---|
| `AZURE_KEYVAULT_URI` | Deploy config | Key Vault endpoint |
| `AZURE_APP_CONFIG_CONNECTION_STRING` | Deploy config | App Configuration endpoint |
| `SPRING_PROFILES_ACTIVE` | Deploy config | `dev`, `uat`, `staging`, `prod`, `dr` |
| `postgres-connection-string` | Key Vault | PostgreSQL JDBC URL |
| `redis-connection-string` | Key Vault | Redis connection string |
| `aad-client-secret` | Key Vault | Azure AD B2C client secret |
| `jwt-signing-key` | Key Vault | RS256 PEM private key |
| `azure-storage-account-key` | Key Vault | Blob storage key |
| `dynatrace-api-token` | Key Vault | Dynatrace API token |
| `service-bus-connection-string` | Key Vault | Azure Service Bus key |

> ⚠️ **Never commit secrets to Git.** Managed Identity on each pod is granted `Key Vault Secrets User` role at deployment time.

---

## 📖 API Documentation

OpenAPI 3.0 spec: `docs/api/openapi.yaml`

| Environment | Swagger UI |
|---|---|
| DEV | `https://api-dev.streamvault.internal/swagger-ui.html` |
| UAT | `https://api-uat.streamvault.com/swagger-ui.html` |
| STAGING | `https://api-staging.streamvault.com/swagger-ui.html` |
| PRODUCTION | Internal only (APIM Developer Portal) |

### Core Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/token` | None | Exchange SSO code for JWT |
| `GET` | `/api/v1/videos` | Bearer | List/search video catalog |
| `GET` | `/api/v1/videos/{id}` | Bearer | Get video metadata |
| `POST` | `/api/v1/stream/{videoId}/token` | Bearer | Get streaming token (SAS) |
| `POST` | `/api/v1/watch-events` | Bearer | Submit watch event |
| `GET` | `/api/v1/users/me` | Bearer | Get current user profile |
| `GET` | `/api/v1/analytics/trending` | Bearer | Trending videos (cached) |
| `POST` | `/api/v1/admin/videos` | Bearer + ADMIN | Upload new video |

---

## 🤝 Contributing

### Branching Strategy (GitFlow)

```
main          ← Production-ready code
release/*     ← Release candidates
develop       ← Integration branch
feature/*     ← New features
bugfix/*      ← Bug fixes
hotfix/*      ← Emergency production patches
```

### Pull Request Requirements

- All CI checks must pass (build, test, security scan)
- Minimum **2 reviewer approvals**
- Code coverage must not drop below **80%**
- No new Critical/High **SonarQube findings**
- Security scan must pass (Trivy, Snyk, OWASP)
- PR description must reference a **JIRA ticket**
- DAST scan must produce no new High/Critical findings

### Commit Message Format

```
feat(streaming): add adaptive bitrate switching for 4K
fix(auth): resolve token refresh race condition
chore(deps): upgrade Spring Boot to 3.2.1
docs(api): update OpenAPI spec for /stream endpoint
test(video): add NFT test for concurrent streaming load
infra(dr): add DR terraform module for West Europe region
```

---

## 📞 Support & On-Call

| Severity | Condition | Response SLA | Channel |
|---|---|---|---|
| **P0 — Service Down** | Production unavailable | 15 min | PagerDuty → On-call engineer |
| **P1 — Major Degradation** | >20% users affected | 30 min | PagerDuty → Team Lead |
| **P2 — Minor Issue** | Single feature degraded | 4 hours | Teams #incidents |
| **P3 — Enhancement** | Non-urgent | Next sprint | JIRA board |

- 📚 Runbooks: `docs/runbooks/`
- 🌐 Status Page: `https://status.streamvault.com`
- 📟 On-Call Rotation: PagerDuty — `streamvault-oncall` schedule
- 🔥 DR Failover Runbook: `docs/runbooks/dr-failover.md`

---

<div align="center">

**Last updated: 2025 | Maintained by the StreamVault Platform Engineering Team**

📝 Proprietary — All rights reserved © 2025 StreamVault Inc.

</div>
