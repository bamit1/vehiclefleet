package com.vehiclefleet.executionservice.redis;

import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
public class CacheClient {

    @Autowired
    private RedisTemplate<String, FleetUpdateEvent> redisTemplate;

    public void addToSet(String key, FleetUpdateEvent event) {
        redisTemplate.opsForZSet().add(key, event, event.getTime());
    }

    public Set<FleetUpdateEvent> getFromSet(String key, int start, int end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public void removeFromSet(String key, FleetUpdateEvent... events) {
        redisTemplate.opsForZSet().remove(key, events);
    }

    public void setLastProcessed(String key, FleetUpdateEvent event) {
        redisTemplate.opsForValue().set(key, event);
    }

    public FleetUpdateEvent getLastProcessed(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
