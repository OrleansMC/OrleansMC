package com.orleansmc.bukkit.teleport;

import com.orleansmc.common.servers.ServerState;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface TeleportProvider {
    public ServerState getAvailableSpawn();
    public void teleportPlayer(Player player, Location location, String worldName, String serverName, boolean force);
    public void teleportPlayer(Player player, Location location, String worldName, String serverName);
}
