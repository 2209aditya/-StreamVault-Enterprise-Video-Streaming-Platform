#!/bin/bash

BASE_URL="https://streamvault.com"

echo "Running smoke tests..."

curl -f $BASE_URL/health || exit 1
curl -f $BASE_URL/videos || exit 1

echo "Smoke tests passed ✅"