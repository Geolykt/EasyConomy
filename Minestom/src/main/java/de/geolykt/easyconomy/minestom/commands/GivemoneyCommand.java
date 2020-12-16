package de.geolykt.easyconomy.minestom.commands;

import de.geolykt.easyconomy.api.Bank;
import de.geolykt.easyconomy.minestom.EasyconomyAdvanced;
import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

@SuppressWarnings("static-access")
public class GivemoneyCommand extends Command {

    private final EasyconomyAdvanced extension;
    private final String perm;

    private final String unpermitted;
    private final String playerNotFound;

   public GivemoneyCommand(EasyconomyAdvanced invokingExtension) {
        super("givemoney", "givebal", "addmoney", "addbal");
        extension = invokingExtension;
        perm = extension.getConfig().getAdminPermission();
        unpermitted = extension.getConfig().getNotPermitted();
        playerNotFound = extension.getConfig().getNotAPlayer();
        addSyntax(this::handleCommand, ArgumentType.Word("target"), ArgumentType.Double("amount"));
    }

    public void handleCommand(CommandSender sender, Arguments args) {
        if (!sender.hasPermission(perm) && !sender.isConsole()) {
            sender.sendMessage(unpermitted);
            return;
        }
        String target = args.getWord("target");
        double money = args.getDouble("amount");

        Bank bank = extension.getEconomy().getBank(target);
        if (bank == null) {
            // Give money to player
            Player player = MinecraftServer.getConnectionManager().getPlayer(target);
            if (player == null || !extension.getEconomy().isPlayerExisting(player.getUuid())) {
                sender.sendMessage(playerNotFound);
                return;
            }
            double now = extension.getEconomy().givePlayerMoney(player.getUuid(), money);
            sender.sendMessage(ChatColor.BRIGHT_GREEN + "Gave " + ChatColor.CYAN + extension.getEconomy().format(money)
                    + ChatColor.BRIGHT_GREEN + " to " + ChatColor.DARK_BLUE + target 
                    + ChatColor.BRIGHT_GREEN + ". The player now has " + extension.getEconomy().format(now)
                    + ChatColor.BRIGHT_GREEN + ".");
        } else {
            bank.addMoney(money);
            sender.sendMessage(ChatColor.BRIGHT_GREEN + "Gave " + ChatColor.CYAN + extension.getEconomy().format(money)
                    + ChatColor.BRIGHT_GREEN + " to the bank " + ChatColor.DARK_BLUE + target 
                    + ChatColor.BRIGHT_GREEN + ". It now has " + extension.getEconomy().format(bank.getMoney())
                    + ChatColor.BRIGHT_GREEN + ".");
        }
    }
}
