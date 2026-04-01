#!/bin/bash

set -e

ACTIVE=$(kubectl get svc streamvault-svc -o jsonpath='{.spec.selector.version}')

if [ "$ACTIVE" == "blue" ]; then
  NEW="green"
else
  NEW="blue"
fi

echo "Switching traffic from $ACTIVE to $NEW"

kubectl patch svc streamvault-svc -p "{\"spec\": {\"selector\": {\"app\": \"streamvault\", \"version\": \"$NEW\"}}}"

echo "Traffic switched to $NEW"