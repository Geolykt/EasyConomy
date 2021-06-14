package de.geolykt.easyconomy.minestom.commands;

import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.api.Bank;
import de.geolykt.easyconomy.minestom.EasyconomyAdvanced;
import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.entity.Player;

@SuppressWarnings("static-access") // Temporary thing until we get some things working
public class GivemoneyCommand extends Command {

    private final @NotNull EasyconomyAdvanced extension;
    private final @NotNull String perm;

    private final @NotNull String unpermitted;
    private final @NotNull String playerNotFound;

    private static final @NotNull Argument<String> TARGET = new ArgumentWord("target");
    private static final @NotNull Argument<Double> AMOUNT = new ArgumentDouble("amount");

    public GivemoneyCommand(@NotNull EasyconomyAdvanced invokingExtension) {
        super("givemoney", "givebal", "addmoney", "addbal");
        extension = invokingExtension;
        perm = extension.getConfig().getAdminPermission();
        unpermitted = extension.getConfig().getNotPermitted();
        playerNotFound = extension.getConfig().getNotAPlayer();
        addSyntax(this::handleCommand, TARGET, AMOUNT);
    }

    public void handleCommand(@NotNull CommandSender sender, @NotNull CommandContext args) {
        if (!sender.hasPermission(perm) && !sender.isConsole()) {
            sender.sendMessage(unpermitted);
            return;
        }
        String target = args.get(TARGET);
        if (target == null) {
            throw new IllegalStateException("Somehow obtained a null target.");
        }
        double money = args.get(AMOUNT);

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
