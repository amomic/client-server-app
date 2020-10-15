#!/bin/sh

cd "$(dirname "$0")"
cd "../"
docker-compose build
docker-compose up --no-start
