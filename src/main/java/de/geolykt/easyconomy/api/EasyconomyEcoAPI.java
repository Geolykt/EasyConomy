package de.geolykt.easyconomy.api;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.wwst.easyconomy.storage.BinaryAccountStoarge;

/**
 * The API interface of the Easyconomy plugin that can be used by other plugins to perform more complex tasks.
 *  It is also used as a bridge between different Economy APIs so the internal components of the plugin are guaranteed to 
 *  work even in a non-vault environment. All implementations of the interface should be thread safe but may block if needed.
 * @author Geolykt
 * @since 1.1.0
 */
public interface EasyconomyEcoAPI {

    /**
     * Obtains the PlayerDataStorage, which handles the storage of the individual player accounts, direct access to this
     *  is discouraged and using different API calls within this instance is preferred.
     * @return The PlayerDataStorage used by the implementation
     * @since 1.1.0
     */
    public @NotNull PlayerDataStorage getPlayerDataStorage();

    /**
     * Obtains the account storage of the plugin, which handles the storage of the bank balances of players, like the
     * PlayerDataStorage, direct access is discouraged.
     * @return The BinaryAccountStoarge used by the implementation
     * @since 1.1.0
     */
    public @NotNull BinaryAccountStoarge getBankStorage();

    /**
     * Grants the player a given sum of money, the implementation should create a new player account if needed but should
     * handle non-existent balances accordingly.
     * @param player The player that should be the target of the operation
     * @param amount The amount of money that should be given to the player
     * @return The new balance of the player
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public double givePlayerMoney(@NotNull UUID player, double amount);

    /**
     * Removes money from the balance of a player, the implementation can create a new player account if needed but should
     * handle non-existent balances accordingly.
     * @param player The player that should be the target of the operation
     * @param amount The amount of money that should be removed from the player
     * @return The new balance of the player
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public double removePlayerMoney(@NotNull UUID player, double amount);

    /**
     * Deposits money to a bank and returns true if the transfer succeeded, false in case a non-existing bank is the target,
     *  however the implementation may decide on other circumstances where false might be returned.
     * @param bankName The bank that should be the target of the operation.
     * @param amount The amount that should be given to the bank. Should be over 0.
     * @return True if the transfer succeeded, false otherwise.
     * @implNote The default implementation does not create a bank if needed and returns false, some other implementations may create a bank though and as such always return true.
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public default boolean giveBankMoney(@NotNull String bankName, double amount) {
        Bank bank = getBank(bankName);
        if (bank == null) {
            return false;
        }
        bank.addMoney(amount);
        return true;
    }

    /**
     * Withdraws money from a bank and returns true if the transfer succeeded, false in case a non-existing bank is the target,
     *  however the implementation may decide on other circumstances where false might be returned.
     * @param bankName The bank that should be the target of the operation.
     * @param amount The amount that should be removed from the bank. Should be over 0.
     * @return True if the transfer succeeded, false otherwise.
     * @implNote The default implementation does not create a bank if needed and returns false, some other implementations may create a bank though and as such always return true.
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public default boolean removeBankMoney(@NotNull String bankName, double amount) {
        Bank bank = getBank(bankName);
        if (bank == null) {
            return false;
        }
        bank.removeMoney(amount);
        return true;
    }

    /**
     * Converts an amount of the currency into the implementation's format. This may include adding adding currency symbols
     *  or adapting punctuation marks and other things to the different languages
     * @param amount The amount.
     * @return The formatted amount
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public @NotNull String format(double amount);

    /**
     * Converts an amount of the currency into the implementation's format. This may include adding adding currency symbols
     *  or adapting punctuation marks and other things to the given locale.
     * @param amount The amount.
     * @param locale The locale that the output should adapt for, or null if it's the system default
     * @return The formatted amount adapted for a locale
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public default @NotNull String format(double amount, @Nullable String locale) {
        return format(amount);
    }

    // Bank querying
    /**
     * Obtains the bank that is bound to a name, or null if it doesn't exist.
     *  The implementation should not create a new bank implicitly.
     * @param name The name of the bank
     * @return The bank bound to the name
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public @Nullable Bank getBank(@NotNull String name);

    /**
     * Creates a new bank with 0 money as long as it doesn't exist already.
     * @param name The name of the bank
     * @return True if the bank was created, false otherwise
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public boolean createBank(@NotNull String name);

    /**
     * Queries whether the Player is known to the economy implementation.
     * @param player the player to query
     * @return True if the player is known, false otherwise
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public boolean isPlayerExisting(@NotNull UUID player);

    /**
     * Explicitly creates a new balance for a player.
     *  The implementation should handle already existing balances safely.
     * @param player the player to query
     * @return True if the player was created, false otherwise
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public boolean createPlayer(@NotNull UUID player);

    // Advanced balance manipulation
    /**
     * Sets the balance of a player to a given amount.
     *  The implementation may create a player is not known.
     * @param player the player to query
     * @param amount The new balance of the player
     * @return the old balance of the player; Double.NEGATIVE_INFINITY if the player isn't known
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public double setBalance(@NotNull UUID player, double amount);

    /**
     * Sets the balance of a bank to a given amount.
     *  The implementation may create a bank if not found, however default implementations do not.
     * @param bank the bank to query
     * @param amount The new balance of the bank
     * @return the old balance of the bank; Double.NEGATIVE_INFINITY if the bank wasn't found
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public double setBalance(@NotNull String bank, double amount);

    /**
     * Sets the balance of a player to a given amount. It should return Double.NEGATIVE_INFINITY if the player is not known.
     * @param player the player to query
     * @return the balance of the player
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public double getPlayerBalance(@NotNull UUID player);

    /**
     * Returns the balance of a bank. It should return Double.NEGATIVE_INFINITY if there is no bank attached to the name.
     * @param bank the bank to query
     * @return the balance of the bank
     * @implSpec The implementation should always be thread safe.
     * @since 1.1.0
     */
    public double getBankBalance(@NotNull String bank);

    /**
     * Transfers a portion of the balance of a Bank to the balance of the other.
     *  The implementation may stop the source player from going negative
     * @param src The source balance
     * @param dest The destination balance
     * @param amount The maximum amount to transfer
     * @return The amount of money that was transfered
     * @implSpec The implementation should always be thread safe
     * @since 1.1.0
     */
    public default double transferBalance(@NotNull UUID src, @NotNull UUID dest, double amount) {
        removePlayerMoney(src, amount);
        givePlayerMoney(dest, amount);
        return amount;
    }
}
