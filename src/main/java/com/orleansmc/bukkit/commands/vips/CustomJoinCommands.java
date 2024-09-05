package com.orleansmc.bukkit.commands.vips;

import com.orleansmc.bukkit.OrleansMC;
import com.orleansmc.bukkit.players.models.CustomLoginModel;
import com.orleansmc.bukkit.players.models.PlayerModel;
import com.orleansmc.bukkit.players.models.PlayerAlertModel;
import com.orleansmc.common.Settings;
import com.orleansmc.common.Util;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.common.webhooks.DiscordWebhook;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.ChannelAgent;
import me.lucko.helper.messaging.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import me.clip.placeholderapi.PlaceholderAPI;

import java.awt.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CustomJoinCommands implements Listener {
    private static class JoinNotification {
        public String message;
        public Sound sound;

        public JoinNotification(String message, Sound sound) {
            this.message = message;
            this.sound = sound;
        }
    }

    public static ChannelAgent<JoinNotification> channelAgent;

    public static void setup(OrleansMC plugin) {
        final Messenger messenger = Bukkit.getServer().getServicesManager().load(Messenger.class);
        if (messenger == null) {
            throw new RuntimeException("Messenger service not found");
        }
        final Channel<JoinNotification> channel = messenger.getChannel("orleans:join_messages", JoinNotification.class);
        channelAgent = channel.newAgent();

        List<String> onlinePlayers = new ArrayList<>();
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            onlinePlayers.clear();
            plugin.serversManager.serverStates.forEach((server, serverState) -> {
                for (String player : serverState.players) {
                    if (!onlinePlayers.contains(player)) {
                        onlinePlayers.add(player);
                    }
                }
            });
        }, 0, 20 * 10);

        channelAgent.addListener(
                (agent, data) -> {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage(Util.getComponent(data.message));
                        player.playSound(player.getLocation(), data.sound, 1, 1);
                    });
                }
        );

        HashMap<String, String> joinSounds = new HashMap<>();
        joinSounds.put("Mızrak Sesi", "minecraft:item.trident.thunder");
        joinSounds.put("Kötü Şans Sesi", "minecraft:event.mob_effect.bad_omen");
        joinSounds.put("Baskın Sesi", "minecraft:event.mob_effect.raid_omen");
        joinSounds.put("Kılıç Sesi", "minecraft:tugkandeman.ent_slash3");
        joinSounds.put("Amethyst Bloğu Sesi", "minecraft:block.amethyst_block.fall");
        joinSounds.put("Örs Düşüşü Sesi", "minecraft:block.anvil.land");
        joinSounds.put("Kasa Açılış Sesi", "minecraft:block.vault.open_shutter");
        joinSounds.put("Uğursuz Eşya Sesi", "minecraft:block.trial_spawner.about_to_spawn_item");
        joinSounds.put("Kehanet Çağrısı Sesi", "minecraft:block.trial_spawner.charge_activate");
        joinSounds.put("End Portalı Sesi", "minecraft:block.end_portal.spawn");
        joinSounds.put("Ender Gözü Sesi", "minecraft:block.end_portal_frame.fill");
        joinSounds.put("Dirilme Sesi", "minecraft:block.respawn_anchor.charge");
        joinSounds.put("Diriliş Sesi", "minecraft:block.respawn_anchor.set_spawn");
        joinSounds.put("Diriliş Bitiş Sesi", "minecraft:block.respawn_anchor.deplete");

        Commands.create()
                .assertPlayer()
                .tabHandler(c -> {
                    if (c.args().size() == 1) {
                        return new ArrayList<>(joinSounds.keySet()
                                .stream().filter(s -> s.toLowerCase().startsWith(c.args().get(0).toLowerCase()))
                                .toList());
                    }
                    return List.of();
                })
                .handler(c -> {
                    Player player = c.sender();
                    if (!player.hasPermission("orleansmc.custom_join_sound")) {
                        player.sendMessage("§cBu komutu kullanmak için VIP olmalısınız.");
                        return;
                    }
                    PlayerModel playerModel = plugin.playersManager.getPlayer(player.getName());
                    if (playerModel == null) {
                        player.sendMessage("§cBir hata oluştu. Lütfen tekrar deneyin.");
                        return;
                    }

                    String soundName = c.args().stream().reduce((s1, s2) -> s1 + " " + s2).orElse("minecraft:block.note_block.pling");
                    String soundValue = joinSounds.get(soundName);
                    if (soundValue == null) {
                        player.sendMessage("§cGeçersiz ses adı. Lütfen doğru bir ses adı girin.");
                        return;
                    }
                    Sound sound = Sound.valueOf(soundValue);
                    if (playerModel.customLogin == null) {
                        playerModel.customLogin = new CustomLoginModel("sunucuya katıldı!", sound);
                    } else {
                        playerModel.customLogin.customLoginSound = sound;
                    }
                    plugin.playersManager.savePlayer(playerModel);
                    player.sendMessage("§aGiriş sesiniz başarıyla güncellendi.");
                })
                .register("join-sound", "giriş-sesi");

        Commands.create()
                .assertPlayer()
                .assertPermission("orleansmc.custom_join_message")
                .tabHandler(c -> {
                    if (c.args().size() == 1) {
                        return Arrays.asList("sunucuya katıldı!", "kayarak indi!", "hoş geldin!", "iyi oyunlar!");
                    }
                    return List.of();
                })
                .handler(c -> {
                    Player player = c.sender();
                    if (!player.hasPermission("orleansmc.custom_join_message")) {
                        player.sendMessage("§cBu komutu kullanmak için VIP olmalısınız.");
                        return;
                    }

                    PlayerModel playerModel = plugin.playersManager.getPlayer(player.getName());
                    if (playerModel == null) {
                        player.sendMessage("§cBir hata oluştu. Lütfen tekrar deneyin.");
                        return;
                    }

                    String message = c.args().stream().reduce((s1, s2) -> s1 + " " + s2).orElse("sunucuya katıldı!");
                    if (playerModel.customLogin == null) {
                        playerModel.customLogin = new CustomLoginModel(message, Sound.BLOCK_NOTE_BLOCK_PLING);
                    } else {
                        playerModel.customLogin.message = message;
                    }
                    plugin.playersManager.savePlayer(playerModel);

                    player.sendMessage("§aGiriş mesajınız başarıyla güncellendi.");
                }).register("join-message", "giriş-mesajı");

        Events.subscribe(PlayerJoinEvent.class).handler(e -> {
            Player player = e.getPlayer();
            if (Settings.SERVER_TYPE != ServerType.REALMS_SPAWN) {
                return;
            }
            if (onlinePlayers.contains(player.getName())) {
                return;
            }
            plugin.getLogger().info("Player " + player.getName() + " joined for the first time.");

            showAllayWelcome(player, plugin);
            if (player.hasPermission("orleansmc.join_notification")) {
                sendWelcomeMessage(player, plugin);
            }
        }).bindWith(plugin);
    }

    private static void showAllayWelcome(Player player, OrleansMC plugin) {
        plugin.playersManager.waitPlayerDataThenRun(player, playerModel -> {
            String alertKey = "welcome";
            PlayerAlertModel alert = playerModel.alerts.stream().filter(a -> a.name.equals(alertKey)).findFirst().orElse(null);
            if (alert != null) {
                return;
            }
            playerModel.alerts.add(new PlayerAlertModel(alertKey, true, (int) Double.POSITIVE_INFINITY));
            plugin.playersManager.savePlayer(playerModel);

            Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "allay-show-up " + player.getName() + " WELCOME"
            );
        }, 0);
    }

    private static void sendWelcomeMessage(Player player, OrleansMC plugin) {
        plugin.playersManager.waitPlayerDataThenRun(player, playerModel -> {
            plugin.getLogger().info("Sending join notification for " + player.getName());
            CustomLoginModel customLogin = playerModel.customLogin;
            Sound sound = customLogin != null && player.hasPermission("orleansmc.custom_join_sound")
                    ? customLogin.customLoginSound : Sound.BLOCK_NOTE_BLOCK_PLING;

            String prefix = PlaceholderAPI.setPlaceholders(player, "%luckperms_meta_chatprefix%");
            String message = player.hasPermission("orleansmc.custom_join_message") && customLogin != null ? customLogin.message : "sunucuya katıldı!";
            String finalMessage = prefix + " " + player.getName() + " " + message;
            player.playSound(player.getLocation(), sound, 1, 1);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> channelAgent.getChannel().sendMessage(new JoinNotification(finalMessage, sound)));
        }, 0);
    }
}
