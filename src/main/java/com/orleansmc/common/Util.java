package com.orleansmc.common;

import com.orleansmc.common.servers.ServerState;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.common.servers.ServersProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

public class Util {
    private static org.slf4j.Logger velocityLogger;
    private static java.util.logging.Logger bukkitLogger;

    public static void loadResourceFile(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            try (InputStream stream = Settings.class.getClassLoader().getResourceAsStream(file.getName());
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                if (stream == null) {
                    throw new IOException("Cannot find resource file " + file.getName());
                }
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                info("Loaded resource file " + file.getName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void setVelocityLogger(org.slf4j.Logger logger) {
        velocityLogger = logger;
    }

    public static void setBukkitLogger(java.util.logging.Logger logger) {
        bukkitLogger = logger;
    }

    public static void info(String message) {
        if (velocityLogger != null) {
            velocityLogger.info(message);
        } else if (bukkitLogger != null) {
            bukkitLogger.info(message);
        } else {
            System.out.println(message);
        }
    }

    public static Component getComponent(String text) {
        final MiniMessage mm = MiniMessage.miniMessage();
        return mm.deserialize(text);
    }

    public static String getString(Component component) {
        final MiniMessage mm = MiniMessage.miniMessage();
        return mm.serialize(component);
    }
}
