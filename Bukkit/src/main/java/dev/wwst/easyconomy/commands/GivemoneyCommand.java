/*
 * EasyconomyAdvanced, a lightweight economy plugin
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

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.api.EasyconomyEcoAPI;
import dev.wwst.easyconomy.utils.MessageTranslator;

public class GivemoneyCommand implements CommandExecutor {

    private final EasyconomyEcoAPI economy;
    private final MessageTranslator msgTranslator;
    private final boolean isTaking;

    /**
     * Creates a new GiveMoneyCommand instance, which handles the givemoney and takemoney commands and their aliases.
     * @param eco The economy object to use
     * @param translator The message translator to use
     * @param take True if the executor should perform the takemoney command, false otherwise
     */
    public GivemoneyCommand(@NotNull EasyconomyEcoAPI eco, @NotNull MessageTranslator translator, boolean take) {
        economy = eco;
        msgTranslator = translator;
        isTaking = take;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            if (isTaking) {
                sender.sendMessage(msgTranslator.getMessageAndReplace("general.syntax", true, "/takemoney <playerName> <amount>"));
            } else {
                sender.sendMessage(msgTranslator.getMessageAndReplace("general.syntax", true, "/givemoney <playerName> <amount>"));
            }
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(msgTranslator.getMessageAndReplace("general.notAnumber", true, args[1]));
            return true;
        }
        List<Entity> ents = Bukkit.selectEntities(sender, args[0]);
        if (ents.size() == 0) {
            final double now;
            if (economy.getBankStorage().has(args[0])) {
                // Add balance to bank account
                if (isTaking) {
                    economy.removeBankMoney(args[0], amount);
                    now = economy.getBankBalance(args[0]);
                } else {
                    economy.giveBankMoney(args[0], amount);
                    now = economy.getBankBalance(args[0]);
                }
            } else {
                // Add balance to (offline) player
                UUID player = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
                if (isTaking) {
                    economy.removePlayerMoney(player, amount);
                    now = economy.getPlayerBalance(player);
                } else {
                    economy.givePlayerMoney(player, amount);
                    now = economy.getPlayerBalance(player);
                }
            }
            sender.sendMessage(msgTranslator.getMessageAndReplace("eco.success",
                    true,
                    args[0],
                    economy.format(amount),
                    economy.format(now)));
        } else {
            // Add balance to all selected players
            for (Entity entity : ents) {
                if (entity instanceof OfflinePlayer) {
                    final double now;
                    if (isTaking) {
                        economy.removePlayerMoney(entity.getUniqueId(), amount);
                        now = economy.getPlayerBalance(entity.getUniqueId());
                    } else {
                        economy.givePlayerMoney(entity.getUniqueId(), amount);
                        now = economy.getPlayerBalance(entity.getUniqueId());
                    }
                    sender.sendMessage(msgTranslator.getMessageAndReplace("eco.success",
                            true,
                            args[0],
                            economy.format(amount),
                            economy.format(now)));
                }
            }
        }
        return true;
    }
}
