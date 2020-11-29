package de.geolykt.easyconomy.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Weiiswurst, Geolykt
 * @since 1.1.0
 */
public interface PlayerDataStorage extends Saveable {

    public double getPlayerData(@NotNull OfflinePlayer p);

    /**
     * Returns the player balance or a given default if the player does not exist.
     *  Does not created a balance.
     * @param player The player to query
     * @param defaultValue the amount to return if the player is not known to the implementation yet.
     * @return The balance of the player, or the specified default value
     * @implNote The implementation should be thread safe.
     * @since 1.1.0
     */
    public double getPlayerDataOrDefault(@NotNull OfflinePlayer player, double defaultValue);

    public double getPlayerData(@NotNull UUID player);

    public @NotNull List<UUID> getAllData();

    public void write(@NotNull UUID key, double value);

    public void reload();

    public @NotNull Map<UUID, Double> getBaltop();
    
    public boolean has(@NotNull UUID key);

    public void backup(@NotNull File backupDir) throws IOException;

    public @NotNull File getStorageFile();

}
