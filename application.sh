#!/usr/bin/env bash
# Retrieving local ip. "grep -v link" is added to prevent VPN tunnel getting in the way
DEFAULT_GATEWAY=$(ip route | grep default | grep -v link | head -1 | awk '{print $3}')
IP_ADDRESS_ON_LAN=$(ip route get ${DEFAULT_GATEWAY} | grep src | awk '{print $5}')
# IP to export JMX and other ports
export HOST_IP="$IP_ADDRESS_ON_LAN"

export POSTGRES_IMAGE="docker-ngmpp-prod.artifactory.engineering-hdc.net/library/postgres:9.5.5-5"
export APPLICATION_IMAGE="identity-server"
export POSTGRES_PORT=5532
export APPLICATION_PORT=9876
export POSTGRES_DB_NAME=identity_service
export POSTGRES_UNAME=identity
export POSTGRES_PASSWORD=changeme001


SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
COMPOSE_FILE_PATH="${SCRIPT_DIR}/deploy/docker-compose.yml"

echo "Building application with compose file location ${COMPOSE_FILE_PATH}"

mvn clean install

echo "Deploy down if already up"

docker-compose -f $COMPOSE_FILE_PATH down

echo "Build the application"
docker-compose -f $COMPOSE_FILE_PATH build
echo "Deploy the application"
docker-compose -f $COMPOSE_FILE_PATH up -d --remove-orphans --no-build


