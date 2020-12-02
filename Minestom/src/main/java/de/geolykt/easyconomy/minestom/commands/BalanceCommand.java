package de.geolykt.easyconomy.minestom.commands;

import de.geolykt.easyconomy.minestom.EasyconomyAdvanced;
import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

@SuppressWarnings("static-access")
public class BalanceCommand extends Command {

    private final EasyconomyAdvanced extension;

    public BalanceCommand(EasyconomyAdvanced invokingExtension) {
        super("balance", "bal", "money");
        extension = invokingExtension;
        this.addSyntax(this::targettedExecution, ArgumentType.Word("target"));
        this.addSyntax(this::selfExecution);
    }

    public void selfExecution(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return;
        }
        sender.sendMessage(ChatColor.BRIGHT_GREEN + "Your balance: " + ChatColor.CYAN 
                + extension.getEconomy().format(extension.getEconomy().getPlayerBalance(((Player)sender).getUuid())));
    }

    public void targettedExecution(CommandSender sender, Arguments args) {
        Player player = MinecraftServer.getConnectionManager().getPlayer(args.getString("target"));
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "You did not specify a valid player.");
            return;
        }
        double balance = extension.getEconomy().getPlayerBalance(player.getUuid());
        if (balance == Double.NEGATIVE_INFINITY) {
            sender.sendMessage(ChatColor.RED + "The player does not have a balance yet.");
            return;
        }
        sender.sendMessage(ChatColor.BRIGHT_GREEN + "Balance of " + ChatColor.DARK_GREEN + args.getString("target")
            + ChatColor.BRIGHT_GREEN + " is " + ChatColor.CYAN + extension.getEconomy().format(balance)
            + ChatColor.BRIGHT_GREEN + ".");
    }
}
