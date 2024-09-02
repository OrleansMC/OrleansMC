package com.orleansmc.bukkit;

import com.orleansmc.bukkit.commands.CommandLoader;
import com.orleansmc.bukkit.listeners.MainListener;
import com.orleansmc.bukkit.players.PlayersManager;
import com.orleansmc.bukkit.players.PlayersProvider;
import com.orleansmc.bukkit.teleport.TeleportManager;
import com.orleansmc.bukkit.teleport.TeleportProvider;
import com.orleansmc.bukkit.actions.ActionManager;
import com.orleansmc.common.Settings;
import com.orleansmc.common.Util;
import com.orleansmc.bukkit.mongo.MongoManager;
import com.orleansmc.bukkit.mongo.MongoProvider;
import com.orleansmc.common.redis.RedisManager;
import com.orleansmc.common.redis.RedisProvider;
import com.orleansmc.common.servers.ServersManager;
import com.orleansmc.common.servers.ServersProvider;
import com.orleansmc.common.webhooks.WebhookManager;
import com.orleansmc.common.webhooks.WebhookProvider;
import me.lucko.helper.plugin.ExtendedJavaPlugin;

import java.io.File;

public class OrleansMC extends ExtendedJavaPlugin {
    public RedisManager redisManager;
    public ServersManager serversManager;
    public MongoManager mongoManager;
    public PlayersManager playersManager;
    public TeleportManager teleportManager;
    public WebhookManager webhookManager;
    public ActionManager actionManager;

    protected void enable() {
        this.getLogger().info("OrleansMC has been enabled!");

        Util.setBukkitLogger(this.getLogger());

        this.reloadSettings();
        this.webhookManager = new WebhookManager();
        this.redisManager = new RedisManager();
        this.mongoManager = new MongoManager();
        this.serversManager = new ServersManager(this.redisManager);
        this.playersManager = new PlayersManager(this);
        this.teleportManager = new TeleportManager(this);
        this.actionManager = new ActionManager(this);

        provideService(ServersProvider.class, this.serversManager);
        provideService(RedisProvider.class, this.redisManager);
        provideService(MongoProvider.class, this.mongoManager);
        provideService(PlayersProvider.class, this.playersManager);
        provideService(TeleportProvider.class, this.teleportManager);
        provideService(WebhookProvider.class, this.webhookManager);
        provideService(ActionManager.class, this.actionManager);

        MainListener.setup(this);
        CommandLoader.load(this);
    }

    protected void disable() {
        this.getLogger().info("OrleansMC has been disabled!");
        this.serversManager.scheduler.shutdown();
        this.redisManager.scheduler.shutdown();
        this.webhookManager.scheduler.shutdown();

        try {
            this.redisManager.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CommandLoader.unload();
    }

    public void reloadSettings() {
        this.getLogger().info("Reloading settings...");
        Settings.loadSettings(new File(this.getDataFolder(), "config.yml"));
    }
}
