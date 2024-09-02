package com.orleansmc.bukkit.mongo;

import lombok.Getter;
import me.lucko.helper.mongo.MongoProvider;
import me.lucko.helper.mongo.external.mongodriver.client.MongoDatabase;
import org.bukkit.Bukkit;

@Getter
public class MongoManager implements com.orleansmc.bukkit.mongo.MongoProvider {
    private final MongoDatabase minecraftDatabase;
    private final MongoDatabase webDatabase;

    public MongoManager() {
        final MongoProvider mongoProvider = Bukkit.getServer().getServicesManager().load(MongoProvider.class);
        if (mongoProvider == null) {
            throw new RuntimeException("MongoProvider service not found");
        }

        this.minecraftDatabase = mongoProvider.getMongo().getDatabase("minecraft");
        this.webDatabase = mongoProvider.getMongo().getDatabase("website");
    }
}
