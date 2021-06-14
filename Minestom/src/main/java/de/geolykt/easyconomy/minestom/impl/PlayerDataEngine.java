package de.geolykt.easyconomy.minestom.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.Files;

import de.geolykt.easyconomy.api.PlayerDataStorage;

/**
 * The default implementation for the {@link PlayerDataStorage} using binary flatfiles to store the data.
 * @author Geolykt
 */
public class PlayerDataEngine implements PlayerDataStorage {

    private final HashMap<UUID, Double> data = new HashMap<>();
    private @NotNull LinkedHashMap<UUID, Double> baltop = new LinkedHashMap<>();

    private final @NotNull File storageLocation;

    public PlayerDataEngine(@NotNull File storingFile) {
        storageLocation = storingFile;
        storingFile.getParentFile().mkdirs();
        reload();
    }

    @Override
    public synchronized void save() throws IOException {
        if (!storageLocation.exists()) {
            storageLocation.createNewFile();
        }
        try (FileOutputStream fos = new FileOutputStream(storageLocation)) {
            fos.write(2); // We store in the v2 storage specification
            Map<UUID, Double> inactiveAccounts = data.entrySet().stream()
                    .filter((entry) -> entry.getValue() == 0.0)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            Map<UUID, Double> activeAccounts = data.entrySet().stream()
                    .filter((entry) -> entry.getValue() != 0.0)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            ByteBuffer buff = ByteBuffer.allocate(24 + inactiveAccounts.size() * 16 + activeAccounts.size() * 24);
            buff.putLong(Long.MIN_VALUE);
            buff.putInt(activeAccounts.size());
            buff.putInt(inactiveAccounts.size());
            buff.putDouble(0.0);
            for (Map.Entry<UUID, Double> entry : inactiveAccounts.entrySet()) {
                buff.putLong(entry.getKey().getMostSignificantBits());
                buff.putLong(entry.getKey().getLeastSignificantBits());
            }
            for (Map.Entry<UUID, Double> entry : activeAccounts.entrySet()) {
                buff.putLong(entry.getKey().getMostSignificantBits());
                buff.putLong(entry.getKey().getLeastSignificantBits());
                buff.putDouble(entry.getValue());
            }
            if (buff.hasRemaining()) {
                throw new BufferUnderflowException();
            }
            fos.write(buff.array());
        }
    }

    @Override
    public double getOrDefault(@NotNull UUID player, double defaultValue) {
        return data.getOrDefault(player, defaultValue);
    }

    @Override
    public @NotNull List<UUID> getAllKeys() {
        List<UUID> uids = Arrays.asList(data.keySet().toArray(new UUID[0]));
        if (uids == null) {
            throw new InternalError("JVM is broken");
        }
        return uids;
    }

    @Override
    public void set(@NotNull UUID key, double value) {
        data.put(key, value);
    }

    @Override
    public synchronized void reload() {
        if (storageLocation.exists()) {
            try (FileInputStream fis = new FileInputStream(storageLocation)) {
                if (fis.read() != 2) {
                    throw new IllegalStateException("The version of the PDS file is unsupported (only v2 supported), consider updating the extension.");
                }
                data.clear();
                ByteBuffer buff = ByteBuffer.wrap(fis.readAllBytes());

                buff.getLong();
                int activeAccounts = buff.getInt();
                int inactiveAccounts = buff.getInt();
                double defaultMoney = buff.getDouble();
                synchronized (data) {
                    while (inactiveAccounts-- > 0) {
                        data.put(new UUID(buff.getLong(), buff.getLong()), defaultMoney);
                    }
                    while (activeAccounts-- > 0) {
                        data.put(new UUID(buff.getLong(), buff.getLong()), buff.getDouble());
                    }
                    if (buff.hasRemaining()) {
                        throw new IOException("Trailling bytes after the stream should have ended.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reloadBaltop();
    }

    @Override
    public @NotNull Map<UUID, Double> getBaltop() {
        reloadBaltop(); // FIXME this is terribly inefficient
        return baltop;
    }

    @Override
    public boolean has(@NotNull UUID key) {
        return data.containsKey(key);
    }

    @Override
    public synchronized void backup(@NotNull File backupDir) throws IOException {
        File backupFile = new File(backupDir, "backup-bal-" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date.from(Instant.now())) + ".dat");
        Files.copy(storageLocation, backupFile);
    }

    @Override
    public @NotNull File getStorageFile() {
        return storageLocation;
    }

    @SuppressWarnings("null")
    public void reloadBaltop() {
        baltop = data.entrySet().stream()
                .dropWhile((entry) -> entry.getValue() < 0.0) // Remove inactive accounts - this eases sorting a bit further
                .sorted((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}