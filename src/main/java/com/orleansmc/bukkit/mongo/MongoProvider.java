package com.orleansmc.bukkit.mongo;

import me.lucko.helper.mongo.external.mongodriver.client.MongoDatabase;

public interface MongoProvider {
    public MongoDatabase getMinecraftDatabase();
    public MongoDatabase getWebDatabase();
}
