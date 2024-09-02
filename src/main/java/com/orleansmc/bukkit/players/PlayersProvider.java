package com.orleansmc.bukkit.players;

import com.orleansmc.bukkit.players.models.PlayerModel;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.function.Consumer;

public interface PlayersProvider extends Listener {
    public void waitPlayerDataThenRun(Player player, Consumer<PlayerModel> consumer, int tries);
    public ArrayList<PlayerModel> getPlayers();
    public PlayerModel getPlayer(String name);
    public void savePlayer(PlayerModel playerModel);
}
