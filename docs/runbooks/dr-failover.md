# DR Failover Runbook

## 🚨 When to trigger
- Region outage
- DB failure
- AKS cluster down

---

## 🔄 Steps

1. Verify outage
2. Run:
   ./scripts/dr-failover.sh

3. Validate:
   ./scripts/dr-health-check.sh

4. Run smoke test:
   ./scripts/smoke-test.sh

---

## ✅ Success Criteria
- Traffic routed to DR
- APIs responding
- No major errors