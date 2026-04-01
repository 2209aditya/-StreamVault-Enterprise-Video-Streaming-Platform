# Incident Response Runbook

## 🚨 Severity Levels

- P1: Platform down
- P2: Partial outage
- P3: Minor issue

---

## 🔥 Steps for P1 Incident

1. Identify affected service
2. Check logs (Dynatrace / kubectl logs)
3. Check pod status:
   kubectl get pods

4. Restart service:
   kubectl rollout restart deployment <service>

5. If not resolved:
   - Trigger failover
   - Inform stakeholders

---

## 📢 Communication

- Notify Slack channel
- Update status page