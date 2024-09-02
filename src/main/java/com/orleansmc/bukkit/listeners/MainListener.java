package com.orleansmc.bukkit.listeners;

import com.orleansmc.bukkit.OrleansMC;
import org.bukkit.event.Listener;

public class MainListener implements Listener {
    OrleansMC plugin;

    public MainListener(OrleansMC plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static void setup(OrleansMC plugin) {
        new MainListener(plugin);
    }
}
