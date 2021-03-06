/*
 * EasyconomyAdvanced, a lightweight economy plugin
 * Copyright (C) Weiiswurst
 * Copyright (C) Geolykt (<https://geolykt.de>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.wwst.easyconomy.eco;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.geolykt.easyconomy.api.Bank;
import de.geolykt.easyconomy.api.EasyconomyEcoAPI;
import de.geolykt.easyconomy.api.PlaceholderBank;
import de.geolykt.easyconomy.api.PlayerDataStorage;
import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.storage.BinaryAccountStoarge;
import dev.wwst.easyconomy.storage.BinaryDataStorage;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

/**
 * The EasyconomyProvider class is the implementation of the economy used by many plugins.
 * While mainly the class is accessed via the interface it implements, 
 * it can also be used directly to perform more complex tasks and act as an API itself.
 * @author Weiiswurst, Geolykt
 * @since 1.1.0
 */
public class VaultEconomyProvider implements Economy, EasyconomyEcoAPI {

    private final PlayerDataStorage playerPDS;
    private final BinaryAccountStoarge bankPDS;
    private final @Nullable Logger logger;
    private static final Pattern INVALID_PLAYERNAME = Pattern.compile("[^a-zA-Z0-9_]");

    private final String currencyFormatSingular,
            currencyFormatPlural,
            currencyNameSingular,
            currencyNamePlural;
    private final int fractionalDigits;

    /**
     * Creates a new instance of the class with the given parameters to load the required dependencies of the plugin
     * @param config The FileConfiguration to use to get all the configurations that the implementation uses.
     * @param invokingPlugin
     * @throws IOException
     */
    public VaultEconomyProvider(@NotNull FileConfiguration config, @NotNull Easyconomy invokingPlugin) throws IOException {
        playerPDS = new BinaryDataStorage(invokingPlugin,
                config.getString("storage-location-player", "balances.dat"), config.getInt("baltopPlayers"));
        bankPDS = new BinaryAccountStoarge(config.getString("storage-location-bank", "banks.dat"), invokingPlugin);

        if(config.getBoolean("enable-logging",true))
            logger = invokingPlugin.getLogger();
        else
            logger = null;

        currencyNameSingular = config.getString("names.currencyNameSingular","Dollar");
        currencyNamePlural = config.getString("names.currencyNamePlural","Dollars");
        currencyFormatSingular = ChatColor.translateAlternateColorCodes('&',config.getString("names.currencyFormatSingular", "%s Dollar"));
        currencyFormatPlural = ChatColor.translateAlternateColorCodes('&',config.getString("names.currencyFormatPlural","%s Dollars"));
        fractionalDigits = config.getInt("decimalsShown");
    }

