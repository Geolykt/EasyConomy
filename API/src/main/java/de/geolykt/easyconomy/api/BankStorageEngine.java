package de.geolykt.easyconomy.api;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The storage engine for bank accounts, the implementation should be thinking about thread safety as many calls could
 * be performed in async.
 * @author Geolykt
 * @since 1.1.0
 */
public interface BankStorageEngine extends Saveable {

    /**
     * Returns the balance of the bank and returns Double.NEGATIVE_INFINITY if there is no bank allocated to the name.
     * @param bank the bank that is the target of the operation
     * @return The balance of the bank
     * @since 1.1.0
     */
    public default double getBalance(@NotNull String bank) {
        return getBalanceOrDefault(bank, Double.NEGATIVE_INFINITY);
    }

    /**
     * Returns the balance of the bank and returns the given default amount if there was no bank allocated to the name.
     * @param bank The bank that should be the target of the operation
     * @param defaultVal the default balance that should be returned if there was not bank with the given name
     * @return The balance of the specified bank or the default value.
     * @since 1.1.0
     */
    public double getBalanceOrDefault(@NotNull String bank, double defaultVal);

    /**
     * The returned list is backed by the internal storage engine, so modifications can lead to the removal of banks or similar.
     * Great care should be taken to avoid accidental deletions.
     * @return Returns a list of the names of all the registered banks.
     * @since 1.1.0
     */
    public @NotNull Set<String> getBanks();

    /**
     * Queries whether the engine is aware that a bank of a given name is in existence.
     * @param bank The bank that should be queried
     * @return True if the engine is aware of the existence, false otherwise
     * @since 1.1.0
     */
    public boolean has(@NotNull String bank);

    /**
     * Adds a bank to the storage engine, there is no clear specification on how implementations should react
     * when there is already a bank with the same name, so the caller must be sure that there is no other bank under the same name.
     * @param bank The bank to add to the storage engine
     * @since 1.1.0
     */
    public void add(@NotNull Bank bank);

    /**
     * Obtains the Bank that matches a given name
     * @param name The name of the bank to look for
     * @return The bank with the requested name or null if none was found
     */
    public @Nullable Bank get(@NotNull String name);

    /**
     * Copies the internal reference file to the backupDirectory
     * @param file The directory where the backups are held
     * @throws IOException In case an IO Exception occurs while copying the files
     * @since 1.1.0
     */
    public void backup(@NotNull File backupDirectory) throws IOException;
}
