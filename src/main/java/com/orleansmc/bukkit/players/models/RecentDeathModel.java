package com.orleansmc.bukkit.players.models;
import me.lucko.helper.mongo.external.bson.Document;

import java.util.Date;

public class RecentDeathModel {
    public String location;
    public String server;
    public Date date;
    public double backPriceMultiplier;

    public RecentDeathModel(String location, String server, Date date, double backPriceMultiplier) {
        this.location = location;
        this.server = server;
        this.date = date;
        this.backPriceMultiplier = backPriceMultiplier;
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("location", location);
        document.put("server", server);
        document.put("date", date);
        document.put("back_price_multiplier", backPriceMultiplier);
        return document;
    }

    public static RecentDeathModel fromDocument(Document document) {
        if (document == null) {
            return new RecentDeathModel(
                    null,
                    null,
                    null,
                    0
            );
        }
        return new RecentDeathModel(
                document.getString("location"),
                document.getString("server"),
                document.getDate("date"),
                document.getDouble("back_price_multiplier")
        );
    }
}
