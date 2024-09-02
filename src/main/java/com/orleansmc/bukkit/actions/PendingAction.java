package com.orleansmc.bukkit.actions;

import com.orleansmc.common.servers.ServerType;
import me.lucko.helper.mongo.external.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public record PendingAction(
        ActionType type,
        String command,
        String reason,
        @Nullable
        ServerType serverType,
        Date date
) {
    public Document toDocument() {
        Document document = new Document();
        document.put("type", type.name());
        document.put("command", command);
        document.put("reason", reason);
        document.put("server_type", serverType == null ? null : serverType.name());
        document.put("date", date);
        return document;
    }

    public static PendingAction fromDocument(Document document) {
        return new PendingAction(
                ActionType.valueOf(document.getString("type")),
                document.getString("command"),
                document.getString("reason"),
                document.getString("server_type") == null ? null : ServerType.valueOf(document.getString("server_type")),
                document.getDate("date")
        );
    }
}
