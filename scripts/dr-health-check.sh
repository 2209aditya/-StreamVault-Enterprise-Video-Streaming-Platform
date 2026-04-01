#!/bin/bash

URL="https://streamvault.com/health"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" $URL)

if [ "$STATUS" == "200" ]; then
  echo "Primary is healthy ✅"
  exit 0
else
  echo "Primary is DOWN ❌"
  exit 1
fi