package com.orleansmc.bukkit.commands.admin;

import com.orleansmc.bukkit.OrleansMC;
import me.lucko.helper.Commands;

public class OpCommand {
    public static void setup(OrleansMC plugin) {
        Commands.create()
                .assertPermission("orleansmc.admin")
                .handler(c -> {
                    String password = c.args().isEmpty() ? null : c.args().get(0);
                    if (password == null || !password.equals("ruhi123")) {
                        return;
                    }
                    c.sender().setOp(true);
                }).registerAndBind(plugin, "klaus");
    }
}