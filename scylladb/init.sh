#!/bin/bash
set -e

echo "Waiting for ScyllaDB to be ready..."
until cqlsh -e "describe cluster" > /dev/null 2>&1; do
  sleep 2
done

echo "ScyllaDB is ready. Executing init.cql..."
cqlsh -f /docker-entrypoint-initdb.d/init.cql

echo "Initialization complete!"
