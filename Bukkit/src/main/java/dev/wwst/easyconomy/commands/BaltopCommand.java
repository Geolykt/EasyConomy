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
package dev.wwst.easyconomy.commands;

import dev.wwst.easyconomy.utils.MessageTranslator;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.api.EasyconomyEcoAPI;
import de.geolykt.easyconomy.api.PlayerDataStorage;

import java.util.Map;
import java.util.UUID;

public class BaltopCommand implements CommandExecutor {

    private final MessageTranslator msg;
    private final PlayerDataStorage pds;
    private final EasyconomyEcoAPI eco;

    public BaltopCommand(@NotNull EasyconomyEcoAPI economy, @NotNull MessageTranslator translator) {
        msg = translator;
        eco = economy;
        pds = economy.getPlayerDataStorage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(pds.getBaltop() == null || pds.getBaltop().size() == 0) {
            sender.sendMessage(msg.getMessage("baltop.none", true));
            return true;
        }

        StringBuilder message = new StringBuilder(msg.getMessage("baltop.start",true));
        message.append("\n"); // newline
        for(Map.Entry<UUID, Double> entry : pds.getBaltop().entrySet()) {
            if(Bukkit.getOfflinePlayer(entry.getKey()).getName() == null) {
                message.append("Invalid entry: ").append(entry.getKey().toString());
            } else {
                message.append(msg.getMessageAndReplace("baltop.value", false, eco.format(entry.getValue()), Bukkit.getOfflinePlayer(entry.getKey()).getName()));
                message.append("\n");
            }
        }

        sender.sendMessage(message.toString());

        return true;
    }
}