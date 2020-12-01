/*
 * EasyconomyAdvanced, a lightweight economy plugin
 * Copyright (C) Weiiswurst
 * Copyright (C) Geolykt (<https://geolykt.de>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
        if (!economy.isPlayerExisting(e.getPlayer().getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                final String cmd = "eco give "+e.getPlayer().getName()+" "+plugin.getConfig().getInt("startingBalance");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);
            }, 25);
        }
    }

}
