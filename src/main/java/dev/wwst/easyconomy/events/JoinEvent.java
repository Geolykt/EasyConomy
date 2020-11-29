package dev.wwst.easyconomy.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.api.EasyconomyEcoAPI;

public class JoinEvent implements Listener {

    private final EasyconomyEcoAPI economy;
    private final Plugin plugin;

    public JoinEvent(@NotNull EasyconomyEcoAPI eco, @NotNull Plugin invokingPlugin) {
        this.economy = eco;
        this.plugin = invokingPlugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(@NotNull PlayerJoinEvent e) {
        if (!economy.isPlayerExisting(e.getPlayer())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                final String cmd = "eco give "+e.getPlayer().getName()+" "+plugin.getConfig().getInt("startingBalance");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);
            }, 25);
        }
    }

}
