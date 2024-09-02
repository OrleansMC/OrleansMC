package com.orleansmc.bukkit.players.models;

import me.lucko.helper.mongo.external.bson.Document;

public class PlayerAlertModel {
    public final String name;
    public boolean sent;
    public int reset;

    public PlayerAlertModel(String name, boolean sent, int reset) {
        this.name = name;
        this.sent = sent;
        this.reset = reset;
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("name", name);
        document.put("sent", sent);
        document.put("reset", reset);
        return document;
    }

    public static PlayerAlertModel fromDocument(Document document) {
        return new PlayerAlertModel(
                document.getString("name"),
                document.getBoolean("sent"),
                document.getInteger("reset")
        );
    }
}
