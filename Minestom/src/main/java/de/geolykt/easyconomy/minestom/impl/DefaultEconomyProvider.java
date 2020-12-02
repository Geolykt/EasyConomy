package de.geolykt.easyconomy.minestom.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.geolykt.easyconomy.api.Bank;
import de.geolykt.easyconomy.api.BankStorageEngine;
import de.geolykt.easyconomy.api.EasyconomyEcoAPI;
import de.geolykt.easyconomy.api.PlaceholderBank;
import de.geolykt.easyconomy.api.PlayerDataStorage;
import de.geolykt.easyconomy.minestom.EasyconomyAdvanced;

/**
 * The default economy provider for the Easyconomy plugin.
 * @author Geolykt
 * @since 1.1.0
 */
public class DefaultEconomyProvider implements EasyconomyEcoAPI {

    private final BankStorageEngine bankPDS;
    private final PlayerDataStorage playerPDS;

    private final String currencyFormat;

    public DefaultEconomyProvider(@NotNull EasyconomyAdvanced extension,
            @NotNull PlayerDataStorage playerStorageEngine,
            @NotNull BankStorageEngine bankStorageEngine) {
        currencyFormat = "$%.2f";
        playerPDS = playerStorageEngine;
        bankPDS = bankStorageEngine;
    }

    @Override
    public boolean createBank(@NotNull String name) {
        if (bankPDS.has(name)) {
            return false;
        }
        bankPDS.add(new PlaceholderBank(name, 0.0));
        return true;
    }

    @Override
    public boolean createPlayer(@NotNull UUID player) {
        if (playerPDS.has(player)) {
            return false;
        }
        playerPDS.set(player, 0.0);
        return true;
    }

    @Override
    public @NotNull String format(double amount) {
        return String.format(currencyFormat, amount);
    }

    @Override
    public @Nullable Bank getBank(@NotNull String bank) {
        return bankPDS.get(bank);
    }

    @Override
    public double getBankBalance(@NotNull String bank) {
        return bankPDS.getBalanceOrDefault(bank, Double.NEGATIVE_INFINITY);
    }

    @Override
    public @NotNull BankStorageEngine getBankStorage() {
        return bankPDS;
    }

    @Override
    public double getPlayerBalance(@NotNull UUID player) {
        return playerPDS.getOrDefault(player, Double.NEGATIVE_INFINITY);
    }

    @Override
    public @NotNull PlayerDataStorage getPlayerDataStorage() {
        return playerPDS;
    }

    @Override
    public double givePlayerMoney(@NotNull UUID player, double amount) {
        final double newBalance = BigDecimal.valueOf(playerPDS.getOrDefault(player, 0.0))
                                            .add(BigDecimal.valueOf(amount)).doubleValue();
        playerPDS.set(player, newBalance);
        return newBalance;
    }

    @Override
    public boolean isPlayerExisting(@NotNull UUID player) {
        return playerPDS.has(player);
    }

    @Override
    public double removePlayerMoney(@NotNull UUID player, double amount) {
        final double newBalance = BigDecimal.valueOf(playerPDS.getOrDefault(player, 0.0))
                                            .subtract(BigDecimal.valueOf(amount)).doubleValue();
        playerPDS.set(player, newBalance);
        return newBalance;
    }

    @Override
    public double setBalance(@NotNull UUID player, double amount) {
        final double oldBalance = playerPDS.getOrDefault(player, Double.NEGATIVE_INFINITY);
        playerPDS.set(player, amount);
        return oldBalance;
    }

    @Override
    public double setBalance(@NotNull String bank, double amount) {
        Bank b = bankPDS.get(bank);
        if (b == null) {
            return Double.NEGATIVE_INFINITY;
        }
        double oldAmount = b.getMoney();
        b.setMoney(amount);
        return oldAmount;
    }

}
