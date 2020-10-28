#!/bin/sh

SERVER_PORT=65000
DATA_PATH="/app/data"

docker start oop2-server > /dev/null
docker exec -ti oop2-server java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 -jar analysis-server/target/analysis-server-1.0-SNAPSHOT-jar-with-dependencies.jar -p $SERVER_PORT -d $DATA_PATH
