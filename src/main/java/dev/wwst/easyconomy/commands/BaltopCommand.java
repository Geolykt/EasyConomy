package dev.wwst.easyconomy.commands;

import dev.wwst.easyconomy.EasyConomyProvider;
import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.storage.PlayerDataStorage;
import dev.wwst.easyconomy.utils.MessageTranslator;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class BaltopCommand implements CommandExecutor {

    private final MessageTranslator msg;
    private final PlayerDataStorage pds;
    private final Economy eco;
    private final String baltopPermission;

    public BaltopCommand(@NotNull EasyConomyProvider economy, @NotNull MessageTranslator translator, @NotNull Easyconomy plugin) {
        msg = translator;
        eco = economy;
        pds = economy.getStorage();
        baltopPermission = plugin.getConfig().getString("permissions.baltop","");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!"".equals(baltopPermission) && !sender.hasPermission(baltopPermission)) {
            sender.sendMessage(msg.getMessageAndReplace("general.noPerms", true, baltopPermission));
            return true;
        }

        if(pds.getBaltop() == null || pds.getBaltop().size() == 0) {
            sender.sendMessage(msg.getMessage("baltop.none", true));
            return true;
        }

        StringBuilder message = new StringBuilder(msg.getMessage("baltop.start",true));
        message.append("\n"); // newline
        for(Map.Entry<UUID, Double> entry : pds.getBaltop().entrySet()) {
            if(Bukkit.getOfflinePlayer(entry.getKey()).getName() == null) {
                message.append("Invalid entry: ").append(entry.getKey().toString());
            } else {
                message.append(msg.getMessageAndReplace("baltop.value", false, eco.format(entry.getValue()), Bukkit.getOfflinePlayer(entry.getKey()).getName()));
                message.append("\n");
            }
        }

        sender.sendMessage(message.toString());

        return true;
    }
}