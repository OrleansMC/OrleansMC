package com.orleansmc.common.servers;

import com.google.gson.Gson;
import com.orleansmc.common.Settings;
import com.orleansmc.common.Util;
import com.orleansmc.common.redis.RedisManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServersManager implements ServersProvider {
    private final String SERVER_SWITCH_CHANNEL_NAME = "servers:teleport";
    private final String CHANNEL_NAME = "servers:state";
    @Setter
    private Consumer<ServerSwitchState> serverSwitchHandler;
    public final ScheduledExecutorService scheduler;
    @Getter
    public final HashMap<String, ServerState> serverStates = new HashMap<>();
    @Setter
    private Consumer<ServerState> onServerStateUpdate;
    @Setter
    private Consumer<ServerState> onServerStateTimeout;
    public RedisManager redisManager;

    public ServersManager(RedisManager redisManager) {
        scheduler = Executors.newScheduledThreadPool(1);
        this.redisManager = redisManager;

        redisManager.addListener(CHANNEL_NAME, (message) -> {
            Gson gson = new Gson();
            ServerState serverState = gson.fromJson(message, ServerState.class);
            updateServerState(serverState);
            if (onServerStateUpdate != null) {
                onServerStateUpdate.accept(serverState);
            }
        });

        redisManager.addListener(SERVER_SWITCH_CHANNEL_NAME, (message) -> {
            Gson gson = new Gson();
            ServerSwitchState serverSwitchState = gson.fromJson(message, ServerSwitchState.class);
            if (serverSwitchHandler != null) {
                serverSwitchHandler.accept(serverSwitchState);
            }
        });

        scheduler.scheduleAtFixedRate(() -> {
            sendServerState();
            checkTimeouts();
        }, 0, Settings.SERVER_STATE_INTERVAL, TimeUnit.SECONDS);

        Util.info("ServerStateManager initialized");
    }

    synchronized public void updateServerState(ServerState serverState) {
        if (!serverStates.containsKey(serverState.name)) {
            Util.info("Server " + serverState.name + " connected");
        }

        ServerState oldServerState = serverStates.get(serverState.name);
        serverStates.put(serverState.name, serverState);
        if (oldServerState != null && oldServerState.players.size() != serverState.players.size()) {
            Util.info("Server " + serverState.name + " player count changed from " + oldServerState.players.size() + " to " + serverState.players.size());
        }
    }

    public void sendServerState() {
        if (Settings.IS_PROXY) return;

        ServerState serverState = new ServerState(
                Settings.SERVER_NAME,
                Settings.SERVER_TYPE.name(),
                Settings.SERVER_IP,
                org.bukkit.Bukkit.getPort(),
                org.bukkit.Bukkit.getServer().getTPS()[0] > 20 ? 20 : (int) Math.round(org.bukkit.Bukkit.getServer().getTPS()[0]),
                new ArrayList<>(org.bukkit.Bukkit.getOnlinePlayers().stream().map(org.bukkit.entity.Player::getName).collect(Collectors.toList())),
                Bukkit.getServer().getWorlds().getFirst().getTime()
        );

        redisManager.sendMessage(CHANNEL_NAME, new Gson().toJson(serverState));
    }

    synchronized private void checkTimeouts() {
        Iterator<Map.Entry<String, ServerState>> iterator = serverStates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServerState> entry = iterator.next();
            ServerState serverStateModel = entry.getValue();
            if (serverStateModel.lastUpdate.getTime() + Settings.SERVER_STATE_TIMEOUT * 1000L < new Date().getTime()) {
                Util.info("Server " + serverStateModel.name + " timed out");
                if (onServerStateTimeout != null) {
                    onServerStateTimeout.accept(serverStateModel);
                }
                iterator.remove();
            }
        }
    }

    public ServerState getAnyServer() {
        return this.getServerStates().values().stream().min(Comparator.comparing(serverState -> serverState.name)).orElse(null);
    }

    public ServerState getAvailableServerByType(ServerType serverType) {
        return this.getServerStates().values().stream()
                .filter(serverState -> ServerType.valueOf(serverState.type) == serverType).min((serverState1, serverState2) -> {
                    if (serverState1.players.size() == serverState2.players.size()) {
                        return serverState1.name.compareTo(serverState2.name);
                    } else {
                        return Integer.compare(serverState1.players.size(), serverState2.players.size());
                    }
                }).orElse(null);
    }

    public void switchServer(String playerName, String serverName) {
        redisManager.sendMessage(SERVER_SWITCH_CHANNEL_NAME, new Gson().toJson(
                new ServerSwitchState(playerName, serverName)
        ));
    }
}