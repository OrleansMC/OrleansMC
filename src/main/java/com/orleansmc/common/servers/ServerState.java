package com.orleansmc.common.servers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerState {
    public final Date lastUpdate;
    public final String name;
    public final String type;
    public final String ip;
    public final int port;
    public final int tps;
    public final List<String> players = new ArrayList<>();

    public ServerState(String name, String type, String ip, int port, int tps, List<String> players) {
        this.lastUpdate = new Date();
        this.name = name;
        this.type = type;
        this.ip = ip;
        this.port = port;
        this.tps = tps;
        this.players.addAll(players);
    }
}
