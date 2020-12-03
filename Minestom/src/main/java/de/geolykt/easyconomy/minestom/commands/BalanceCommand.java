package de.geolykt.easyconomy.minestom.commands;

import de.geolykt.easyconomy.minestom.EasyconomyAdvanced;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

@SuppressWarnings("static-access")
public class BalanceCommand extends Command {

    private final EasyconomyAdvanced extension;

    private final String balanceSelf;
    private final String balanceOther;
    private final String notAPlayer;
    private final String invalidPlayer;

    public BalanceCommand(EasyconomyAdvanced invokingExtension) {
        super("balance", "bal", "money");
        extension = invokingExtension;

        balanceSelf = extension.getConfig().getSelfBalance();
        balanceOther = extension.getConfig().getOthersBalance();
        notAPlayer = extension.getConfig().getNotAPlayer();
        invalidPlayer = extension.getConfig().getInvalidPlayer();

        this.setDefaultExecutor(this::selfExecution);
        this.addSyntax(this::targettedExecution, ArgumentType.Word("target"));
    }

    public void selfExecution(CommandSender sender, Arguments args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(notAPlayer);
            return;
        }
        double balance = extension.getEconomy().getPlayerBalance(((Player)sender).getUuid());
        sender.sendMessage(String.format(balanceSelf, extension.getEconomy().format(balance)));
    }

    public void targettedExecution(CommandSender sender, Arguments args) {
        Player player = MinecraftServer.getConnectionManager().getPlayer(args.getString("target"));
        if (player == null) {
            sender.sendMessage(invalidPlayer);
            return;
        }
        double balance = extension.getEconomy().getPlayerBalance(player.getUuid());
        if (balance == Double.NEGATIVE_INFINITY) {
            sender.sendMessage(invalidPlayer);
            return;
        }
        sender.sendMessage(String.format(balanceOther, args.getString("target"), extension.getEconomy().format(balance)));
    }
}
