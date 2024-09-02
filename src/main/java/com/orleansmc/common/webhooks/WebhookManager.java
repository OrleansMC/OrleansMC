package com.orleansmc.common.webhooks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebhookManager implements WebhookProvider {
    public final ScheduledExecutorService scheduler;
    private final ArrayList<DiscordWebhook> webhookQueue = new ArrayList<>();

    public WebhookManager() {
        scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            if (webhookQueue.isEmpty()) {
                return;
            }
            DiscordWebhook webhook = webhookQueue.remove(0);
            try {
                webhook.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 600L, 600L, TimeUnit.MILLISECONDS);
    }

    public void sendWebhook(DiscordWebhook webhook) {
        webhookQueue.add(webhook);
    }
}
