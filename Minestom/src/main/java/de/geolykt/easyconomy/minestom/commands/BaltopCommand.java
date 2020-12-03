package de.geolykt.easyconomy.minestom.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import de.geolykt.easyconomy.minestom.EasyconomyAdvanced;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

@SuppressWarnings("static-access")
public class BaltopCommand extends Command {

    private final EasyconomyAdvanced extension;
    private final HashMap<UUID, String> uuidToNameMap = new HashMap<>();

    private final String header;
    private final String entry;

    private final int entriesPerPage;

    public BaltopCommand(EasyconomyAdvanced invokingExtension) {
        super("baltop", "moneytop");
        extension = invokingExtension;
        entriesPerPage = extension.getConfig().getBaltopPageSize();
        header = extension.getConfig().getBaltopHeader();
        entry = extension.getConfig().getBaltopEntry();
        setDefaultExecutor(this::performCommand);
    }


    public void performCommand(CommandSender sender, Arguments args) {
        sender.sendMessage(String.format(header, entriesPerPage));
        Iterator<Map.Entry<UUID, Double>> balances = extension.getEconomy().getPlayerDataStorage().getBaltop().entrySet().iterator();
        int i = 0;
        while (balances.hasNext() && i++ < entriesPerPage) {
            Map.Entry<UUID, Double> e = balances.next();
            if (!uuidToNameMap.containsKey(e.getKey())) {
                Player p = MinecraftServer.getConnectionManager().getPlayer(e.getKey());
                if (p == null) {
                    i--;
                    continue;
                }
                uuidToNameMap.put(e.getKey(), p.getUsername());
            }
            sender.sendMessage(String.format(entry, i, uuidToNameMap.get(e.getKey()), extension.getEconomy().format(e.getValue())));
        }
    }
}