    /**
     * Checks if economy method is enabled.
     *
     * @return Success or Failure
     */
    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("EasyConomy"); // FIXME magic constant
    }

    /**
     * Gets name of economy method
     *
     * @return Name of Economy Method
     */
    @Override
    @NotNull
    public String getName() {
        return "EasyConomyAdvanced";
    }

    /**
     * Returns true if the given implementation supports banks.
     *
     * @return true if the implementation supports banks
     */
    @Override
    public boolean hasBankSupport() {
        return true;
    }

    /**
     * Some economy plugins round off after a certain number of digits.
     * This function returns the number of digits the plugin keeps
     * or -1 if no rounding occurs.
     *
     * @return number of digits after the decimal point kept
     */
    @Override
    public int fractionalDigits() {
        return -1;
    }

    /**
     * Format amount into a human readable String This provides translation into
     * economy specific formatting to improve consistency between plugins.
     *
     * @param amount to format
     * @return Human readable string describing amount
     */
    @Override
    @NotNull
    public String format(double amount) {
        if(fractionalDigits >= 0) {
            amount = new BigDecimal(amount).setScale(fractionalDigits, RoundingMode.DOWN).doubleValue();
        }
        if(amount != 1) return String.format(currencyFormatPlural,amount);
        else return String.format(currencyFormatSingular,amount);
    }

    /**
     * Returns the name of the currency in plural form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (plural)
     */
    @Override
    @NotNull
    public String currencyNamePlural() {
        return currencyNamePlural;
    }

    /**
     * Returns the name of the currency in singular form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (singular)
     */
    @Override
    @NotNull
    public String currencyNameSingular() {
        return currencyNameSingular;
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer)} instead.
     */
    @Override
    public boolean hasAccount(@NotNull String playerName) {
        if (INVALID_PLAYERNAME.matcher(playerName).find()) {
            return bankPDS.get(playerName) != null;
        } else {
            return hasAccount(Bukkit.getOfflinePlayer(playerName));
        }
    }

    /**
     * Checks if this player has an account on the server yet
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param player to check
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(@NotNull OfflinePlayer player) {
        return playerPDS.has(player.getUniqueId());
    }

    /**
     * @param playerName
     * @param worldName
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer, String)} instead.
     */
    @Override
    public boolean hasAccount(@NotNull String playerName, @NotNull String worldName) {
        return hasAccount(playerName);
    }

    /**
     * Checks if this player has an account on the server yet on the given world
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param player    to check in the world
     * @param worldName world-specific account
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(@NotNull OfflinePlayer player, @NotNull String worldName) {
        return hasAccount(player);
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer)} instead.
     */
    @Override
    public double getBalance(@NotNull String playerName) {
        if (INVALID_PLAYERNAME.matcher(playerName).find()) {
            return bankBalance(playerName).balance;
        } else {
            return getBalance(Bukkit.getOfflinePlayer(playerName));
        }
    }

    /**
     * Gets balance of a player
     *
     * @param player of the player
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(@NotNull OfflinePlayer player) {
        return playerPDS.getOrDefault(player.getUniqueId(), Double.NEGATIVE_INFINITY);
    }

    /**
     * @param playerName
     * @param world
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer, String)} instead.
     */
    @Override
    public double getBalance(@NotNull String playerName, @NotNull String world) {
        return getBalance(playerName);
    }

    /**
     * Gets balance of a player on the specified world.
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player to check
     * @param world  name of the world
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(@NotNull OfflinePlayer player, @NotNull String world) {
        return getBalance(player);
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #has(OfflinePlayer, double)} instead.
     */
    @Override
    public boolean has(@NotNull String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    /**
     * Checks if the player account has the amount - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to check
     * @param amount to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    @Override
    public boolean has(@NotNull OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use @{link {@link #has(OfflinePlayer, String, double)} instead.
     */
    @Override
    public boolean has(@NotNull String playerName, @NotNull String worldName, double amount) {
        return getBalance(playerName) >= amount;
    }

    /**
     * Checks if the player account has the amount in a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to check
     * @param worldName to check with
     * @param amount    to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    @Override
    public boolean has(@NotNull OfflinePlayer player, @NotNull String worldName, double amount) {
        return getBalance(player) >= amount;
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, double)} instead.
     */
    @Override
    @NotNull 
    public EconomyResponse withdrawPlayer(@NotNull String playerName, double amount) {
        if (INVALID_PLAYERNAME.matcher(playerName).find()) {
            return bankWithdraw(playerName, amount);
        } else {
            return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
        }
    }

    /**
     * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to withdraw from
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
    @Override
    @NotNull
    public EconomyResponse withdrawPlayer(@NotNull OfflinePlayer player, double amount) {
        final double oldBalance = getBalance(player);
        // BigDecimal for less approximations when dealing with doubles (due to how floating point values are handled in
        // Java, there will always be approximations) ( https://docs.oracle.com/cd/E19957-01/806-3568/ncg_goldberg.html )
        final double newBalance = BigDecimal.valueOf(oldBalance).subtract(BigDecimal.valueOf(amount)).doubleValue();
        if(logger != null)
            logger.info("[TRANSFER-DEL] " + player.getUniqueId() + " " + format(amount));
        playerPDS.set(player.getUniqueId(), newBalance);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, String, double)} instead.
     */
    @Override
    @NotNull
    public EconomyResponse withdrawPlayer(@NotNull String playerName, @NotNull String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    /**
     * Withdraw an amount from a player on a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to withdraw from
     * @param worldName - name of the world
     * @param amount    Amount to withdraw
     * @return Detailed response of transaction
     */
    @Override
    @NotNull
    public EconomyResponse withdrawPlayer(@NotNull OfflinePlayer player, @NotNull String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, double)} instead.
     */
    @Override
    @NotNull
    public EconomyResponse depositPlayer(@NotNull String playerName, double amount) {
        if (INVALID_PLAYERNAME.matcher(playerName).find()) {
            if (!bankPDS.has(playerName)) {
                bankPDS.add(new PlaceholderBank(playerName, amount));
                return new EconomyResponse(amount, amount, EconomyResponse.ResponseType.SUCCESS, null);
            } else {
                return bankDeposit(playerName, amount);
            }
        } else {
            return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
        }
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to deposit to
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
    @Override
    @NotNull
    public EconomyResponse depositPlayer(@NotNull OfflinePlayer player, double amount) {
        final double oldBalance = getBalance(player);
        final double newBalance = BigDecimal.valueOf(oldBalance).add(BigDecimal.valueOf(amount)).doubleValue();
        if(logger != null)
            logger.info("[TRANSFER-ADD] " + player.getUniqueId() + " " + format(amount));
        playerPDS.set(player.getUniqueId(), newBalance);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, String, double)} instead.
     */
    @Override
    @NotNull
    public EconomyResponse depositPlayer(@NotNull String playerName, @NotNull String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player    to deposit to
     * @param worldName name of the world
     * @param amount    Amount to deposit
     * @return Detailed response of transaction
     */
    @Override
    @NotNull
    public EconomyResponse depositPlayer(@NotNull OfflinePlayer player, @NotNull String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    /**
     * @param name
     * @param player
     * @deprecated As of VaultAPI 1.4 use {{@link #createBank(String, OfflinePlayer)} instead.
     */
    @Override
    @NotNull
    public EconomyResponse createBank(@NotNull String name, @NotNull String player) {
        if (bankPDS.has(name)) {
            return new EconomyResponse(0, bankPDS.getMoney(name),
                    EconomyResponse.ResponseType.FAILURE, "Bank already exists.");
        }
        Set<UUID> uuids = new HashSet<UUID>();
        OfflinePlayer plyr = Bukkit.getOfflinePlayer(player);
        if (plyr != null) {
            uuids.add(plyr.getUniqueId());
        }
        bankPDS.add(new Account(name, 0, uuids));
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * Creates a bank account with the specified name and the player as the owner
     *
     * @param name   of account
     * @param player the account should be linked to
     * @return EconomyResponse Object
     */
    @Override
    @NotNull
    public EconomyResponse createBank(@NotNull String name, @NotNull OfflinePlayer player) {
        if (bankPDS.has(name)) {
            return new EconomyResponse(0, bankPDS.getMoney(name),
                    EconomyResponse.ResponseType.FAILURE, "Bank already exists.");
        }
        Set<UUID> uuids = new HashSet<UUID>();
        uuids.add(player.getUniqueId());
        bankPDS.add(new Account(name, 0, uuids));
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * Deletes a bank account with the specified name.
     *
     * @param name of the back to delete
     * @return if the operation completed successfully
     */
    @Override
    @NotNull
    public EconomyResponse deleteBank(@NotNull String name) {
        Bank bank = bankPDS.removeAccount(name);
        if (bank == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE, "Bank doesn't exist.");
        }
        return new EconomyResponse(bank.getMoney(), 0, EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * Returns the amount the bank has
     *
     * @param name of the account
     * @return EconomyResponse Object
     */
    @Override
    @NotNull
    public EconomyResponse bankBalance(@NotNull String name) {
        Bank bank = bankPDS.get(name);
        if (bank == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE, "Bank doesn't exist.");
        }
        return new EconomyResponse(0, bank.getMoney(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * Returns true or false whether the bank has the amount specified - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to check for
     * @return EconomyResponse Object
     */
    @Override
    @NotNull
    public EconomyResponse bankHas(@NotNull String name, double amount) {
        Bank bank = bankPDS.get(name);
        if (bank == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE, "Bank doesn't exist.");
        }
        if (bank.getMoney() < amount) {
            return new EconomyResponse(0, bank.getMoney(), EconomyResponse.ResponseType.FAILURE, null);
        } else {
            return new EconomyResponse(0, bank.getMoney(), EconomyResponse.ResponseType.SUCCESS, null);
        }
    }

    /**
     * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to withdraw
     * @return EconomyResponse Object
     */
    @Override
    @NotNull
    public EconomyResponse bankWithdraw(@NotNull String name, double amount) {
        Bank bank = bankPDS.get(name);
        if (bank == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE, "Bank doesn't exist.");
        }
        if(logger != null)
            logger.info("[BANK-TRANSFER] "+ name +" "+format(-amount));
        bank.removeMoney(amount);
        return new EconomyResponse(amount, bank.getMoney(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to deposit
     * @return EconomyResponse Object
     */
    @Override
    @NotNull
    public EconomyResponse bankDeposit(@NotNull String name, double amount) {
        Bank bank = bankPDS.get(name);
        if (bank == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE, "Bank doesn't exist.");
        }
        if(logger != null)
            logger.info("[BANK-TRANSFER] "+ name +" "+format(amount));
        bank.addMoney(amount);
        return new EconomyResponse(amount, bank.getMoney(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * @param name
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankOwner(String, OfflinePlayer)} instead.
     */
    @Override
    @NotNull
    public EconomyResponse isBankOwner(@NotNull String name, @NotNull String playerName) {
        Bank bank = bankPDS.get(name);
        if (bank == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE, "Bank doesn't exist.");
        }
        return new EconomyResponse(0, 
                bank.getMoney(),
                bank.isMember(playerName) ? ResponseType.SUCCESS : ResponseType.FAILURE,
                 null);
    }

    /**
     * Check if a player is the owner of a bank account
     *
     * @param name   of the account
     * @param player to check for ownership
     * @return EconomyResponse Object
     */
    @Override
    @NotNull
    public EconomyResponse isBankOwner(@NotNull String name, @NotNull OfflinePlayer player) {
        Bank bank = bankPDS.get(name);
        if (bank == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE, "Bank doesn't exist.");
        }
        return new EconomyResponse(0, 
                bank.getMoney(),
                bank.isMember(player.getUniqueId()) ? ResponseType.SUCCESS : ResponseType.FAILURE,
                 null);
    }

    /**
     * @param name
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankMember(String, OfflinePlayer)} instead.
     */
    @Override
    @NotNull
    public EconomyResponse isBankMember(@NotNull String name, @NotNull String playerName) {
        return isBankOwner(name, playerName);
    }

    /**
     * Check if the player is a member of the bank account
     *
     * @param name   of the account
     * @param player to check membership
     * @return EconomyResponse Object
     */
    @Override
    @NotNull
    public EconomyResponse isBankMember(@NotNull String name, @NotNull OfflinePlayer player) {
        return isBankOwner(name, player);
    }

    /**
     * Gets the list of bank names.
     *  Modifying the list modifies the banks, so great care should be taken!
     *
     * @return the List of Banks
     */
    @Override
    @NotNull
    public List<String> getBanks() {
        return Arrays.asList((String[]) bankPDS.getBanks().toArray());
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer)} instead.
     */
    @Override
    public boolean createPlayerAccount(@NotNull String playerName) {
        if (INVALID_PLAYERNAME.matcher(playerName).find()) {
            if (bankPDS.has(playerName)) {
                return false;
            } else {
                bankPDS.add(new Account(playerName, 0, new HashSet<UUID>()));
                return true;
            }
        } else {
            return createPlayerAccount(Bukkit.getOfflinePlayer(playerName));
        }
    }

    /**
     * Attempts to create a player account for the given player
     *
     * @param player OfflinePlayer
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(@NotNull OfflinePlayer player) {
        if (playerPDS.has(player.getUniqueId())) {
            return false;
        } else {
            playerPDS.set(player.getUniqueId(), 0.0);
            return true;
        }
    }

    /**
     * @param playerName
     * @param worldName
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer, String)} instead.
     */
    @Override
    public boolean createPlayerAccount(@NotNull String playerName, @NotNull String worldName) {
        return createPlayerAccount(playerName);
    }

    /**
     * Attempts to create a player account for the given player on the specified world
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this then false will always be returned.
     *
     * @param player    OfflinePlayer
     * @param worldName String name of the world
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(@NotNull OfflinePlayer player, @NotNull String worldName) {
        return createPlayerAccount(player);
    }

    /**
     * Sets the balance of a bank account to a given number.
     *  Does not create a new bank account and safely returns false if there is none.
     * @param bank The bank account that needs to be affected.
     * @param amount The new bank balance
     * @return True if the bank existed, false otherwise
     */
    public boolean setBankBalance(@NotNull String bank, double amount) {
        if (!bankPDS.has(bank)) {
            return false;
        }
        bankPDS.get(bank).setMoney(amount);
        return true;
    }

    /**
     * Sets the balance of a player to a given number.
     *  Does not create a new player balance and safely returns false if there is none.
     * @param player The player that needs to be affected.
     * @param amount The new balance of the player's balance
     * @return True if the player balance existed, false otherwise
     */
    public boolean setPlayerBalance(@NotNull OfflinePlayer player, double amount) {
        if (!playerPDS.has(player.getUniqueId())) {
            return false;
        }
        playerPDS.set(player.getUniqueId(), amount);
        return true;
    }

    @Override
    public @NotNull PlayerDataStorage getPlayerDataStorage() {
        return playerPDS;
    }

    @Override
    public @NotNull BinaryAccountStoarge getBankStorage() {
        return bankPDS;
    }

    @Override
    public double givePlayerMoney(@NotNull UUID player, double amount) {
        final double oldBalance = playerPDS.getOrDefault(player, 0.0);
        final double newBalance = BigDecimal.valueOf(oldBalance).add(BigDecimal.valueOf(amount)).doubleValue();
        if(logger != null)
            logger.info("[TRANSFER-ADD] " + player + " " + format(amount));
        playerPDS.set(player, newBalance);
        return newBalance;
    }

    @Override
    public double removePlayerMoney(@NotNull UUID player, double amount) {
        final double oldBalance = playerPDS.getOrDefault(player, 0.0);
        final double newBalance = BigDecimal.valueOf(oldBalance).subtract(BigDecimal.valueOf(amount)).doubleValue();
        if(logger != null)
            logger.info("[TRANSFER-DEL] " + player + " " + format(amount));
        playerPDS.set(player, newBalance);
        return newBalance;
    }

    @Override
    public @Nullable Bank getBank(@NotNull String name) {
        return bankPDS.get(name);
    }

    @Override
    public boolean createBank(@NotNull String name) {
        if (bankPDS.has(name)) {
            bankPDS.add(new PlaceholderBank(name, 0.0));
            return true;
        }
        return false;
    }

    @Override
    public boolean isPlayerExisting(@NotNull UUID player) {
        return playerPDS.has(player);
    }

    @Override
    public boolean createPlayer(@NotNull UUID player) {
        if (playerPDS.has(player)) {
            return false;
        }
        playerPDS.set(player, 0.0); // FIXME not the default money!
        return true;
    }

    @Override
    public double setBalance(@NotNull UUID player, double amount) {
        double old = playerPDS.getOrDefault(player, Double.NEGATIVE_INFINITY);
        playerPDS.set(player, amount);
        return old;
    }

    @Override
    public double setBalance(@NotNull String bank, double amount) {
        Bank bankObj = bankPDS.get(bank);
        if (bankObj != null) {
            double old = bankObj.getMoney();
            bankObj.setMoney(amount);
            return old;
        }
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double getPlayerBalance(@NotNull UUID player) {
        return playerPDS.getOrDefault(player, Double.NEGATIVE_INFINITY);
    }

    @Override
    public double getBankBalance(@NotNull String bank) {
        return bankPDS.getBalanceOrDefault(bank, Double.NEGATIVE_INFINITY);
    }
}
