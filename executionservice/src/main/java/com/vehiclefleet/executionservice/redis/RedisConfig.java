package com.vehiclefleet.executionservice.redis;

import com.vehiclefleet.executionservice.models.FleetUpdateEvent;
import com.vehiclefleet.executionservice.serde.RedisFleetUpdateEventSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName("redis");
        factory.setPort(6379);
        return factory;
    }


    @Bean
    public RedisTemplate<String, FleetUpdateEvent> redisTemplate() {
        RedisTemplate<String, FleetUpdateEvent> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new RedisFleetUpdateEventSerializer());
        return template;
    }
}
