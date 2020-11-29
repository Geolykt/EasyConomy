package dev.wwst.easyconomy.commands;

import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.utils.MessageTranslator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.api.EasyconomyEcoAPI;

public class PayCommand implements CommandExecutor {


    private final EasyconomyEcoAPI eco;
    private final MessageTranslator msg;

    private final double minimumTransactionAmount;

    public PayCommand(@NotNull EasyconomyEcoAPI economy, @NotNull MessageTranslator translator, @NotNull Easyconomy plugin) {
        eco = economy;
        msg = translator;
        minimumTransactionAmount = plugin.getConfig().getDouble("minimumTransactionAmount",0.1d);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(msg.getMessage("general.playerOnly",true));
            return true;
        }
        if(args.length != 2) {
            sender.sendMessage(msg.getMessageAndReplace("general.syntax",true,"/pay <target> <amount>"));
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1].replaceAll(",","."));
        } catch(NumberFormatException e) {
            sender.sendMessage(msg.getMessageAndReplace("general.notAnumber",true,args[1]));
            return true;
        }
        amount = Math.abs(amount);
        if(amount < minimumTransactionAmount) {
            sender.sendMessage(msg.getMessageAndReplace("pay.amountTooSmall",true,eco.format(minimumTransactionAmount)));
            return true;
        }
        final Player p = (Player) sender;
        if(args[0].equalsIgnoreCase(p.getName())) {
            sender.sendMessage(msg.getMessage("pay.self",true));
            return true;
        }
        if(eco.getPlayerBalance(p.getUniqueId()) < amount) {
            sender.sendMessage(msg.getMessageAndReplace("general.insufficientFunds",true,eco.format(amount-eco.getPlayerBalance(p.getUniqueId()))));
            return true;
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if(!eco.isPlayerExisting(target.getUniqueId())) {
            sender.sendMessage(msg.getMessageAndReplace("general.noAccount",true,args[0]));
            return true;
        }
        eco.transferBalance(p.getUniqueId(), target.getUniqueId(), amount);
        p.sendMessage(msg.getMessageAndReplace("pay.you",true,target.getName(),eco.format(amount),eco.format(eco.getPlayerBalance(p.getUniqueId()))));
        if(target.getPlayer() != null) {
            target.getPlayer().sendMessage(msg.getMessageAndReplace("pay.target", true, p.getName(), eco.format(amount), eco.format(eco.getPlayerBalance(target.getUniqueId()))));
        }
        return true;
    }
}