package de.geolykt.easyconomy.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

/**
 * @author Geolykt
 * @since 1.1.0
 */
public interface PlayerDataStorage extends Saveable {

    /**
     * Returns the player balance or a given default if the player does not exist.
     *  Does not created a balance.
     * @param player The player to query
     * @param defaultValue the amount to return if the player is not known to the implementation yet.
     * @return The balance of the player, or the specified default value
     * @implNote The implementation should be thread safe.
     * @since 1.1.0
     */
    public double getOrDefault(@NotNull UUID player, double defaultValue);

    public @NotNull List<UUID> getAllKeys();

    /**
     * Sets the balance of a player
     * @param key The player that should be the target of the operation
     * @param value The new balance of the player
     * @implNote As always, this should be thread safe
     * @since 1.1.0
     */
    public void set(@NotNull UUID key, double value);

    /**
     * Reloads the data from file
     * @implNote This should be thread safe, but may block
     * @since 1.1.0
     */
    public void reload();

    public @NotNull Map<UUID, Double> getBaltop();
    
    public boolean has(@NotNull UUID key);

    public void backup(@NotNull File backupDir) throws IOException;

    public @NotNull File getStorageFile();

}
