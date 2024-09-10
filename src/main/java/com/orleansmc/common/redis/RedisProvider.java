package com.orleansmc.common.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.function.Consumer;

public interface RedisProvider {
    public JedisPool getJedisPool();
    public Jedis getJedis();
    public String get(String key);
    public void set(String key, String value);
    public void delete(String key);
    public boolean exists(String key);
    public void setex(String key, int seconds, String value);
    public long ttl(String key);
    public void expire(String key, int seconds);
    public void addListener(String channel, Consumer<String> listener);
    public void sendMessage(String channel, String message);
    public void close();
}