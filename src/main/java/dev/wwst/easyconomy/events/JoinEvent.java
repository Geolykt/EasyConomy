package dev.wwst.easyconomy.events;

import dev.wwst.easyconomy.utils.Configuration;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinEvent implements Listener {

    private final Economy economy;
    private final JavaPlugin plugin;

    public JoinEvent(Economy eco, JavaPlugin invokingPlugin) {
        this.economy = eco;
        this.plugin = invokingPlugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        if (!economy.hasAccount(e.getPlayer())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                final String cmd = "eco give "+e.getPlayer().getName()+" "+Configuration.get().getInt("startingBalance");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);
            }, 25);
        }
    }

}
