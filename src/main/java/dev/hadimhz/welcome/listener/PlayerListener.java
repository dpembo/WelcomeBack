package dev.hadimhz.welcome.listener;

import dev.hadimhz.welcome.config.Config;
import dev.hadimhz.welcome.util.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerListener implements Listener {

    private final Map<UUID, Long> userJoinTimer = new HashMap<>();
    private final Set<UUID> welcomed;
    private final Config config;
    private final Logger logger;
    private long joinedAt;
    private Player player;


    public PlayerListener(Plugin plugin, Config config) {

        this.config = config;
        this.logger = plugin.getLogger();

        this.welcomed = new HashSet<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        final UUID uuid = event.getPlayer().getUniqueId();
        final long elapsed = (System.currentTimeMillis() - userJoinTimer.getOrDefault(uuid, 0L)) / 1000;
        final boolean withinDelay = config.onlyWelcomeAfterXDelay != -1 && elapsed < config.onlyWelcomeAfterXDelay;
        final boolean vanished = isVanished(event.getPlayer());

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(() -> String.format(
                    "%s rejoined after %ds (delay threshold=%d, withinDelay=%b, vanished=%b)",
                    event.getPlayer().getName(), elapsed, config.onlyWelcomeAfterXDelay, withinDelay, vanished));
        }

        if (vanished || withinDelay) {
            logger.fine(() -> "Skipping welcome message for " + event.getPlayer().getName()
                    + " (vanished=" + vanished + ", withinDelay=" + withinDelay + ")");
            return;
        }

        logger.fine(() -> "Broadcasting welcome message for " + event.getPlayer().getName());

        player = event.getPlayer();

        welcomed.clear();
        joinedAt = System.currentTimeMillis();


        if (config.onPlayerJoin.isEmpty()) return;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

            if (onlinePlayer.getUniqueId().equals(uuid)) continue;

            onlinePlayer.sendMessage(Chat.translate(config.onPlayerJoin.replaceAll("%player%", player.getName())));
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        userJoinTimer.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }


    public Set<UUID> getWelcomed() {
        return welcomed;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public Player getPlayer() {
        return player;
    }

    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

}
