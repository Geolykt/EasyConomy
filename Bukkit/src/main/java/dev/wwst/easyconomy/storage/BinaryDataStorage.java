/*
 * EasyconomyAdvanced, a lightweight economy plugin
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
package dev.wwst.easyconomy.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.Files;

import de.geolykt.easyconomy.api.PlayerDataStorage;
import dev.wwst.easyconomy.Easyconomy;

/**
 * An implementation of the PlayerDataStoarge that directly stores data in binary form.
 *  This yields in a smaller file size and faster I/O speeds.
 * @author Geolykt
 */
public class BinaryDataStorage implements PlayerDataStorage {
    private boolean modified = false;

    private final File file;
    private final Map<UUID, Double> balances = Collections.synchronizedMap(new HashMap<>());

    private final Easyconomy plugin;

    private LinkedHashMap<UUID, Double> balTop;
    private double smallestBalTop = Double.MAX_VALUE;
    private double lastBackup = 0;

    public BinaryDataStorage(@NotNull Easyconomy invokingPlugin, @NotNull String path, int baltopLength) {
        plugin = invokingPlugin;
        plugin.getLogger().info("Loading Storage: " + path);
        long timestamp = System.currentTimeMillis();

        File storageFolder = new File(plugin.getDataFolder() + "/storage");
        if (!storageFolder.exists())
            storageFolder.mkdirs();

        file = new File(plugin.getDataFolder() + "/storage", path);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reload();
        plugin.addSaveable(this);

        if (baltopLength > 0) {
            plugin.getLogger().info(
                    "Calculating top balances... (if you have thousands of accounts, this could take a few seconds)");
            recalcBaltop(balances, baltopLength);
            plugin.getLogger().info(balTop.size() + " balances are now in the baltop.");
        } else {
            balTop = null;
        }
        timestamp = System.currentTimeMillis() - timestamp;
        plugin.getLogger().info("Loaded Storage: " + path + " within " + timestamp + "ms");
    }

    @Override
    public double getOrDefault(@NotNull UUID player, double defaultValue) {
        return balances.getOrDefault(player, defaultValue);
    }

    @Override
    @NotNull
    public List<UUID> getAllKeys() {
        ArrayList<UUID> data = new ArrayList<>();
        synchronized (balances) {
            balances.forEach((id, balance) -> {
                if (balance != null && balance != 0.0) {
                    data.add(id);
                }
            });
        }
        return data;
    }

    /*
     ** Saves the current FileConfiguration to the file on the disk
     */
    @Override
    public void save() {
        if (modified) {
            long time = System.currentTimeMillis();
            synchronized (BinaryDataStorage.class) {
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    fileOut.write(1);
                    synchronized (balances) {
                        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
                            fileOut.write(ByteBuffer.allocate(24)
                                    .putLong(entry.getKey().getMostSignificantBits())
                                    .putLong(entry.getKey().getLeastSignificantBits())
                                    .putDouble(entry.getValue())
                                    .array());
                        }
                    }
                    plugin.getLogger().info("Storage file " + file.getName() + " saved within " + (System.currentTimeMillis() - time) + "ms.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            modified = false;
        }
    }

    @Override
    public void set(@NotNull UUID account, double balance) {
        balances.put(account, balance);
        if (balance > smallestBalTop) {
            System.out.println("Recalculating top balances (If you have a lot of accounts, this should happen very rarely)");
            balTop.put(account, balance);
            recalcBaltop(balTop, plugin.getConfig().getInt("baltopPlayers"));
        }
        modified = true;
    }

    @Override
    public void reload() {
        long time = System.currentTimeMillis();
        try (FileInputStream fileIn = new FileInputStream(file)) {
            int version = fileIn.read();
            ByteBuffer buff = ByteBuffer.wrap(fileIn.readAllBytes());
            balances.clear();
            switch (version) {
            case 2: { // New v2 binary storage format, which stores less data on servers that have a high amount of inactive players.
                lastBackup = buff.getLong();
                int activeAccounts = buff.getInt(); // Used for corruption tests
                int inactiveAccounts = buff.getInt();
                double defaultMoney = buff.getDouble();
                // Corruption tests
                if (inactiveAccounts*16 + activeAccounts*24 != buff.remaining()) {
                    plugin.getLogger().severe("Storage file " + file.getName() + " has an invalid length."
                            + " It's probably corrupted and the plugin will be disabled to prevent damage.");
                    Bukkit.getPluginManager().disablePlugin(plugin);
                    throw new IOException("Unexpected file size for the v2 binary storage format.");
                }
                synchronized (balances) {
                    while (inactiveAccounts-- > 0) {
                        balances.put(new UUID(buff.getLong(), buff.getLong()), defaultMoney);
                    }
                }
            }
            case 1: // Old v1 binary storage format
                break;
            case -1: // File hasn't yet been created.
                return;
            default:
                plugin.getLogger().warning("Storage file " + file.getName() + " has an invalid version."
                        + " Reading it anyway.");
            }
            if (buff.remaining() % 24 != 0) {
                plugin.getLogger().severe("Storage file " + file.getName() + " has an invalid length."
                        + " It's probably corrupted and the plugin will be disabled to prevent damage.");
                Bukkit.getPluginManager().disablePlugin(plugin);
                // This should force everything in the stack to terminate as plugins don't get disabled instantly
                throw new IOException("Unexpected file size"); 
            }
            while (buff.hasRemaining()) {
                balances.put(new UUID(buff.getLong(), buff.getLong()), buff.getDouble());
            }
            plugin.getLogger().info("Storage file " + file.getName() + " loaded within " + (System.currentTimeMillis() - time) + "ms.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        modified = false;
    }

    /**
     * Recalculates the balance top by sorting the raw unsorted data which needs to be provided to the function.
     * @param notSorted The unsorted leaderboard
     * @param baltopLength The maximum length of the leaderboard
     * @since 1.1.0
     * @author Weiiswurst
     */
    private void recalcBaltop(@NotNull Map<UUID, Double> notSorted, int baltopLength) {
        balTop = notSorted.entrySet().stream()
                .filter((entry) -> entry.getValue() != 0.0)
                .sorted((c1, c2) -> -c1.getValue().compareTo(c2.getValue()))
                .peek(val->{
                    if(val.getValue() < smallestBalTop) smallestBalTop = val.getValue();})
                .limit(baltopLength)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        if(balTop.size() < baltopLength) {
            smallestBalTop = Double.NEGATIVE_INFINITY;
        }
    }

    @Override
    @NotNull
    public Map<UUID, Double> getBaltop() {
        return balTop;
    }

    @Override
    public boolean has(@NotNull UUID key) {
        return balances.containsKey(key);
    }

    @Override
    public synchronized void backup(@NotNull File backupFolder) throws IOException {
        File backupFile = new File(backupFolder, "backup-bal-" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date.from(Instant.now())) + ".dat");
        Files.copy(file, backupFile);
        lastBackup = System.currentTimeMillis();
    }

    @Override
    @NotNull
    public File getStorageFile() {
        return file;
    }
}