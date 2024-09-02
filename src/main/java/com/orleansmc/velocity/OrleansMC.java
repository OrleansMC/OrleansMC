package com.orleansmc.velocity;

import com.google.inject.Inject;
import com.orleansmc.common.Settings;
import com.orleansmc.common.Util;
import com.orleansmc.common.redis.RedisManager;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.common.servers.ServersManager;
import com.orleansmc.common.servers.ServerState;
import com.orleansmc.common.webhooks.DiscordWebhook;
import com.orleansmc.common.webhooks.WebhookManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import org.slf4j.Logger;

import java.awt.*;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Path;

@Plugin(
        id = "orleansmc",
        name = "OrleansMC",
        version = "0.1",
        description = "OrleansMC Main Plugin",
        url = "https://mustafacan.dev",
        authors = {"MustqfaCan"}
)
public class OrleansMC {
    RedisManager redisManager;
    ServersManager serversManager;
    WebhookManager webhookManager;

    @Inject
    private Logger logger;

    @Inject
    private @DataDirectory Path dataDirectory;

    @Inject
    private ProxyServer server;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("OrleansMC has been enabled!");
        logger.info("Data directory: {}", dataDirectory.toString());
        Util.setVelocityLogger(logger);

        Settings.loadSettings(new File(dataDirectory.toFile(), "config.yml"));
        redisManager = new RedisManager();
        serversManager = new ServersManager(redisManager);
        webhookManager = new WebhookManager();
        serversManager.setOnServerStateUpdate(serverStatus -> addServer(
                serverStatus.name, serverStatus.ip, serverStatus.port
        ));
        serversManager.setOnServerStateTimeout(serverStatus -> removeServer(
                serverStatus.name
        ));
        serversManager.setServerSwitchHandler(serverSwitchState -> {
            Player player = server.getPlayer(serverSwitchState.playerName).orElse(null);
            if (player != null) {
                RegisteredServer targetServer = server.getServer(serverSwitchState.serverName).orElse(null);
                if (targetServer == null) {
                    logger.warn("Server {} not found", serverSwitchState.serverName);
                    player.sendMessage(Util.getComponent("<red>Sunucu bulunamadı!"));
                    return;
                }
                player.createConnectionRequest(targetServer).fireAndForget();
                logger.info("Player {} is switching to {}", player.getUsername(), targetServer.getServerInfo().getName());
            }
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        boolean isLoggedIn = serversManager.serverStates.values().stream().anyMatch(
                serverState -> serverState.players.contains(event.getPlayer().getUsername())
        );
        if (!isLoggedIn) {
            return;
        }
        Player player = event.getPlayer();
        DiscordWebhook webhook = new DiscordWebhook(Settings.IN_OUT_WEBHOOK_URL);
        webhook.setAvatarUrl("https://mc-heads.net/avatar/" + player.getUsername() + "/64.png");
        webhook.setUsername(player.getUsername());
        webhook.addEmbed(
                new DiscordWebhook.EmbedObject()
                        .setTitle("Oyuncu sunucudan ayrıldı.")
                        .addField("Oyuncu", event.getPlayer().getUsername(), true)
                        .addField("IP Adresi", "||" + event.getPlayer().getRemoteAddress().getAddress().getHostAddress() + "||", true)
                        .addField("Tarih", "<t:" + (System.currentTimeMillis() / 1000) + ":F>", false)
                        .setColor(Color.RED)
        );
        webhookManager.sendWebhook(
                webhook
        );
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onPlayerLogin(ServerPreConnectEvent event) {
        if (event.getOriginalServer().getServerInfo().getName().equalsIgnoreCase("lobby")) {
            RegisteredServer spawn = getAvailableSpawn();
            if (spawn != null) {
                logger.info("Player {} is connecting to {}", event.getPlayer().getUsername(), spawn.getServerInfo().getName());
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(spawn));
                boolean isFirstLogin = serversManager.serverStates.values().stream().noneMatch(
                        serverState -> serverState.players.contains(event.getPlayer().getUsername())
                );
                if (isFirstLogin) {
                    Player player = event.getPlayer();
                    DiscordWebhook webhook = new DiscordWebhook(Settings.IN_OUT_WEBHOOK_URL);
                    webhook.setAvatarUrl("https://mc-heads.net/avatar/" + player.getUsername() + "/64.png");
                    webhook.setUsername(player.getUsername());
                    webhook.addEmbed(
                            new DiscordWebhook.EmbedObject()
                                    .setTitle("Oyuncu sunucuya giriş yaptı.")
                                    .addField("Oyuncu", player.getUsername(), true)
                                    .addField("IP Adresi", "||" + player.getRemoteAddress().getAddress().getHostAddress() + "||", true)
                                    .addField("Tarih", "<t:" + (System.currentTimeMillis() / 1000) + ":F>", false)
                                    .setColor(Color.GREEN)
                    );
                    webhookManager.sendWebhook(
                            webhook
                    );
                }
                return;
            } else {
                logger.warn("No available spawn servers");
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(
                        Util.getComponent(
                                "<bold><gradient:#00ffcc:#00ccff>Sunucu yeniden başlatılıyor!</gradient></bold>\n\n" +
                                        "<color:#b066ff>Lütfen birkaç dakika içerisinde tekrar deneyin.</color>\n\n" +
                                        "<hover:show_text:'Destek için tıkla!'><click:open_url:'https://discord.gg/orleansmc'><color:#7289da>Destek İçin: <u>discord.gg/orleansmc</u></color></click></hover>"
                        )
                );
                return;
            }
        }

        logger.info("Player {} is connecting to {}", event.getPlayer().getUsername(), event.getOriginalServer().getServerInfo().getName());
    }

    public RegisteredServer getAvailableSpawn() {
        ServerState spawnStatus = serversManager.getAvailableServerByType(ServerType.REALMS_SPAWN);
        return spawnStatus != null ? server.getServer(spawnStatus.name).orElse(null) : null;
    }

    public void addServer(String serverName, String ip, int port) {
        if (server.getServer(serverName).isEmpty()) {
            InetSocketAddress address = new InetSocketAddress(ip, port);
            ServerInfo serverInfo = new ServerInfo(serverName, address);
            server.registerServer(serverInfo);

            logger.info("Registered server {} at {}:{}", serverName, ip, port);
        }
    }

    public void removeServer(String serverName) {
        if (server.getServer(serverName).isPresent()) {
            server.unregisterServer(server.getServer(serverName).get().getServerInfo());
            logger.info("Unregistered server {}", serverName);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("OrleansMC has been disabled!");
        serversManager.scheduler.shutdown();
        redisManager.scheduler.shutdown();
        webhookManager.scheduler.shutdown();
        try {
            redisManager.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
