#!/bin/bash

echo "Generating NFT Report..."

echo "Latency:" $(curl -o /dev/null -s -w "%{time_total}" https://streamvault.com)

echo "CPU Usage:"
kubectl top pods

echo "Memory Usage:"
kubectl top pods

echo "Report generated"