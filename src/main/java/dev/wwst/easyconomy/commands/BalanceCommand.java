package dev.wwst.easyconomy.commands;

import dev.wwst.easyconomy.utils.Configuration;
import dev.wwst.easyconomy.utils.MessageTranslator;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand implements CommandExecutor {

    private final Economy eco;
    private final MessageTranslator msg;

    public BalanceCommand(@NotNull Economy economy, @NotNull MessageTranslator translator) {
        eco = economy;
        msg = translator;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            if(args.length != 1) {
                sender.sendMessage(msg.getMessageAndReplace("general.syntax",true,"/bal <playerName>"));
                return true;
            }
            sendBalanceOfOther(sender,args[0]);
            return true;
        }

        String permission = Configuration.get().getString("permissions.balance", "");
        if(!"".equals(permission) && !sender.hasPermission(permission)) {
            sender.sendMessage(msg.getMessageAndReplace("general.noPerms", true, permission));
            return true;
        }

        Player p = (Player) sender;
        if(args.length == 0) {
            p.sendMessage(msg.getMessageAndReplace("balance.ofSelf", true, eco.format(eco.getBalance(p))));
        } else if(args.length == 1) {
            String otherBalancePerm = Configuration.get().getString("permissions.othersBalance","");
            if(!"".equals(otherBalancePerm) && !sender.hasPermission(otherBalancePerm)) {
                sender.sendMessage(msg.getMessageAndReplace("general.noPerms",true,otherBalancePerm));
                return true;
            }
            sendBalanceOfOther(sender,args[0]);
        } else {
            sender.sendMessage(msg.getMessageAndReplace("general.syntax",true,"/bal <playerName>"));
        }
        return true;
    }

    private void sendBalanceOfOther(@NotNull CommandSender sender, @NotNull String otherName) {
        @SuppressWarnings("deprecation")
        OfflinePlayer p = Bukkit.getOfflinePlayer(otherName);
        if(!p.hasPlayedBefore() || !eco.hasAccount(p)) {
            sender.sendMessage(msg.getMessageAndReplace("general.noAccount",true,otherName));
        } else {
            sender.sendMessage(msg.getMessageAndReplace("balance.ofOther", true, p.getName(), eco.format(eco.getBalance(p))));
        }
    }
}
