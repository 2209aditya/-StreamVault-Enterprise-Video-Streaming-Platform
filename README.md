# 🎬 StreamVault — Enterprise Video Streaming Platform
### Scalable to 10 Million Concurrent Viewers | Azure Cloud Native | Angular + Spring Boot + PostgreSQL

---

## 📋 Table of Contents

1. [Project Overview](#-project-overview)
2. [Architecture Overview](#-architecture-overview)
3. [High-Level Design (HLD)](#-high-level-design-hld)
4. [Low-Level Design (LLD)](#-low-level-design-lld)
5. [Azure Infrastructure](#-azure-infrastructure)
6. [Database Design](#-database-design)
7. [Authentication & SSO](#-authentication--sso)
8. [RBAC & IAM Rules](#-rbac--iam-rules)
9. [CI/CD Pipeline](#-cicd-pipeline)
10. [Blue-Green Deployment](#-blue-green-deployment)
11. [ArgoCD & GitOps](#-argocd--gitops)
12. [Helm Charts](#-helm-charts)
13. [Dynatrace Monitoring](#-dynatrace-monitoring)
14. [Project Structure](#-project-structure)
15. [Getting Started](#-getting-started)
16. [Environment Variables](#-environment-variables)
17. [API Documentation](#-api-documentation)
18. [Contributing](#-contributing)

---

## 🚀 Project Overview

**StreamVault** is a cloud-native, enterprise-grade video streaming platform engineered to handle **10 million concurrent viewers**. Built on Microsoft Azure, it leverages modern microservices architecture, CDN-backed video delivery, real-time analytics, and enterprise-grade security.

| Attribute | Details |
|-----------|---------|
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
|---------|-----------|---------|
| **API Gateway** | Azure API Management + NGINX Ingress | Rate limiting, routing, throttling |
| **Auth Service** | Azure AD B2C + Spring Security | SSO, JWT, OAuth 2.0 |
| **User Service** | Spring Boot | User profiles, subscriptions, preferences |
| **Video Service** | Spring Boot | Video metadata, catalog, search |
| **Streaming Service** | Spring Boot + Azure Media Services | HLS/DASH adaptive bitrate |
| **Encoder Service** | Azure Media Services | Transcoding to multiple resolutions |
| **Analytics Service** | Spring Boot + Azure Stream Analytics | Real-time viewing metrics |
| **Notification Service** | Spring Boot + Azure Communication Services | Email, push, SMS |
| **Recommendation Service** | Spring Boot + Azure ML | AI-powered content recommendations |

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
|------------|---------|-----|
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
// Streaming Token Generation (Azure Function trigger on stream request)
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
|--------------|---------|---------|
| `VideoEncoderFunction` | Blob trigger (upload) | Kick off encoding job |
| `StreamTokenFunction` | HTTP trigger | Generate short-lived SAS streaming URL |
| `WatchEventFunction` | Service Bus trigger | Process watch events → PostgreSQL |
| `ThumbnailGenFunction` | Blob trigger | Auto-generate video thumbnails |
| `PurgeExpiredTokensFunction` | Timer trigger (hourly) | Clean up expired stream tokens |
| `UsageMetricsFunction` | Timer trigger (5 min) | Aggregate viewer metrics to Redis |
| `RecommendationRefreshFunction` | Timer trigger (daily) | Refresh ML recommendations |

### 5. Azure App Configuration

All non-secret configuration is stored in **Azure App Configuration** with feature flags:

```
# Feature Flags
feature.4k-streaming.enabled = true
feature.recommendations.enabled = true
feature.live-streaming.enabled = false    # Beta

# App Settings
app.video.max-upload-size-gb = 50
app.streaming.token-ttl-hours = 4
app.analytics.batch-size = 1000
app.cache.video-meta-ttl-minutes = 30
```

Spring Boot integration:
```yaml
# bootstrap.yml
spring:
  cloud:
    azure:
      appconfiguration:
        stores:
          - connection-string: ${AZURE_APP_CONFIG_CONNECTION_STRING}
            selects:
              - key-filter: /streamvault/${spring.profiles.active}/*
            monitoring:
              enabled: true
              refresh-interval: 30s
```

### 6. Azure Key Vault

All secrets are stored in **Azure Key Vault** — never in environment variables or config files:

| Secret Name | Description |
|------------|-------------|
| `postgres-connection-string` | Full JDBC connection string |
| `redis-connection-string` | Redis primary connection key |
| `aad-client-secret` | Azure AD B2C client secret |
| `jwt-signing-key` | RS256 private key (PEM) |
| `azure-storage-account-key` | Blob storage key |
| `dynatrace-api-token` | Dynatrace API token |
| `service-bus-connection-string` | Azure Service Bus key |

Spring Boot integration:
```yaml
spring:
  cloud:
    azure:
      keyvault:
        secret:
          property-source-enabled: true
          endpoint: https://streamvault-kv.vault.azure.net/
```

---

## ☁️ Azure Infrastructure

### Infrastructure as Code (Bicep/Terraform)

```
infrastructure/
├── main.tf
├── variables.tf
├── outputs.tf
├── modules/
│   ├── networking/          # VNet, NSG, Private DNS
│   ├── aks/                 # AKS cluster, node pools
│   ├── database/            # PostgreSQL Flexible Server
│   ├── cache/               # Azure Cache for Redis (Premium P3)
│   ├── storage/             # Blob Storage, Azure CDN
│   ├── security/            # Key Vault, Managed Identity
│   ├── apim/                # API Management
│   ├── functions/           # Azure Functions App Plan
│   ├── monitoring/          # Log Analytics, App Insights
│   └── frontdoor/           # Azure Front Door + WAF
```

### AKS Node Pools

| Pool Name | VM SKU | Min | Max | Purpose |
|-----------|--------|-----|-----|---------|
| `system` | Standard_D4s_v3 | 3 | 3 | System pods, CoreDNS |
| `backend` | Standard_D8s_v3 | 5 | 50 | Spring Boot services |
| `frontend` | Standard_D4s_v3 | 2 | 20 | Angular SSR (optional) |
| `functions` | Standard_D4s_v3 | 2 | 10 | Azure Functions KEDA |

### Azure Services Summary

| Service | SKU | Purpose |
|---------|-----|---------|
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
    subscription_tier   VARCHAR(20) DEFAULT 'FREE',        -- FREE|BASIC|PREMIUM|ENTERPRISE
    oauth_provider      VARCHAR(30),                        -- AAD|GOOGLE|GITHUB
    external_id         VARCHAR(255),
    is_active           BOOLEAN DEFAULT TRUE,
    last_login_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ DEFAULT now(),
    updated_at          TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_users_email         ON users(email);
CREATE INDEX idx_users_external_id   ON users(external_id, oauth_provider);

CREATE TABLE user_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    refresh_token   TEXT,
    device_info     JSONB,
    ip_address      INET,
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_sessions_user_id    ON user_sessions(user_id);
CREATE INDEX idx_sessions_expires_at ON user_sessions(expires_at);

-- ─────────────────────────────────────────
--  VIDEO CATALOG
-- ─────────────────────────────────────────
CREATE TABLE videos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               VARCHAR(500) NOT NULL,
    description         TEXT,
    duration_seconds    INTEGER,
    status              VARCHAR(30) DEFAULT 'PROCESSING',  -- PROCESSING|ACTIVE|INACTIVE|DELETED
    visibility          VARCHAR(20) DEFAULT 'PUBLIC',      -- PUBLIC|PRIVATE|UNLISTED
    thumbnail_url       TEXT,
    blob_path           TEXT,
    hls_manifest_url    TEXT,
    dash_manifest_url   TEXT,
    resolutions         JSONB,      -- ["360p","480p","720p","1080p","4k"]
    metadata            JSONB,      -- director, cast, tags, language, etc.
    view_count          BIGINT DEFAULT 0,
    like_count          INTEGER DEFAULT 0,
    published_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ DEFAULT now(),
    updated_at          TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_videos_status       ON videos(status);
CREATE INDEX idx_videos_published_at ON videos(published_at DESC);
CREATE INDEX idx_videos_view_count   ON videos(view_count DESC);
CREATE INDEX idx_videos_fts          ON videos USING gin(to_tsvector('english', title || ' ' || coalesce(description, '')));

CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    slug        VARCHAR(100) UNIQUE NOT NULL,
    parent_id   UUID REFERENCES categories(id)
);

CREATE TABLE video_categories (
    video_id    UUID REFERENCES videos(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (video_id, category_id)
);

-- ─────────────────────────────────────────
--  STREAMING & ANALYTICS
-- ─────────────────────────────────────────
CREATE TABLE watch_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    video_id        UUID REFERENCES videos(id) ON DELETE CASCADE,
    session_id      UUID,
    watch_duration  INTEGER,    -- seconds watched
    total_duration  INTEGER,    -- video total seconds
    completion_pct  SMALLINT,   -- 0-100
    quality         VARCHAR(10),
    device_type     VARCHAR(20),
    country_code    CHAR(2),
    client_ip       INET,
    started_at      TIMESTAMPTZ DEFAULT now(),
    ended_at        TIMESTAMPTZ
) PARTITION BY RANGE (started_at);

-- Monthly partitions (auto-create via pg_partman)
CREATE TABLE watch_events_2025_01 PARTITION OF watch_events
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE INDEX idx_watch_events_user_id  ON watch_events(user_id, started_at DESC);
CREATE INDEX idx_watch_events_video_id ON watch_events(video_id, started_at DESC);

CREATE TABLE stream_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    video_id        UUID REFERENCES videos(id) ON DELETE CASCADE,
    token_hash      VARCHAR(64) UNIQUE NOT NULL,
    sas_url         TEXT NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_stream_tokens_hash       ON stream_tokens(token_hash);
CREATE INDEX idx_stream_tokens_expires_at ON stream_tokens(expires_at);

-- ─────────────────────────────────────────
--  SUBSCRIPTIONS & BILLING
-- ─────────────────────────────────────────
CREATE TABLE subscription_plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(50) UNIQUE NOT NULL,
    tier            VARCHAR(20) NOT NULL,
    price_monthly   NUMERIC(10,2),
    max_streams     SMALLINT,
    max_resolution  VARCHAR(10),
    features        JSONB
);

CREATE TABLE user_subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    plan_id         UUID REFERENCES subscription_plans(id),
    status          VARCHAR(20) DEFAULT 'ACTIVE',   -- ACTIVE|CANCELLED|EXPIRED|TRIAL
    started_at      TIMESTAMPTZ DEFAULT now(),
    expires_at      TIMESTAMPTZ,
    payment_ref     VARCHAR(255)
);
CREATE INDEX idx_subscriptions_user_id   ON user_subscriptions(user_id, status);
CREATE INDEX idx_subscriptions_expires   ON user_subscriptions(expires_at);

-- ─────────────────────────────────────────
--  MATERIALIZED VIEWS (refreshed every 5 min)
-- ─────────────────────────────────────────
CREATE MATERIALIZED VIEW mv_trending_videos AS
SELECT
    v.id,
    v.title,
    v.thumbnail_url,
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

### Database Connection Pooling (PgBouncer)

```yaml
# pgbouncer config (sidecar in AKS)
pool_mode: transaction
max_client_conn: 5000
default_pool_size: 100
min_pool_size: 10
reserve_pool_size: 20
```

---

## 🔐 Authentication & SSO

### Azure AD B2C Integration (OIDC / SAML 2.0)

```
SSO Flow:
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

**Spring Security Configuration:**
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

**Angular Auth Guard:**
```typescript
// auth.guard.ts — Uses MSAL Angular
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

| Role | Description | Can Do |
|------|-------------|--------|
| `ROLE_VIEWER` | Regular subscriber | Watch videos, manage own profile |
| `ROLE_CONTENT_MANAGER` | Content team | Upload, edit, publish videos |
| `ROLE_ANALYST` | Analytics team | Read-only access to dashboards & metrics |
| `ROLE_DEVELOPER` | Engineering team | Access to logs, trace data, non-prod envs |
| `ROLE_DEVOPS` | DevOps engineer | AKS, pipeline, infra access (non-prod) |
| `ROLE_DEVOPS_PROD` | Senior DevOps | Production deployment approval |
| `ROLE_TESTER` | QA team | Test environments, test data management |
| `ROLE_ADMIN` | Platform admin | Full system access |

### Azure IAM Role Assignments

```
# Developers
Subscription: StreamVault-Dev-Sub
  - Role: Contributor
  - Scope: /subscriptions/{dev-sub-id}

Subscription: StreamVault-Prod-Sub
  - Role: Reader
  - Scope: /subscriptions/{prod-sub-id}
  
AKS Cluster (Production):
  - Role: Azure Kubernetes Service Cluster User Role
  - Scope: /subscriptions/{prod}/resourceGroups/rg-streamvault-prod/providers/Microsoft.ContainerService/managedClusters/aks-streamvault-prod

# DevOps Engineers  
Subscription: StreamVault-Dev-Sub
  - Role: Owner
  - Scope: /subscriptions/{dev-sub-id}

Subscription: StreamVault-Prod-Sub
  - Role: Contributor (minus role assignment)
  - Scope: /subscriptions/{prod-sub-id}
  
Key Vault (Production):
  - Role: Key Vault Secrets User (read only)
  - Scope: /subscriptions/{prod}/resourceGroups/.../vaults/streamvault-kv

# Testers
Subscription: StreamVault-Dev-Sub
  - Role: Contributor
  - Scope: /subscriptions/{dev-sub-id}/resourceGroups/rg-streamvault-test

# CI/CD Service Principal
Subscription: Both
  - Role: Contributor
  - Scope: Resource Group level (not subscription)
  
AKS:
  - Role: Azure Kubernetes Service RBAC Writer
  
ACR:
  - Role: AcrPush
```

### Kubernetes RBAC

```yaml
# Developer ClusterRole — read-only prod, full dev
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
# DevOps ClusterRole — full prod access
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: devops-role
rules:
  - apiGroups: ["*"]
    resources: ["*"]
    verbs: ["*"]
---
# Tester Role — test namespace only
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: test
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

### Azure DevOps Pipeline (Full Flow)

```
Developer Push → Feature Branch
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│  PR Validation Pipeline (azure-pipelines-pr.yml)         │
│  ├── Lint (ESLint / Checkstyle)                          │
│  ├── Unit Tests (Jest / JUnit)                           │
│  ├── Code Coverage Gate (≥80%)                           │
│  ├── SAST Scan (SonarQube)                               │
│  ├── Dependency Vulnerability Scan (OWASP, Snyk)         │
│  └── Build Validation (no merge if fail)                 │
└─────────────────────────────────────────────────────────┘
       │ PR Approved & Merged to main
       ▼
┌─────────────────────────────────────────────────────────┐
│  Main CI Pipeline (azure-pipelines.yml)                  │
│  Stage 1: BUILD                                          │
│  ├── Angular: npm ci → ng build --configuration=prod     │
│  ├── Spring Boot: mvn clean package -DskipTests          │
│  ├── Docker Build (multi-stage Dockerfile)               │
│  ├── Docker Push → Azure Container Registry (ACR)        │
│  └── Tag: {service}-{branch}-{commit_sha}                │
│                                                          │
│  Stage 2: TEST                                           │
│  ├── Unit Tests + Coverage Report                        │
│  ├── Integration Tests (Testcontainers + PostgreSQL)     │
│  ├── Contract Tests (Spring Cloud Contract)              │
│  └── Performance Smoke Tests (k6)                        │
│                                                          │
│  Stage 3: SECURITY SCAN                                  │
│  ├── Container Image Scan (Trivy)                        │
│  ├── DAST Scan (OWASP ZAP)                               │
│  └── Secret Detection (GitGuardian)                      │
│                                                          │
│  Stage 4: DEPLOY → DEV                                   │
│  ├── Update Helm values (image tag)                      │
│  ├── Commit to GitOps repo                               │
│  └── ArgoCD auto-syncs dev namespace                     │
│                                                          │
│  Stage 5: DEPLOY → STAGING                               │
│  ├── Approval gate (auto for staging)                    │
│  ├── E2E Tests (Playwright/Cypress)                      │
│  ├── Load Tests (k6 — 1000 VUs)                          │
│  └── ArgoCD syncs staging namespace                      │
│                                                          │
│  Stage 6: DEPLOY → PRODUCTION                            │
│  ├── Manual approval gate (2 approvers)                  │
│  ├── Blue-Green switch                                   │
│  └── ArgoCD syncs production namespace                   │
└─────────────────────────────────────────────────────────┘
```

### azure-pipelines.yml (Excerpt)

```yaml
trigger:
  branches:
    include: [main, release/*]

variables:
  ACR_NAME: streamvaultacr
  AKS_CLUSTER: aks-streamvault-prod
  RESOURCE_GROUP: rg-streamvault-prod
  IMAGE_TAG: $(Build.SourceBranchName)-$(Build.SourceVersion)

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
            displayName: 'Maven Build'
          - task: Docker@2
            inputs:
              containerRegistry: acr-service-connection
              repository: streamvault/backend
              command: buildAndPush
              tags: $(IMAGE_TAG)

      - job: BuildFrontend
        pool:
          vmImage: ubuntu-22.04
        steps:
          - task: NodeTool@0
            inputs:
              versionSpec: '20.x'
          - script: |
              npm ci
              npx ng build --configuration=production
            displayName: 'Angular Build'
          - task: Docker@2
            inputs:
              containerRegistry: acr-service-connection
              repository: streamvault/frontend
              command: buildAndPush
              tags: $(IMAGE_TAG)

  - stage: Test
    dependsOn: Build
    jobs:
      - job: IntegrationTests
        steps:
          - script: mvn verify -Pintegration-tests
            displayName: 'Integration Tests (Testcontainers)'

  - stage: DeployDev
    dependsOn: Test
    condition: and(succeeded(), eq(variables['Build.SourceBranch'], 'refs/heads/main'))
    jobs:
      - deployment: DeployToDev
        environment: development
        strategy:
          runOnce:
            deploy:
              steps:
                - script: |
                    cd gitops-repo/environments/dev
                    sed -i "s/tag:.*/tag: $(IMAGE_TAG)/" values.yaml
                    git commit -am "chore: update image tag to $(IMAGE_TAG)"
                    git push
                  displayName: 'Update GitOps Repo (ArgoCD picks up)'
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
 Active    Standby (smoke tested)
```

### Switching Traffic

```bash
# Step 1: Deploy new version to green slot
kubectl apply -f k8s/green/deployment.yaml --namespace=production-green

# Step 2: Run smoke tests against green
./scripts/smoke-test.sh https://green.streamvault.internal

# Step 3: Switch Front Door origin to green (zero downtime)
az afd origin update \
  --resource-group rg-streamvault-prod \
  --profile-name streamvault-fd \
  --origin-group-name backend-origins \
  --origin-name blue-origin \
  --weight 0

az afd origin update \
  --origin-name green-origin \
  --weight 1000

# Step 4: Monitor for 15 minutes (Dynatrace auto-alert)
# Step 5: If healthy — decommission blue
# Step 5: If issues — instant rollback (re-weight blue to 1000)
```

### Helm Blue-Green Values

```yaml
# values-blue.yaml
deployment:
  slot: blue
  replicaCount: 20
  image:
    tag: "v2.1.0"
  service:
    selector:
      slot: blue

# values-green.yaml
deployment:
  slot: green
  replicaCount: 20
  image:
    tag: "v2.2.0"
  service:
    selector:
      slot: green
```

---

## 🔄 ArgoCD & GitOps

### Repository Structure

```
gitops-repo/
├── applications/
│   ├── backend.yaml         # ArgoCD Application CRD
│   ├── frontend.yaml
│   └── functions.yaml
├── environments/
│   ├── dev/
│   │   ├── values.yaml
│   │   └── kustomization.yaml
│   ├── staging/
│   │   ├── values.yaml
│   │   └── kustomization.yaml
│   └── production/
│       ├── values-blue.yaml
│       ├── values-green.yaml
│       └── kustomization.yaml
└── base/
    └── streamvault/         # Helm charts base
```

### ArgoCD Application CRD

```yaml
# applications/backend.yaml
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

```yaml
# argocd-rbac-cm configmap
policy.csv: |
  # Developers — read-only on prod, full on dev
  p, role:developer, applications, get,    dev/*, allow
  p, role:developer, applications, sync,   dev/*, allow
  p, role:developer, applications, get,    production/*, allow
  p, role:developer, applications, sync,   production/*, deny

  # DevOps — full access all envs
  p, role:devops, applications, *,         */*, allow
  p, role:devops, clusters,     get,       *,   allow

  # Testers — sync dev and staging only
  p, role:tester, applications, get,       dev/*, allow
  p, role:tester, applications, get,       staging/*, allow

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
    ├── values-staging.yaml
    ├── values-prod.yaml
    └── templates/
        ├── _helpers.tpl
        ├── deployment.yaml
        ├── service.yaml
        ├── ingress.yaml
        ├── hpa.yaml                  # Horizontal Pod Autoscaler
        ├── pdb.yaml                  # Pod Disruption Budget
        ├── configmap.yaml
        ├── serviceaccount.yaml
        ├── networkpolicy.yaml
        └── NOTES.txt
```

### values.yaml (Excerpt)

```yaml
global:
  imageRegistry: streamvaultacr.azurecr.io
  imageTag: latest
  environment: production

backend:
  replicaCount: 10
  image:
    repository: streamvault/backend
    pullPolicy: IfNotPresent
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
    periodSeconds: 10
  readinessProbe:
    httpGet:
      path: /actuator/health/readiness
      port: 8080
    initialDelaySeconds: 20
    periodSeconds: 5

frontend:
  replicaCount: 5
  image:
    repository: streamvault/frontend
  resources:
    requests:
      memory: "256Mi"
      cpu: "100m"
    limits:
      memory: "512Mi"
      cpu: "500m"

ingress:
  enabled: true
  className: nginx
  annotations:
    nginx.ingress.kubernetes.io/proxy-body-size: "50g"
    nginx.ingress.kubernetes.io/use-regex: "true"
  hosts:
    - host: api.streamvault.com
      paths:
        - path: /api
          pathType: Prefix
    - host: streamvault.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: streamvault-tls
      hosts: [streamvault.com, api.streamvault.com]
```

---

## 📊 Dynatrace Monitoring

### What is Monitored

| Category | Metrics |
|---------|---------|
| **Application Performance** | Response time, error rate, throughput per service |
| **Streaming Quality** | Buffering ratio, bitrate switches, startup time |
| **Infrastructure** | AKS node CPU/memory, pod restarts, PVC usage |
| **Database** | Query latency, connection pool saturation, deadlocks |
| **User Experience** | Real User Monitoring (RUM), Apdex score |
| **Business KPIs** | Active viewers, concurrent streams, subscription conversions |

### Dynatrace OneAgent Helm Deployment

```yaml
# charts/dynatrace/values.yaml
oneAgent:
  apiUrl: https://ENVIRONMENT_ID.live.dynatrace.com/api
  apiToken: $(DYNATRACE_API_TOKEN)   # from Key Vault
  hostGroup: streamvault-production
  networkZone: azure-eastus

operator:
  image: public.ecr.aws/dynatrace/dynatrace-operator

dynakube:
  apiUrl: https://ENVIRONMENT_ID.live.dynatrace.com/api
  tokens: dynatrace-tokens
  oneAgent:
    classicFullStack:
      tolerations:
        - effect: NoSchedule
          key: node-role.kubernetes.io/control-plane
```

### Alerting Rules

```yaml
# Dynatrace Alerting Profile: Critical
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

  - name: "Concurrent Viewers Drop > 20%"
    condition: active_streams_delta < -20%
    window: 2m
    severity: CRITICAL
    notify: [pagerduty]

  - name: "AKS Pod Restart Loop"
    condition: pod_restarts > 3
    window: 10m
    severity: HIGH

  - name: "Database Connection Pool > 90%"
    condition: db_pool_utilization > 90%
    window: 2m
    severity: CRITICAL
```

### Spring Boot + Dynatrace Integration

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
│       └── on-call.md
│
├── frontend/                          # Angular Application
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/
│   │   │   │   ├── auth/              # MSAL, guards, interceptors
│   │   │   │   ├── services/
│   │   │   │   └── interceptors/      # JWT, error, retry
│   │   │   ├── features/
│   │   │   │   ├── video-player/      # HLS.js player component
│   │   │   │   ├── catalog/           # Browse & search
│   │   │   │   ├── dashboard/         # User dashboard
│   │   │   │   └── admin/             # Admin panel
│   │   │   └── shared/
│   │   ├── environments/
│   │   │   ├── environment.ts
│   │   │   ├── environment.staging.ts
│   │   │   └── environment.prod.ts
│   │   └── assets/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── backend/                           # Spring Boot Microservices
│   ├── user-service/
│   │   ├── src/main/java/com/streamvault/user/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── domain/
│   │   │   ├── config/
│   │   │   └── security/
│   │   ├── src/test/
│   │   ├── Dockerfile
│   │   └── pom.xml
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
│   │   ├── staging/
│   │   └── production/
│   └── modules/
│       ├── networking/
│       ├── aks/
│       ├── database/
│       ├── cache/
│       └── security/
│
├── charts/                            # Helm Charts
│   └── streamvault/
│
├── gitops/                            # ArgoCD GitOps Repo
│   ├── applications/
│   └── environments/
│
├── pipelines/                         # Azure DevOps Pipelines
│   ├── azure-pipelines.yml
│   ├── azure-pipelines-pr.yml
│   └── templates/
│       ├── build-backend.yml
│       ├── build-frontend.yml
│       ├── test.yml
│       └── deploy.yml
│
└── scripts/
    ├── blue-green-switch.sh
    ├── smoke-test.sh
    ├── db-migration.sh
    └── load-test.sh
```

---

## 🚀 Getting Started

### Prerequisites

```bash
# Required tools
az --version          # Azure CLI 2.55+
kubectl version       # 1.28+
helm version          # 3.13+
java --version        # Java 21
node --version        # Node 20+
terraform --version   # 1.6+
argocd version        # 2.9+
```

### 1. Clone & Setup

```bash
git clone https://github.com/your-org/streamvault.git
cd streamvault

# Login to Azure
az login
az account set --subscription "StreamVault-Dev"

# Connect to AKS
az aks get-credentials \
  --resource-group rg-streamvault-dev \
  --name aks-streamvault-dev
```

### 2. Provision Infrastructure

```bash
cd infrastructure/environments/dev
terraform init
terraform plan -out=tfplan
terraform apply tfplan
```

### 3. Deploy via Helm (Dev)

```bash
# Install/upgrade backend
helm upgrade --install streamvault-backend ./charts/streamvault \
  --namespace development \
  --create-namespace \
  --values charts/streamvault/values-dev.yaml \
  --set backend.image.tag=$(git rev-parse --short HEAD)

# Install/upgrade frontend
helm upgrade --install streamvault-frontend ./charts/streamvault \
  --namespace development \
  --values charts/streamvault/values-dev.yaml \
  --set frontend.image.tag=$(git rev-parse --short HEAD)
```

### 4. Run Locally

```bash
# Backend (user-service example)
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
# Backend unit tests
cd backend/user-service && mvn test

# Backend integration tests (requires Docker)
mvn verify -Pintegration-tests

# Frontend unit tests
cd frontend && npm test

# E2E tests
cd frontend && npx playwright test

# Load test (dev)
k6 run scripts/load-test.js --vus 100 --duration 60s
```

---

## 🔧 Environment Variables

All secrets are in **Azure Key Vault**. Non-secret config is in **Azure App Configuration**.

| Variable | Source | Description |
|----------|--------|-------------|
| `AZURE_KEYVAULT_URI` | Deploy config | Key Vault endpoint |
| `AZURE_APP_CONFIG_CONNECTION_STRING` | Deploy config | App Configuration endpoint |
| `SPRING_PROFILES_ACTIVE` | Deploy config | `dev`, `staging`, `prod` |
| `postgres-connection-string` | Key Vault | PostgreSQL JDBC URL |
| `redis-connection-string` | Key Vault | Redis connection string |
| `aad-client-secret` | Key Vault | Azure AD B2C client secret |
| `jwt-signing-key` | Key Vault | RS256 PEM private key |
| `azure-storage-account-key` | Key Vault | Blob storage key |
| `dynatrace-api-token` | Key Vault | Dynatrace API token |

> ⚠️ **Never commit secrets to Git.** All secrets are managed exclusively through Azure Key Vault. The managed identity on each pod is granted `Key Vault Secrets User` role at deployment time.

---

## 📖 API Documentation

OpenAPI 3.0 spec: `docs/api/openapi.yaml`

Live docs available via Swagger UI:
- Dev: `https://api-dev.streamvault.com/swagger-ui.html`
- Staging: `https://api-staging.streamvault.com/swagger-ui.html`

### Core Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
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

- All checks must pass (CI pipeline)
- Minimum 2 reviewer approvals
- Code coverage must not drop below 80%
- No new critical/high SonarQube findings
- Security scan must pass (Trivy, Snyk)
- PR description must reference a JIRA ticket

### Commit Message Format

```
type(scope): short description

feat(streaming): add adaptive bitrate switching for 4K
fix(auth): resolve token refresh race condition
chore(deps): upgrade Spring Boot to 3.2.1
docs(api): update OpenAPI spec for /stream endpoint
```

---

## 📝 License

Proprietary — All rights reserved © 2025 StreamVault Inc.

---

## 📞 Support & On-Call

| Severity | Response SLA | Channel |
|----------|-------------|---------|
| P0 — Service Down | 15 min | PagerDuty → On-call engineer |
| P1 — Major Degradation | 30 min | PagerDuty → Team lead |
| P2 — Minor Issue | 4 hours | Teams #incidents channel |
| P3 — Enhancement | Next sprint | JIRA board |

**Runbooks:** `docs/runbooks/`
**Status Page:** https://status.streamvault.com
**On-Call Rotation:** PagerDuty — `streamvault-oncall` schedule

---

*Last updated: 2025 | Maintained by the StreamVault Platform Engineering Team*
