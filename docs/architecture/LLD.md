# StreamVault - Low Level Design (LLD)

## 🔹 API Gateway Flow

Client → Front Door → API Gateway → Microservices

---

## 🔹 Streaming Flow

1. User requests video
2. API Gateway validates token
3. Streaming service returns signed URL
4. CDN serves HLS chunks

---

## 🔹 Video Upload Flow

1. Admin uploads video
2. Stored in Blob Storage
3. Event sent to Service Bus
4. Encoder Function processes video
5. HLS chunks stored in Blob

---

## 🔹 Analytics Flow

1. User watches video
2. Event sent to Service Bus
3. Analytics service consumes
4. Data stored in DB + Redis

---

## 🔹 Caching Strategy

- Redis:
  - Trending videos
  - User sessions
- CDN:
  - Static assets
  - Video segments

---

## 🔹 Database Design

### User Table
- id
- email
- name

### Video Table
- id
- title
- url

---

## 🔹 Failure Handling

- Retry via Service Bus
- Circuit breaker (Resilience4j)
- Fallback responses