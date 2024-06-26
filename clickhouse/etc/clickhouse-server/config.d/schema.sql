CREATE DATABASE IF NOT EXISTS fleet;

USE fleet;

CREATE TABLE IF NOT EXISTS fleet_events (
    vehicleId String,
    distance Decimal64(2),
    avgSpeed Int32,
    avgFuelLevel Int32,
    overSpeed Bool,
    time Int64
) ENGINE = MergeTree ORDER BY (vehicleId, time);

CREATE TABLE IF NOT EXISTS fleet_events_queue (
    vehicleId String,
    distance Decimal64(2),
    avgSpeed Int32,
    avgFuelLevel Int32,
    overSpeed Bool,
    time Int64
) ENGINE = Kafka()
SETTINGS kafka_broker_list = 'kafka:9092',
       kafka_topic_list = 'fleet-events',
       kafka_group_name = 'clickhouse',
       kafka_format = 'JSONEachRow',
       kafka_num_consumers = 1;

CREATE MATERIALIZED VIEW IF NOT EXISTS fleet_events_mv TO fleet_events AS
SELECT vehicleId, distance, avgSpeed, avgFuelLevel, overSpeed, time
FROM fleet_events_queue;


CREATE TABLE IF NOT EXISTS fleet_events_daily (
    vehicleId String,
    distance AggregateFunction(sum, Decimal64(2)),
    overSpeed AggregateFunction(max, Bool),
    time Date
) ENGINE = AggregatingMergeTree ORDER BY (time, vehicleId);


CREATE MATERIALIZED VIEW IF NOT EXISTS fleet_events_daily_mv TO fleet_events_daily AS
SELECT vehicleId, sumState(distance) as distance, maxState(overSpeed) as overSpeed, toDate(toDateTime(time/1000)) as time
FROM fleet_events
GROUP BY time, vehicleId;

-- select vehicleId, sumMerge(distance), maxMerge(overSpeed), time from fleet_events_daily4 group by vehicleId, time