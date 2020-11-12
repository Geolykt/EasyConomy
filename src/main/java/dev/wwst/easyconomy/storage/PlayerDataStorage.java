package dev.wwst.easyconomy.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

/**
 * @author Weiiswurst
 */
public interface PlayerDataStorage extends Saveable {

    public double getPlayerData(OfflinePlayer p);

    public double getPlayerData(UUID player);

    public List<UUID> getAllData();

    public void write(UUID key, double value);

    public void reload();

    public Map<UUID, Double> getBaltop();
    
    public boolean has(UUID key);

}
