package com.orleansmc.common.redis;

import com.orleansmc.common.Settings;
import com.orleansmc.common.Util;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RedisManager implements RedisProvider {
    private final JedisPool jedisPool;
    private final HashMap<String, List<Consumer<String>>> listeners = new HashMap<>();
    private final Set<String> subscribed = ConcurrentHashMap.newKeySet();
    public final ScheduledExecutorService scheduler;

    private PubSubListener listener = null;

    public RedisManager() {
        String redisHost = Settings.REDIS_HOST;
        int redisPort = Settings.REDIS_PORT;
        String redisPassword = Settings.REDIS_PASSWORD;
        scheduler = Executors.newScheduledThreadPool(1);

        Util.info("Connecting to Redis at " + redisHost + ":" + redisPort);
        this.jedisPool = new JedisPool(redisHost, redisPort);
        subscribed.add("orleansmc");

        try (Jedis jedis = this.jedisPool.getResource()) {
            if (redisPassword != null && !redisPassword.isEmpty()) {
                Util.info("Authenticating to Redis...");
                jedis.auth(redisPassword);
            }
            jedis.ping();
        }

        scheduler.scheduleAtFixedRate(() -> {
            // ensure subscribed to all channels
            PubSubListener listener = this.listener;

            if (listener == null || !listener.isSubscribed()) {
                return;
            }

            for (String channel : this.subscribed) {
                listener.subscribe(channel.getBytes(StandardCharsets.UTF_8));
            }
        }, 600L, 600L, TimeUnit.MILLISECONDS);

        new Thread(() -> {
            new Runnable() {
                private boolean broken = false;

                @Override
                public void run() {
                    if (this.broken) {
                        Util.info("Attempting to resubscribe to Redis...");
                        this.broken = false;
                    }

                    try (Jedis jedis = getJedis()) {
                        try {
                            listener = new PubSubListener();
                            jedis.subscribe(RedisManager.this.listener, "redis-dummy".getBytes(StandardCharsets.UTF_8));
                        } catch (Exception e) {
                            // Attempt to unsubscribe this instance and try again.
                            try {
                                listener.unsubscribe();
                            } catch (Exception ignored) {
                            }
                            listener = null;
                            this.broken = true;
                        }
                    }

                    if (this.broken) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            this.run();
                        }).start();
                    }
                }
            }.run();
        }).start();
    }

    @Nonnull
    public JedisPool getJedisPool() {
        Objects.requireNonNull(this.jedisPool, "jedisPool");
        return this.jedisPool;
    }

    @Nonnull
    public Jedis getJedis() {
        return getJedisPool().getResource();
    }

    public void close() {
        if (this.listener != null) {
            this.listener.unsubscribe();
            this.listener = null;
        }

        if (this.jedisPool != null) {
            this.jedisPool.close();
        }
    }

    public void subscribe(String channel) {
        subscribed.add(channel);
        Util.info("Subscribed to channel: " + channel);
    }

    public void unsubscribe(String channel) {
        subscribed.remove(channel);
        listeners.remove(channel);

        if (this.listener != null) {
            this.listener.unsubscribe(channel.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void addListener(String channel, Consumer<String> listener) {
        this.subscribe(channel);
        this.listeners.computeIfAbsent(channel, k -> new ArrayList<>()).add(listener);
    }

    public void sendMessage(String channel, String message) {
        try (Jedis jedis = getJedis()) {
            jedis.publish(channel, message);
        }
    }

    @Override
    public void set(String key, String value) {
        try (Jedis jedis = getJedis()) {
            jedis.set(key, value);
        }
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.get(key);
        }
    }

    @Override
    public void delete(String key) {
        try (Jedis jedis = getJedis()) {
            jedis.del(key);
        }
    }

    @Override
    public void setex(String key, int seconds, String value) {
        try (Jedis jedis = getJedis()) {
            jedis.setex(key, seconds, value);
        }
    }

    @Override
    public long ttl(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.ttl(key);
        }
    }

    @Override
    public void expire(String key, int seconds) {
        try (Jedis jedis = getJedis()) {
            jedis.expire(key, seconds);
        }
    }

    @Override
    public boolean exists(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.exists(key);
        }
    }

    private final class PubSubListener extends BinaryJedisPubSub {
        @Override
        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
            String channelName = new String(channel, StandardCharsets.UTF_8);
            Util.info("Unsubscribed from channel: " + channelName);
        }

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            String channelName = new String(channel, StandardCharsets.UTF_8);

            List<Consumer<String>> listeners = RedisManager.this.listeners.get(channelName);
            if (listeners != null) {
                String messageString = new String(message, StandardCharsets.UTF_8);
                for (Consumer<String> listener : listeners) {
                    listener.accept(messageString);
                }
            }
        }
    }
}