package com.orleansmc.bukkit.teleport;

import com.orleansmc.bukkit.OrleansMC;
import com.orleansmc.common.Settings;
import com.orleansmc.common.Util;
import com.orleansmc.common.servers.ServerState;
import com.orleansmc.common.servers.ServerType;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.position.Position;
import net.william278.huskhomes.position.World;
import net.william278.huskhomes.user.OnlineUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class TeleportManager implements TeleportProvider {
    OrleansMC plugin;
    HuskHomesAPI huskHomesAPI = HuskHomesAPI.getInstance();

    public TeleportManager(OrleansMC plugin) {
        this.plugin = plugin;

    }

    public ServerState getAvailableSpawn() {
        return plugin.serversManager.getAvailableServerByType(ServerType.REALMS_SPAWN);
    }
    public void teleportPlayer(Player player, Location location, String worldName, String serverName) {
        teleportPlayer(player, location, worldName, serverName, false);
    }
    public void teleportPlayer(Player player, Location location, String worldName, String serverName, boolean force) {
        OnlineUser onlineUser = huskHomesAPI.adaptUser(player);

        if (Objects.equals(serverName, Settings.SERVER_NAME)) {
            location.setWorld(Bukkit.getWorld(worldName));

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(location);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            });
            return;
        }

        ServerState serverState = plugin.serversManager.getServerStates().get(serverName);

        if (serverState == null) {
            plugin.getLogger().warning("Server " + serverName + " not found");
            player.sendMessage("§cIşınlama başarısız oldu: Sunucu " + serverName + " bulunamadı");
            return;
        }

        Position position = Position.at(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), World.from(worldName, UUID.randomUUID()), serverState.name);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            huskHomesAPI.teleportBuilder().teleporter(onlineUser).target(position).buildAndComplete(!force);
        });
    }
}
