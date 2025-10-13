#!/bin/bash
# Keycloak Realm Export Script
#
# This script exports the GitGud realm configuration from the running Keycloak
# container and saves it to the version-controlled realms directory.
#
# Usage: ./scripts/keycloak-export.sh

set -e

CONTAINER_NAME="gitgud-keycloak"
REALM_NAME="gitgud"
EXPORT_DIR="/tmp/keycloak-export"
OUTPUT_FILE="./docker/keycloak/realms/${REALM_NAME}-realm.json"

echo "🔑 Exporting Keycloak realm configuration..."

# Check if container is running
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "❌ Error: Keycloak container '${CONTAINER_NAME}' is not running"
    echo "   Start it with: docker-compose up -d keycloak"
    exit 1
fi

# Export realm (skip users - they should come from database backups)
echo "📤 Exporting realm '${REALM_NAME}' from container..."
docker exec -it "$CONTAINER_NAME" /opt/keycloak/bin/kc.sh export \
    --dir "$EXPORT_DIR" \
    --realm "$REALM_NAME" \
    --users skip

# Copy exported file to host
echo "📋 Copying exported realm to host..."
docker cp "${CONTAINER_NAME}:${EXPORT_DIR}/${REALM_NAME}-realm.json" "$OUTPUT_FILE"

# Clean up exported file in container
docker exec "$CONTAINER_NAME" rm -rf "$EXPORT_DIR"

echo "✅ Realm exported successfully to: $OUTPUT_FILE"
echo ""
echo "📝 Next steps:"
echo "   1. Review the changes: git diff $OUTPUT_FILE"
echo "   2. Optionally clean up the JSON (remove IDs, format, etc.)"
echo "   3. Commit the changes: git add $OUTPUT_FILE && git commit -m 'Update Keycloak realm'"
echo ""
echo "⚠️  Remember to:"
echo "   - Remove any sensitive data (secrets, passwords)"
echo "   - Replace secrets with environment variable references: \${env.VAR_NAME}"
echo "   - Test the import by restarting: docker-compose restart keycloak"
