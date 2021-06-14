package de.geolykt.easyconomy.minestom.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.minestom.EasyconomyAdvanced;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

@SuppressWarnings("static-access")
public class BaltopCommand extends Command {

    private final @NotNull EasyconomyAdvanced extension;
    private static final @NotNull HashMap<UUID, String> UUID_TO_NAME = new HashMap<>();

    private final String header;
    private final String entry;

    private final int entriesPerPage;

    public BaltopCommand(@NotNull EasyconomyAdvanced invokingExtension) {
        super("baltop", "moneytop");
        extension = invokingExtension;
        entriesPerPage = extension.getConfig().getBaltopPageSize();
        header = extension.getConfig().getBaltopHeader();
        entry = extension.getConfig().getBaltopEntry();
        setDefaultExecutor(this::performCommand);
    }


    /**
     * Banana.
     *
     * @param sender sender
     * @param args unused
     */
    public void performCommand(@NotNull CommandSender sender, @NotNull CommandContext args) {
        String message = String.format(header, entriesPerPage);
        if (message == null) {
            throw new InternalError("The JVM is kinda fucked");
        }
        sender.sendMessage(message);
        Iterator<Map.Entry<UUID, Double>> balances = extension.getEconomy().getPlayerDataStorage().getBaltop().entrySet().iterator();
        int i = 0;
        while (balances.hasNext() && i++ < entriesPerPage) {
            Map.Entry<UUID, Double> e = balances.next();
            if (!UUID_TO_NAME.containsKey(e.getKey())) {
                UUID id = e.getKey();
                if (id == null) {
                    throw new IllegalStateException("Null Id is in circulation!");
                }
                Player p = MinecraftServer.getConnectionManager().getPlayer(id);
                if (p == null) {
                    i--;
                    continue;
                }
                UUID_TO_NAME.put(id, p.getUsername());
            }
            message = String.format(entry, i, UUID_TO_NAME.get(e.getKey()), extension.getEconomy().format(e.getValue()));
            if (message == null) {
                throw new InternalError("The JVM is kinda fucked");
            }
            sender.sendMessage(message);
        }
    }
}
