# On-Call Runbook

## 📅 Responsibilities

- Monitor alerts (Dynatrace)
- Respond to incidents
- Perform health checks

---

## 🔍 Daily Checklist

- Check service health endpoints
- Verify AKS node status
- Check error rates

---

## 🧰 Useful Commands

kubectl get pods
kubectl logs <pod>
kubectl top nodes

---

## 🚀 Escalation

- L1 → L2 → Architect
- Escalate if unresolved in 30 mins