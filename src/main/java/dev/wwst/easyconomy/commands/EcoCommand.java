package dev.wwst.easyconomy.commands;

import dev.wwst.easyconomy.EasyConomyProvider;
import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.storage.BinaryAccountStoarge;
import dev.wwst.easyconomy.storage.PlayerDataStorage;
import dev.wwst.easyconomy.utils.MessageTranslator;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

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

public class EcoCommand implements CommandExecutor {

    private final Economy eco;
    private final PlayerDataStorage balanceFile;
    private final BinaryAccountStoarge bankFile;
    private final MessageTranslator msg;
    private final String version;
    private final String permissionModify;
    private final String[] backupCMD;
    private final Easyconomy plugin;
    private final File backupDir;

    public EcoCommand(@NotNull EasyConomyProvider economy, @NotNull MessageTranslator translator, @NotNull Easyconomy invokingPlugin, File backupDirectory) {
        this.eco = economy;
        this.balanceFile = economy.getStorage();
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
        final double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch(NumberFormatException e) {
            sender.sendMessage(msg.getMessageAndReplace("general.notAnumber",true,amountStr));
            return true;
        }
        final EconomyResponse res;
        switch (operation.toLowerCase()) {
            case "add": case "addmoney": case "give": case "givemoney":
                res = eco.depositPlayer(p,amount);
                break;
            case "remove": case "removemoney": case "take": case "takemoney":
                res = eco.withdrawPlayer(p,amount);
                break;
            case "set": case "setbalance": case "setmoney":
                eco.withdrawPlayer(p,eco.getBalance(p));
                res = eco.depositPlayer(p,amount);
                break;
            default:
                res = null;
                break;
        }
        if(res == null)
            sender.sendMessage(msg.getMessageAndReplace("general.syntax",true,"/eco give|take|set <playerName> <amount>"));
        else
            sender.sendMessage(msg.getMessageAndReplace("eco.success",true,p.getName(),eco.format(res.amount),eco.format(res.balance)));
        return true;
    }
}
