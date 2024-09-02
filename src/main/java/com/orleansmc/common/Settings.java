package com.orleansmc.common;

import com.orleansmc.common.servers.ServerType;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

public class Settings {
    public static String SERVER_NAME = "default";
    public static ServerType SERVER_TYPE;
    public static String SERVER_IP = "0.0.0.0";
    public static int SERVER_STATE_INTERVAL = 1;
    public static int SERVER_STATE_TIMEOUT = 3;
    public static boolean IS_PROXY = false;
    public static String REDIS_HOST = "localhost";
    public static int REDIS_PORT = 6379;
    public static String REDIS_PASSWORD = null;
    public static String IN_OUT_WEBHOOK_URL = "https://discordapp.com/api/webhooks/ABBB";
    public static String ACTIONS_WEBHOOK_URL = "https://discordapp.com/api/webhooks/ABBB";

    public static void loadSettings(File file) {
        var yaml = new Yaml();
        try {
            Util.loadResourceFile(file);
            Map<String, Object> fileConfigData = yaml.load(
                    new FileInputStream(file)
            );

            SERVER_NAME = (String) fileConfigData.get("server-name");
            SERVER_IP = (String) fileConfigData.get("server-ip");
            SERVER_TYPE = ServerType.valueOf(((String) fileConfigData.get("server-type")).toUpperCase());
            IS_PROXY = (Boolean) fileConfigData.get("proxy");
            REDIS_HOST = (String) fileConfigData.get("redis-host");
            REDIS_PORT = (int) fileConfigData.get("redis-port");
            REDIS_PASSWORD = (String) fileConfigData.get("redis-password");
            SERVER_STATE_INTERVAL = (int) fileConfigData.get("server-state-interval");
            SERVER_STATE_TIMEOUT = (int) fileConfigData.get("server-state-timeout");
            IN_OUT_WEBHOOK_URL = (String) fileConfigData.get("in-out-webhook-url");
            ACTIONS_WEBHOOK_URL = (String) fileConfigData.get("actions-webhook-url");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}