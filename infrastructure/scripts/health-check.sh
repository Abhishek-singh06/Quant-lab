#!/bin/bash
# =========================================
# QuantLab Health Check Script
# =========================================
# Checks the health of all QuantLab services.
# Usage: ./health-check.sh
# =========================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
QUANT_URL="${QUANT_URL:-http://localhost:8000}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:3000}"

echo "========================================"
echo "  QuantLab Health Check"
echo "========================================"
echo ""

check_service() {
    local name=$1
    local url=$2
    local endpoint=$3

    printf "%-20s " "$name:"
    if response=$(curl -sf --max-time 5 "${url}${endpoint}" 2>/dev/null); then
        echo -e "${GREEN}✓ Healthy${NC}"
        echo "  Response: $response" | head -c 200
        echo ""
    else
        echo -e "${RED}✗ Unhealthy${NC}"
    fi
}

check_service "Backend API" "$BACKEND_URL" "/api/health"
check_service "Quant Service" "$QUANT_URL" "/health"
check_service "Frontend" "$FRONTEND_URL" "/"

echo ""
echo "========================================"
echo "  Check complete"
echo "========================================"
