package dev.wwst.easyconomy.events;

import dev.wwst.easyconomy.utils.Configuration;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class JoinEvent implements Listener {

    private final Economy economy;
    private final Plugin plugin;

    public JoinEvent(@NotNull Economy eco, @NotNull Plugin invokingPlugin) {
        this.economy = eco;
        this.plugin = invokingPlugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(@NotNull PlayerJoinEvent e) {
        if (!economy.hasAccount(e.getPlayer())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                final String cmd = "eco give "+e.getPlayer().getName()+" "+Configuration.get().getInt("startingBalance");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);
            }, 25);
        }
    }

}
