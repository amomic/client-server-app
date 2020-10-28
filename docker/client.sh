#!/bin/sh

SERVER_URL=server
SERVER_PORT=65000

docker start oop2-client > /dev/null
docker exec -ti oop2-client java -jar client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar $SERVER_URL $SERVER_PORT $@
