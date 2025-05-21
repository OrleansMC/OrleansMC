package com.orleansmc.bukkit.players.models;

import me.lucko.helper.mongo.external.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlayerInviteModel {
    @Nullable
    public String invitedBy;
    public List<String> invitedPlayers;

    public PlayerInviteModel(@Nullable String invitedBy, List<String> invitedPlayers) {
        this.invitedBy = invitedBy;
        this.invitedPlayers = invitedPlayers;
    }

    @SuppressWarnings("unchecked")
    public static PlayerInviteModel fromDocument(Document document) {
        if (document == null) {
            return new PlayerInviteModel(null, new ArrayList<>());
        }
        return new PlayerInviteModel(
                document.get("invited_by") != null ? document.getString("invited_by") : null,
                document.get("invited_players", ArrayList.class)
        );
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("invited_by", invitedBy);
        document.put("invited_players", invitedPlayers);
        return document;
    }
}
