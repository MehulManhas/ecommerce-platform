#!/usr/bin/env bash
#
# benchmark.sh — simple latency benchmark for a single GET endpoint.
# Portable: needs only bash, curl, sort and awk (all present on macOS).
#
# Usage:
#   ./benchmark.sh <url> [requests]
# Examples:
#   ./benchmark.sh http://localhost:9090/products 1000
#   ./benchmark.sh http://localhost:9090/products/<uuid> 2000
#
set -euo pipefail

URL="${1:?usage: benchmark.sh <url> [requests]}"
N="${2:-1000}"
WARMUP=20

tmp="$(mktemp)"
trap 'rm -f "$tmp"' EXIT

echo "Warming up ($WARMUP requests)…"
for _ in $(seq 1 "$WARMUP"); do curl -s -o /dev/null "$URL" || true; done

echo "Measuring $N requests against $URL …"
for _ in $(seq 1 "$N"); do
  curl -s -o /dev/null -w '%{time_total}\n' "$URL"
done | sort -n > "$tmp"

awk '
{ v[NR] = $1 * 1000; sum += $1 * 1000 }
END {
  n = NR
  if (n == 0) { print "no samples"; exit 1 }
  i50 = int(n * 0.50); if (i50 < 1) i50 = 1
  i95 = int(n * 0.95); if (i95 < 1) i95 = 1
  i99 = int(n * 0.99); if (i99 < 1) i99 = 1
  printf "samples : %d\n", n
  printf "avg     : %7.2f ms\n", sum / n
  printf "min     : %7.2f ms\n", v[1]
  printf "p50     : %7.2f ms\n", v[i50]
  printf "p95     : %7.2f ms\n", v[i95]
  printf "p99     : %7.2f ms\n", v[i99]
  printf "max     : %7.2f ms\n", v[n]
}' "$tmp"
