package com.orleansmc.common.servers;

import java.util.HashMap;

public interface ServersProvider {
    public void updateServerState(ServerState serverState);
    public void sendServerState();
    public ServerState getAnyServer();
    public ServerState getAvailableServerByType(ServerType serverType);
    public HashMap<String, ServerState> getServerStates();
    public void switchServer(String playerName, String serverName);
}