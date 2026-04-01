# StreamVault - High Level Design (HLD)

## 📌 Overview
StreamVault is a cloud-native video streaming platform designed to support 10 million concurrent users using Azure-native services and microservices architecture.

---

## 🏗 System Components

### 1. Client Layer
- Angular SPA (CDN hosted)
- Supports web/mobile browsers

### 2. Edge Layer
- Azure Front Door (Global LB + WAF)
- Azure CDN (Video + static delivery)

### 3. API Layer
- API Gateway (Spring Cloud Gateway / APIM)
- Routes requests to backend microservices

### 4. Microservices Layer
- User Service
- Video Service
- Streaming Service
- Analytics Service
- Notification Service

### 5. Data Layer
- PostgreSQL (user, metadata)
- Redis (cache, sessions)
- Blob Storage (video chunks)

### 6. Async/Event Layer
- Azure Service Bus
- Event-driven communication

### 7. Observability
- Dynatrace APM
- Centralized logging

---

## ⚡ Key Design Decisions

- CDN-first video delivery
- Stateless microservices
- Event-driven processing
- Horizontal scalability via AKS
- Multi-region DR readiness

---

## 📈 Scalability Strategy

- Auto-scaling AKS node pools
- CDN edge caching
- Redis caching layer
- Async processing via Service Bus

---

## 🔐 Security

- Azure AD B2C (OIDC)
- JWT authentication
- WAF + DDoS protection
- Signed video URLs

---

## 🌍 Disaster Recovery

- Active-Passive DR setup
- Geo-replicated storage
- DNS-based failover