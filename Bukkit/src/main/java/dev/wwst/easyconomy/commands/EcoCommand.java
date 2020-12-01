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

import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.utils.MessageTranslator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.api.BankStorageEngine;
import de.geolykt.easyconomy.api.EasyconomyEcoAPI;
import de.geolykt.easyconomy.api.PlayerDataStorage;

public class EcoCommand implements CommandExecutor {

    private final EasyconomyEcoAPI eco;
    private final PlayerDataStorage balanceFile;
    private final BankStorageEngine bankFile;
    private final MessageTranslator msg;
    private final String version;
    private final String permissionModify;
    private final String[] backupCMD;
    private final Easyconomy plugin;
    private final File backupDir;

    public EcoCommand(@NotNull EasyconomyEcoAPI economy, @NotNull MessageTranslator translator, @NotNull Easyconomy invokingPlugin, File backupDirectory) {
        this.eco = economy;
        this.balanceFile = economy.getPlayerDataStorage();
        this.bankFile = economy.getBankStorage();
        this.msg = translator;
        this.version = invokingPlugin.getDescription().getVersion();
        this.permissionModify = invokingPlugin.getConfig().getString("permissions.modify","");
        List<String> cmd = invokingPlugin.getConfig().getStringList("saving.backup-postrun");
        if (cmd == null || cmd.isEmpty()) {
            backupCMD = null;
        } else {
            backupCMD = cmd.toArray(new String[0]);
        }
        this.plugin = invokingPlugin;
        this.backupDir = backupDirectory;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(command.getName().equals("eco") && args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&aEasyConomy by Weiiswurst#0016, forked by Geolykt." + " Running version "+ version));
            return true;
        } else {
            if("".equals(permissionModify) && !sender.isOp() || !permissionModify.equals("") && !sender.hasPermission(permissionModify)) {
                sender.sendMessage(msg.getMessageAndReplace("general.noPerms",true,"".equals(permissionModify)?"Operator permissions":permissionModify));
                return true;
            }
            if(command.getName().equals("eco")) {
                if (args.length != 3) {
                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("help")) {
                            sender.sendMessage(msg.getMessage("eco.helpMessage",true));
                            return true;
                        } else if (args[0].equalsIgnoreCase("backup")) {
                            sender.sendMessage(msg.getMessage("backup.start", true));
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    // Save files
                                    this.balanceFile.save();
                                    this.bankFile.save();
                                    // Backup files
                                    this.balanceFile.backup(backupDir);
                                    this.bankFile.backup(backupDir);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    sender.sendMessage(msg.getMessageAndReplace("backup.ioissue", true));
                                    return;
                                }
                                if (backupCMD != null) {
                                    ProcessBuilder procBuilder = new ProcessBuilder(backupCMD).directory(backupDir).inheritIO();
                                    try {
                                        Process proc = procBuilder.start();
                                        if (!proc.waitFor(15, TimeUnit.SECONDS)) {
                                            sender.sendMessage(msg.getMessage("backup.timeout", true));
                                            proc.destroy();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                sender.sendMessage(msg.getMessage("backup.finished", true));
                            });
                            return true;
                        }
                    }
                    sender.sendMessage(msg.getMessageAndReplace("general.syntax", true, "/eco " + args[0] + " <playerName> <amount>"));
                    return true;
                } else {
                    return performOperation(sender, args[0], args[1], args[2]);
                }
            } else if(args.length != 2) {
                sender.sendMessage(msg.getMessageAndReplace("general.syntax", true, "/givemoney|takemoney|setmoney <playerName> <amount>"));
                return true;
            } else {
                return performOperation(sender, command.getName(), args[0], args[1]);
            }
        }
    }

    private boolean performOperation(CommandSender sender, String operation, String target, String amountStr) {
        @SuppressWarnings("deprecation")
        final OfflinePlayer p = Bukkit.getOfflinePlayer(target);
        if(!p.isOnline() && !p.hasPlayedBefore()) {
            sender.sendMessage(msg.getMessageAndReplace("general.noAccount",true,target));
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch(NumberFormatException e) {
            sender.sendMessage(msg.getMessageAndReplace("general.notAnumber",true,amountStr));
            return true;
        }
        double now = 0.0;
        switch (operation.toLowerCase()) {
            case "add": case "addmoney": case "give": case "givemoney":
                now = eco.givePlayerMoney(p.getUniqueId(), amount);
                break;
            case "remove": case "removemoney": case "take": case "takemoney":
                now = eco.removePlayerMoney(p.getUniqueId(),amount);
                break;
            case "set": case "setbalance": case "setmoney":
                now = amount;
                amount = Math.abs(amount - eco.setBalance(p.getUniqueId(), amount));
                break;
            default:
                sender.sendMessage(msg.getMessageAndReplace("general.syntax", true, "/eco give|take|set <playerName> <amount>"));
                return true;
        }
        sender.sendMessage(msg.getMessageAndReplace("eco.success", true, p.getName(), eco.format(amount), eco.format(now)));
        return true;
    }
}
