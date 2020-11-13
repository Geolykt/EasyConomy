package dev.wwst.easyconomy;

import dev.wwst.easyconomy.storage.*;
import dev.wwst.easyconomy.utils.Configuration;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author Weiiswurst
 */
public class EasyConomyProvider implements Economy {

    private final PlayerDataStorage playerPDS;
    private final BinaryAccountStoarge bankPDS;
    private final Logger logger;

    private final String currencyFormatSingular,
            currencyFormatPlural;

    public EasyConomyProvider(FileConfiguration config, Easyconomy invokingPlugin) throws IOException {
        playerPDS = new BinaryDataStorage(config.getString("storage-location-player", "balances.dat"), config.getInt("baltopPlayers"));
        bankPDS = new BinaryAccountStoarge(new File(config.getString("storage-location-bank", "banks.dat")), invokingPlugin);

        if(Configuration.get().getBoolean("enable-logging",true))
            logger = Easyconomy.getPluginLogger();
        else
            logger = null;

        currencyFormatSingular = ChatColor.translateAlternateColorCodes('&',config.getString("names.currencyFormatSingular", "%s Dollar"));
        currencyFormatPlural = ChatColor.translateAlternateColorCodes('&',config.getString("names.currencyFormatPlural","%s Dollars"));
    }

    public PlayerDataStorage getStorage() {
        return playerPDS;
    }

