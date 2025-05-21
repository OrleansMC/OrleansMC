package com.orleansmc.bukkit.players;

import com.orleansmc.bukkit.OrleansMC;
import com.orleansmc.bukkit.players.models.PlayerModel;
import com.orleansmc.common.Util;
import lombok.Getter;
import me.lucko.helper.mongo.external.bson.Document;
import me.lucko.helper.mongo.external.mongodriver.Block;
import me.lucko.helper.mongo.external.mongodriver.client.MongoCollection;
import me.lucko.helper.mongo.external.mongodriver.client.model.UpdateOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class PlayersManager implements PlayersProvider {
    private final OrleansMC plugin;
    @Getter
    private final HashMap<String, PlayerModel> players = new HashMap<>();
    private final MongoCollection<Document> playersCollection;

    public PlayersManager(OrleansMC plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.playersCollection = plugin.mongoManager.getMinecraftDatabase().getCollection("players");
        loadPlayers();
    }

    private void loadPlayers() {
        StringBuilder mongoFindRegex = new StringBuilder();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            mongoFindRegex.append(Pattern.quote(player.getName())).append("|");
        }

        // Regex ifadesindeki son '|' karakterini kaldır
        if (!mongoFindRegex.isEmpty()) {
            mongoFindRegex.setLength(mongoFindRegex.length() - 1);
        }

        // MongoDB sorgusu
        playersCollection.find(
                new Document("_id", new Document("$regex", mongoFindRegex.toString()).append("$options", "i")) // "i" seçeneği büyük/küçük harf duyarsızlık sağlar
        ).forEach((Block<? super Document>) document -> {
            PlayerModel playerModel = PlayerModel.fromDocument(document);
            players.put(playerModel.name, playerModel);
        });
    }

    public PlayerModel getPlayer(String name) {
        if (players.containsKey(name)) {
            return players.get(name);
        }
        return null;
    }


    public PlayerModel fetchPlayer(String name) {
        Document document = playersCollection.find(new Document("_id", name.toLowerCase())).first();
        if (document == null) {
            final PlayerModel playerModel = new PlayerModel(
                    name,
                    Bukkit.getOfflinePlayer(name).getUniqueId().toString(),
                    0,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    null,
                    null
            );
            savePlayer(playerModel);
            return playerModel;
        }
        return PlayerModel.fromDocument(document);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerModel playerModel = fetchPlayer(player.getName());
            players.put(playerModel.name, playerModel);
            plugin.getLogger().info("Player " + player.getName() + " joined and player model has been cached.");
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerModel playerModel = getPlayer(player.getName());
        if (playerModel != null) {
            players.remove(playerModel.name);
            plugin.getLogger().info("Player " + player.getName() + " quit and player model has been removed from cache.");
        }
    }

    public void waitPlayerDataThenRun(Player player, Consumer<PlayerModel> consumer, int tries) {
        if (tries > 20 * 20) {
            plugin.getLogger().warning("Failed to get player data for " + player.getName());
            player.kick(Util.getComponent("<red>Verileriniz yüklenemedi. Lütfen tekrar giriş yapın."));
            return;
        }
        PlayerModel playerModel = plugin.playersManager.getPlayer(player.getName());
        if (playerModel == null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> waitPlayerDataThenRun(player, consumer, tries + 1), 1);
            return;
        }
        consumer.accept(playerModel);
    }

    public void savePlayer(PlayerModel playerModel) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> playersCollection.updateOne(
                new Document("_id", playerModel.name.toLowerCase()),
                new Document("$set", playerModel.toDocument()),
                new UpdateOptions().upsert(true)
        ));
    }
}
