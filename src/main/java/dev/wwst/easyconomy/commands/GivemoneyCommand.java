package dev.wwst.easyconomy.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import dev.wwst.easyconomy.EasyConomyProvider;
import dev.wwst.easyconomy.utils.MessageTranslator;
import net.milkbowl.vault.economy.EconomyResponse;

public class GivemoneyCommand implements CommandExecutor {

    private final EasyConomyProvider economy;
    private final MessageTranslator msgTranslator;
    private final boolean isTaking;

    /**
     * Creates a new GiveMoneyCommand instance, which handles the givemoney and takemoney commands and their aliases.
     * @param eco The economy object to use
     * @param translator The message translator to use
     * @param take True if the executor should perform the takemoney command, false otherwise
     */
    public GivemoneyCommand(@NotNull EasyConomyProvider eco, @NotNull MessageTranslator translator, boolean take) {
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
            EconomyResponse respone;
            if (economy.getBankStorage().isAccountExisting(args[0])) {
                // Add balance to bank account
                if (isTaking) {
                    respone = economy.bankWithdraw(args[0], amount);
                } else {
                    respone = economy.bankDeposit(args[0], amount);
                }
            } else {
                // Add balance to (offline) player
                if (isTaking) {
                    respone = economy.withdrawPlayer(Bukkit.getOfflinePlayer(args[0]), amount);
                } else {
                    respone = economy.depositPlayer(Bukkit.getOfflinePlayer(args[0]), amount);
                }
            }
            sender.sendMessage(msgTranslator.getMessageAndReplace("eco.success",
                    true,
                    args[0],
                    economy.format(respone.amount),
                    economy.format(respone.balance)));
        } else {
            // Add balance to all selected players
            for (Entity entity : ents) {
                if (entity instanceof OfflinePlayer) {
                    EconomyResponse respone;
                    if (isTaking) {
                        respone = economy.withdrawPlayer((OfflinePlayer) entity, amount);
                    } else {
                        respone = economy.depositPlayer((OfflinePlayer) entity, amount);
                    }
                    sender.sendMessage(msgTranslator.getMessageAndReplace("eco.success",
                            true,
                            entity.getName(),
                            economy.format(respone.amount),
                            economy.format(respone.balance)));
                }
            }
        }
        return true;
    }
}
