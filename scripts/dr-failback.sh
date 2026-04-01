#!/bin/bash

echo "🔄 Initiating Failback to Primary..."

az network dns record-set cname set-record \
  --resource-group streamvault-rg \
  --zone-name streamvault.com \
  --record-set-name app \
  --cname primary.streamvault.com

echo "Traffic switched back to primary"