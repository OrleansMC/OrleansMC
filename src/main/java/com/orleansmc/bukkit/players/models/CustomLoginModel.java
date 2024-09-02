package com.orleansmc.bukkit.players.models;

import me.lucko.helper.mongo.external.bson.Document;
import org.bukkit.Sound;

public class CustomLoginModel {
    public Sound customLoginSound;
    public String message;

    public CustomLoginModel(String message, Sound customLoginSound) {
        this.customLoginSound = customLoginSound;
        this.message = message;
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("custom_login_sound", customLoginSound != null ? customLoginSound.name() : null);
        document.put("message", message);
        return document;
    }

    public static CustomLoginModel fromDocument(Document document) {
        if (document == null) {
            return null;
        }
        return new CustomLoginModel(
                document.getString("message"),
                document.getString("custom_login_sound") != null ? Sound.valueOf(document.getString("custom_login_sound")) : null
        );
    }
}
