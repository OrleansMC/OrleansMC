package com.orleansmc.bukkit.commands;

import com.orleansmc.bukkit.commands.admin.ClearAlerts;
import com.orleansmc.bukkit.commands.admin.OpCommand;
import com.orleansmc.bukkit.commands.vips.CustomJoinCommands;
import com.orleansmc.bukkit.OrleansMC;

public class CommandLoader {
    public static void load(OrleansMC plugin) {
        CustomJoinCommands.setup(plugin);
        ClearAlerts.setup(plugin);
        OpCommand.setup(plugin);
    }

    public static void unload() {
        CustomJoinCommands.channelAgent.close();
    }
}
