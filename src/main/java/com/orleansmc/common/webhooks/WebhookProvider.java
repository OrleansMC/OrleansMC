package com.orleansmc.common.webhooks;

public interface WebhookProvider {
    public void sendWebhook(DiscordWebhook webhook);
}
