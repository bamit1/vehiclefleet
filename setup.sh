mvn clean install -f ./listener/pom.xml
docker compose -f doker-compose.yml -p vehiclefleet up -d --build
docker exec kafka kafka-topics --bootstrap-server kafka:9092 --topic fleet-update-events --create --partitions 1 --replication-factor 1
docker exec clickhouse clickhouse-client --multiline --queries-file /tmp/clickhouse-server/schema.sql