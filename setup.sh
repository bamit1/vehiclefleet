mvn clean install -f ./listener/pom.xml
mvn clean package -f ./emitter/pom.xml
mvn clean install -f ./executionservice/pom.xml
docker compose -f doker-compose.yml -p vehiclefleet up -d --build
sleep 20
docker exec clickhouse clickhouse-client --multiline --queries-file /tmp/clickhouse-server/schema.sql