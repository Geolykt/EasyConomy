package dev.wwst.easyconomy.commands;

import java.util.List;

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
            if (economy.getBankStorage().isAccountExisting(args[0])) {
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
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
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
                        economy.removePlayerMoney((OfflinePlayer) entity, amount);
                        now = economy.getPlayerBalance((OfflinePlayer) entity);
                    } else {
                        economy.givePlayerMoney((OfflinePlayer) entity, amount);
                        now = economy.getPlayerBalance((OfflinePlayer) entity);
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
