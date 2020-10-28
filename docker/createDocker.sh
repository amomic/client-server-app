#!/bin/sh

cd "$(dirname "$0")"
cd "../"
docker rm -f oop2-client
docker rm -f oop2-server
docker rm -f oop2-mvn
docker-compose build
docker-compose up --no-start