    /**
     * Checks if economy method is enabled.
     *
     * @return Success or Failure
     */
    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("EasyConomy");
    }

    /**
     * Gets name of economy method
     *
     * @return Name of Economy Method
     */
    @Override
    public String getName() {
        return "EasyConomy";
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
    public String format(double amount) {
        int decimalsShown = Configuration.get().getInt("decimalsShown");
        if(decimalsShown >= 0) {
            amount = new BigDecimal(amount).setScale(decimalsShown, RoundingMode.DOWN).doubleValue();
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
    public String currencyNamePlural() {
        return Configuration.get().getString("names.currencyNamePlural","Dollars");
    }

    /**
     * Returns the name of the currency in singular form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (singular)
     */
    @Override
    public String currencyNameSingular() {
        return Configuration.get().getString("names.currencyNameSingular","Dollar");
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer)} instead.
     */
    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return bankPDS.getAccount(playerName) != null;
        } else {
            return hasAccount(player);
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
    public boolean hasAccount(OfflinePlayer player) {
        return playerPDS.has(player.getUniqueId());
    }

    /**
     * @param playerName
     * @param worldName
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer, String)} instead.
     */
    @Override
    public boolean hasAccount(String playerName, String worldName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return bankPDS.getAccount(playerName) != null;
        } else {
            return hasAccount(player);
        }
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
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer)} instead.
     */
    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return bankBalance(playerName).balance;
        } else {
            return getBalance(player);
        }
    }

    /**
     * Gets balance of a player
     *
     * @param player of the player
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(OfflinePlayer player) {
        return playerPDS.getPlayerData(player);
    }

    /**
     * @param playerName
     * @param world
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer, String)} instead.
     */
    @Override
    public double getBalance(String playerName, String world) {
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
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #has(OfflinePlayer, double)} instead.
     */
    @Override
    public boolean has(String playerName, double amount) {
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
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use @{link {@link #has(OfflinePlayer, String, double)} instead.
     */
    @Override
    public boolean has(String playerName, String worldName, double amount) {
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
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return getBalance(player) >= amount;
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, double)} instead.
     */
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return bankWithdraw(playerName, amount);
        } else {
            return withdrawPlayer(player, amount);
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
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        final double oldBalance = getBalance(player);
        // BigDecimal for less approximations when dealing with doubles (due to how floating point values are handled in
        // Java, there will always be approximations) ( https://docs.oracle.com/cd/E19957-01/806-3568/ncg_goldberg.html )
        final double newBalance = BigDecimal.valueOf(oldBalance).subtract(BigDecimal.valueOf(amount)).doubleValue();
        if(logger != null)
            logger.info("[TRANSFER] "+player.getUniqueId()+" "+format(-amount));
        playerPDS.write(player.getUniqueId(), newBalance);
        //logger.info("New bal"+getBalance(player)+" old bal "+oldBalance);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, String, double)} instead.
     */
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
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
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    /**
     * @param playerName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, double)} instead.
     */
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            return bankDeposit(playerName, amount);
        } else {
            return depositPlayer(player, amount);
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
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return withdrawPlayer(player,-amount);
    }

    /**
     * @param playerName
     * @param worldName
     * @param amount
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, String, double)} instead.
     */
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
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
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    /**
     * @param name
     * @param player
     * @deprecated As of VaultAPI 1.4 use {{@link #createBank(String, OfflinePlayer)} instead.
     */
    @Override
    public EconomyResponse createBank(String name, String player) {
        if (bankPDS.isAccountExisting(name)) {
            return new EconomyResponse(0, bankPDS.getMoney(name),
                    EconomyResponse.ResponseType.FAILURE, "Bank already exists.");
        }
        Set<UUID> uuids = new HashSet<UUID>();
        OfflinePlayer plyr = Bukkit.getOfflinePlayer(player);
        if (plyr != null) {
            uuids.add(plyr.getUniqueId());
        }
        bankPDS.addAccount(new Account(name, 0, uuids));
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
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        if (bankPDS.isAccountExisting(name)) {
            return new EconomyResponse(0, bankPDS.getMoney(name),
                    EconomyResponse.ResponseType.FAILURE, "Bank already exists.");
        }
        Set<UUID> uuids = new HashSet<UUID>();
        uuids.add(player.getUniqueId());
        bankPDS.addAccount(new Account(name, 0, uuids));
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, null);
    }

    /**
     * Deletes a bank account with the specified name.
     *
     * @param name of the back to delete
     * @return if the operation completed successfully
     */
    @Override
    public EconomyResponse deleteBank(String name) {
        Account bank = bankPDS.removeAccount(name);
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
    public EconomyResponse bankBalance(String name) {
        Account bank = bankPDS.getAccount(name);
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
    public EconomyResponse bankHas(String name, double amount) {
        // TODO what should the method really do?
        Account bank = bankPDS.getAccount(name);
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
    public EconomyResponse bankWithdraw(String name, double amount) {
        Account bank = bankPDS.getAccount(name);
        if (bank == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE, "Bank doesn't exist.");
        }
        if(logger != null)
            logger.info("[BANK-TRANSFER] "+ name +" "+format(-amount));
        bank.removeMoney(amount);
        return new EconomyResponse(amount, bank.getMoney(), EconomyResponse.ResponseType.FAILURE, null);
    }

    /**
     * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name   of the account
     * @param amount to deposit
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        Account bank = bankPDS.getAccount(name);
        if (bank == null) {
            return new EconomyResponse(0, 0,
                    EconomyResponse.ResponseType.FAILURE, "Bank doesn't exist.");
        }
        if(logger != null)
            logger.info("[BANK-TRANSFER] "+ name +" "+format(amount));
        bank.addMoney(amount);
        return new EconomyResponse(amount, bank.getMoney(), EconomyResponse.ResponseType.FAILURE, null);
    }

    /**
     * @param name
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankOwner(String, OfflinePlayer)} instead.
     */
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        Account bank = bankPDS.getAccount(name);
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
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        Account bank = bankPDS.getAccount(name);
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
    public EconomyResponse isBankMember(String name, String playerName) {
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
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return isBankOwner(name, player);
    }

    /**
     * Gets the list of banks
     *
     * @return the List of Banks
     */
    @Override
    public List<String> getBanks() {
        return Arrays.asList((String[]) bankPDS.getAccounts().toArray());
    }

    /**
     * @param playerName
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer)} instead.
     */
    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player == null) {
            if (bankPDS.isAccountExisting(playerName)) {
                return false;
            } else {
                bankPDS.addAccount(new Account(playerName, 0, new HashSet<UUID>()));
                return true;
            }
        } else {
            return createPlayerAccount(player);
        }
    }

    /**
     * Attempts to create a player account for the given player
     *
     * @param player OfflinePlayer
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (playerPDS.has(player.getUniqueId())) {
            return false;
        } else {
            playerPDS.write(player.getUniqueId(), 0.0);
            return true;
        }
    }

    /**
     * @param playerName
     * @param worldName
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer, String)} instead.
     */
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
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
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }
}
