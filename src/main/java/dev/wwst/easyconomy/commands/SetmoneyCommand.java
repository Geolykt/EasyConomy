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

public class SetmoneyCommand implements CommandExecutor {

    private final EasyconomyEcoAPI economy;
    private final MessageTranslator msgTranslator;

    /**
     * Creates a new SetmoneyCommand instance, which handles the setmoney command and it's aliases.
     * @param eco The economy object to use
     * @param translator The message translator to use
     */
    public SetmoneyCommand(@NotNull EasyconomyEcoAPI eco, @NotNull MessageTranslator translator) {
        economy = eco;
        msgTranslator = translator;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            sender.sendMessage(msgTranslator.getMessageAndReplace("general.syntax", true, "/setmoney <playerName> <amount>"));
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
            if (economy.setBalance(args[0], amount) == Double.NEGATIVE_INFINITY) {
                // Set balance of (offline) player instead since there was no given bank balance
                economy.setBalance(Bukkit.getOfflinePlayer(args[0]).getUniqueId(), amount);
            }
            sender.sendMessage(msgTranslator.getMessageAndReplace("eco.success",
                    true,
                    args[0],
                    economy.format(amount),
                    economy.format(amount)));
        } else {
            // Add balance to all selected players
            for (Entity entity : ents) {
                if (entity instanceof OfflinePlayer) {
                    economy.setBalance(Bukkit.getOfflinePlayer(args[0]).getUniqueId(), amount);
                    sender.sendMessage(msgTranslator.getMessageAndReplace("eco.success",
                            true,
                            args[0],
                            economy.format(amount),
                            economy.format(amount)));
                }
            }
        }
        return true;
    }

}
