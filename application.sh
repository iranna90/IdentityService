#!/usr/bin/env bash
export POSTGRES_IMAGE="docker-ngmpp-prod.artifactory.engineering-hdc.net/library/postgres:9.5.5-5"
export APPLICATION_IMAGE="identity-server"
export POSTGRES_PORT=1111
export APPLICATION_PORT=9876

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
COMPOSE_FILE_PATH="${SCRIPT_DIR}/deploy/docker-compose.yml"

echo "Building application with compose file location ${COMPOSE_FILE_PATH}"

mvn clean install

echo "Deploy down if already up"

docker-compose -f $COMPOSE_FILE_PATH down

echo "Deploy the application"
docker-compose -f $COMPOSE_FILE_PATH up


