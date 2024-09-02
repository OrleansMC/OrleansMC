package com.orleansmc.common.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.function.Consumer;

public interface RedisProvider {
    public JedisPool getJedisPool();
    public Jedis getJedis();
    public void addListener(String channel, Consumer<String> listener);
    public void sendMessage(String channel, String message);
    public void close();
}