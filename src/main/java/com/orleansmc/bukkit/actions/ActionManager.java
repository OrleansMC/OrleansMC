package com.orleansmc.bukkit.actions;

import com.orleansmc.bukkit.OrleansMC;
import com.orleansmc.common.Settings;
import com.orleansmc.common.servers.ServerState;
import com.orleansmc.common.webhooks.DiscordWebhook;
import me.lucko.helper.mongo.external.bson.Document;
import me.lucko.helper.mongo.external.mongodriver.Block;
import me.lucko.helper.mongo.external.mongodriver.client.MongoCollection;
import org.bukkit.Bukkit;

import java.util.Objects;

public class ActionManager implements ActionProvider {
    private final OrleansMC plugin;
    private final MongoCollection<Document> pendingActionsCollection;

    public ActionManager(OrleansMC plugin) {
        this.plugin = plugin;
        this.pendingActionsCollection = plugin.mongoManager.getMinecraftDatabase().getCollection("pending_actions");
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkAndExecutePendingActions, 0, 10);
    }

    private void checkAndExecutePendingActions() {
        ServerState availableServer = plugin.serversManager.getAvailableServerByType(Settings.SERVER_TYPE);
        if (availableServer != null && Objects.equals(availableServer.name, Settings.SERVER_NAME)) {
            executePendingActions();
        }
    }

    public void executePendingActions() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            processPendingActions(new Document("server_type", Settings.SERVER_TYPE.name()));
            ServerState anyServer = plugin.serversManager.getAnyServer();
            if (anyServer.name.equals(Settings.SERVER_NAME)) {
                processPendingActions(new Document("server_type", null));
            }
        });
    }

    private void processPendingActions(Document query) {
        pendingActionsCollection.find(query).forEach((Block<? super Document>) actionDocument -> {
            plugin.getLogger().info("Found pending action: " + actionDocument.toJson());
            PendingAction action = PendingAction.fromDocument(actionDocument);
            executePendingAction(action);
            pendingActionsCollection.deleteOne(actionDocument);
        });
    }

    public void executePendingAction(PendingAction action) {
        if (action.type() == ActionType.COMMAND) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().info("Executing command: " + action.command());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.command());
            });
        }
        sendWebhook(action);
    }

    private void sendWebhook(PendingAction action) {
        DiscordWebhook webhook = new DiscordWebhook(Settings.ACTIONS_WEBHOOK_URL);
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("Eylem Gerçekleştirildi")
                .setDescription("Komut: " + action.command())
                .addField("Sebep", action.reason(), false)
                .addField("Sunucu Türü", action.serverType() == null ? "HERHANGİ" : action.serverType().name(), false)
                .setFooter("Sunucu: " + Settings.SERVER_NAME, null)
        );
        plugin.webhookManager.sendWebhook(webhook);
    }
}