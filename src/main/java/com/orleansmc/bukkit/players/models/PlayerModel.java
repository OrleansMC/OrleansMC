package com.orleansmc.bukkit.players.models;

import me.lucko.helper.mongo.external.bson.Document;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerModel {
    public final String name;
    public final String uuid;
    public int totalDeletedRealms = 0;
    public final PlayerInviteModel invites;
    public final List<PlayerAlertModel> alerts;
    public List<RecentDeathModel> recentDeaths;
    @Nullable
    public CustomLoginModel customLogin;

    public PlayerModel(
            String name,
            String uuid,
            int totalDeletedRealms,
            ArrayList<PlayerAlertModel> alerts,
            ArrayList<RecentDeathModel> recentDeaths,
            @Nullable CustomLoginModel customLogin,
            @Nullable PlayerInviteModel invites
    ) {
        this.name = name;
        this.uuid = uuid;
        this.alerts = alerts;
        this.totalDeletedRealms = totalDeletedRealms;
        this.customLogin = customLogin;
        this.recentDeaths = recentDeaths;
        this.invites = invites != null ? invites : new PlayerInviteModel(null, new ArrayList<>());
    }

    public Document toDocument() {
        recentDeaths.sort((o1, o2) -> o2.date.compareTo(o1.date));
        recentDeaths = new ArrayList<>(recentDeaths.subList(0, Math.min(recentDeaths.size(), 5)));

        Document document = new Document();
        document.put("name", name);
        document.put("uuid", uuid);
        document.put("total_deleted_realms", totalDeletedRealms);
        document.put("invites", invites.toDocument());
        document.put("alerts", alerts.stream().map(PlayerAlertModel::toDocument).toList());
        document.put("custom_login", customLogin != null ? customLogin.toDocument() : null);
        document.put("recent_deaths", recentDeaths.stream().map(RecentDeathModel::toDocument).toList());
        return document;
    }

    public static PlayerModel fromDocument(Document document) {
        if (document == null) {
            return null;
        }
        ArrayList<PlayerAlertModel> alerts = new ArrayList<>();
        for (Object o : (List<?>) document.getOrDefault("alerts", new ArrayList<>())) {
            alerts.add(PlayerAlertModel.fromDocument((Document) o));
        }
        ArrayList<RecentDeathModel> recentDeathModels = new ArrayList<>();
        for (Object o : (List<?>) document.getOrDefault("recent_deaths", new ArrayList<>())) {
            recentDeathModels.add(RecentDeathModel.fromDocument((Document) o));
        }
        recentDeathModels.sort((o1, o2) -> o2.date.compareTo(o1.date));
        recentDeathModels = new ArrayList<>(recentDeathModels.subList(0, Math.min(recentDeathModels.size(), 5)));
        return new PlayerModel(
                document.getString("name"),
                document.getString("uuid"),
                document.getInteger("total_deleted_realms"),
                alerts,
                recentDeathModels,
                CustomLoginModel.fromDocument((Document) document.getOrDefault("custom_login", null)),
                PlayerInviteModel.fromDocument((Document) document.getOrDefault("invites", null))
        );
    }
}
