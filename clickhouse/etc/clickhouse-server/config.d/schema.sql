CREATE DATABASE IF NOT EXISTS fleet;

USE fleet;

CREATE TABLE IF NOT EXISTS fleet_events (
    vehicleId Int32,
    lat Float64,
    lng Float64,
    speed Int32,
    fuelLevel Int32,
    time DateTime
) ENGINE = MergeTree ORDER BY (time, vehicleId);

CREATE TABLE IF NOT EXISTS fleet_events_queue (
    vehicleId Int32,
    lat Float64,
    lng Float64,
    speed Int32,
    fuelLevel Int32,
    time DateTime
) ENGINE = Kafka()
SETTINGS kafka_broker_list = 'kafka:9092',
       kafka_topic_list = 'fleet-update-events',
       kafka_group_name = 'clickhouse',
       kafka_format = 'JSONEachRow',
       kafka_num_consumers = 1;

CREATE MATERIALIZED VIEW IF NOT EXISTS fleet_events_mv TO fleet_events AS
SELECT vehicleId, lat, lng, speed, fuelLevel, time
FROM fleet_events_queue;
