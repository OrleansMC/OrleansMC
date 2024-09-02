package com.orleansmc.bukkit.commands.admin;

import com.orleansmc.bukkit.OrleansMC;
import com.orleansmc.bukkit.players.models.PlayerModel;
import me.lucko.helper.Commands;

public class ClearAlerts {
    public static void setup(OrleansMC plugin) {
        Commands.create()
                .assertOp()
                .handler(c -> {
                    String playerName = !c.args().isEmpty() ? c.args().get(0) : null;
                    PlayerModel player = plugin.playersManager.getPlayer(playerName);
                    if (player == null) {
                        c.reply("§cPlayer not found.");
                        return;
                    }
                    player.alerts.clear();
                    plugin.playersManager.savePlayer(player);
                    c.reply("§aAlerts cleared for " + player);
                })
                .registerAndBind(plugin, "clear-alerts");
    }
}
