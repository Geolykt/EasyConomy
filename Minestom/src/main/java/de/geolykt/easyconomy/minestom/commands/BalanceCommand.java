package de.geolykt.easyconomy.minestom.commands;

import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.minestom.EasyconomyAdvanced;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

@SuppressWarnings("static-access")
public class BalanceCommand extends Command {

    private final EasyconomyAdvanced extension;

    private final @NotNull String balanceSelf;
    private final @NotNull String balanceOther;
    private final @NotNull String notAPlayer;
    private final @NotNull String invalidPlayer;

    private static final @NotNull ArgumentWord TARGET = new ArgumentWord("target");

    public BalanceCommand(EasyconomyAdvanced invokingExtension) {
        super("balance", "bal", "money");
        extension = invokingExtension;

        balanceSelf = extension.getConfig().getSelfBalance();
        balanceOther = extension.getConfig().getOthersBalance();
        notAPlayer = extension.getConfig().getNotAPlayer();
        invalidPlayer = extension.getConfig().getInvalidPlayer();

        this.setDefaultExecutor(this::selfExecution);
        this.addSyntax(this::targettedExecution, TARGET);
    }

    /**
     * Command executor used when there are no further arguments.
     *
     * @param sender The sender
     * @param args Arguments (unused)
     */
    public void selfExecution(@NotNull CommandSender sender, @NotNull CommandContext args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(notAPlayer);
            return;
        }
        double balance = extension.getEconomy().getPlayerBalance(((Player)sender).getUuid());
        String message = String.format(balanceSelf, extension.getEconomy().format(balance));
        if (message == null) {
            throw new InternalError("The JVM is kinda fucked");
        }
        sender.sendMessage(message);
    }

    public void targettedExecution(@NotNull CommandSender sender, @NotNull CommandContext args) {
        @SuppressWarnings("null") // Nothing you can do
        Player player = MinecraftServer.getConnectionManager().getPlayer(args.get(TARGET));
        if (player == null) {
            sender.sendMessage(invalidPlayer);
            return;
        }
        double balance = extension.getEconomy().getPlayerBalance(player.getUuid());
        if (balance == Double.NEGATIVE_INFINITY) {
            sender.sendMessage(invalidPlayer);
            return;
        }
        String message = String.format(balanceOther, args.get(TARGET), extension.getEconomy().format(balance));
        if (message == null) {
            throw new InternalError("The JVM is kinda fucked");
        }
        sender.sendMessage(message);
    }
}
