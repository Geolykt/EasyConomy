package de.geolykt.easyconomy.minestom.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.Files;

import de.geolykt.easyconomy.api.Bank;
import de.geolykt.easyconomy.api.BankStorageEngine;
import de.geolykt.easyconomy.api.PlaceholderBank;

/**
 * The default IMPLEMENTATION of the BankStorageEngine.
 * @author Geolykt
 * @since 1.1.0
 */
public class BankDataEngine implements BankStorageEngine {

    private final File loc;
    private final @NotNull HashMap<String, Bank> banks = new HashMap<>();
    
    public BankDataEngine(File storageFile) {
        loc = storageFile;
    }

    @Override
    public synchronized void save() throws IOException {
        synchronized (banks) {
            try (FileOutputStream ioStream = new FileOutputStream(loc)) {
                for (Map.Entry<String, Bank> acc : banks.entrySet()) {
                    acc.getValue().serialize(ioStream);
                }
            }
        }
    }

    protected @NotNull Bank deserialize(ByteBuffer in) {
        int objlen = in.getInt();
        byte[] cstrName = new byte[in.get()];
        in.get(cstrName);
        String name = new String(cstrName, StandardCharsets.UTF_8);
        double money = in.getDouble();
        int remainingPlayers = Math.floorDiv(objlen - 9 - cstrName.length, 16);
        // TODO implement a memberlist on bank accounts (requires Bank implementation that supports this)
        while (remainingPlayers-- > 0) {
            in.getLong(); // most significant
            in.getLong(); // least significant
        }
        return new PlaceholderBank(name, money);
    }

    protected synchronized void reload() throws IOException {
        synchronized (banks) {
            if (!loc.exists()) {
                loc.getParentFile().mkdirs();
                loc.createNewFile();
                return;
            }
            ByteBuffer buff;
            try (FileInputStream fis = new FileInputStream(loc)) {
                buff = ByteBuffer.wrap(fis.readAllBytes());
            }
            banks.clear();
            while (buff.hasRemaining()) {
                Bank bank = deserialize(buff);
                banks.put(bank.getName(), bank);
            }
        }
    }

    @Override
    public double getBalanceOrDefault(@NotNull String bank, double defaultVal) {
        if (banks.containsKey(bank)) {
            return banks.get(bank).getMoney();
        }
        return defaultVal;
    }

    @Override
    public @NotNull Set<String> getBanks() {
        Set<String> keyset = banks.keySet();
        if (keyset == null) {
            // Unlikely that this would happen
            return new HashSet<>();
        }
        return keyset;
    }

    @Override
    public boolean has(@NotNull String bank) {
        return banks.containsKey(bank);
    }

    @Override
    public void add(@NotNull Bank bank) {
        banks.put(bank.getName(), bank);
    }

    @Override
    public @Nullable Bank get(@NotNull String name) {
        return banks.get(name);
    }

    @Override
    public synchronized void backup(@NotNull File backupDirectory) throws IOException {
        File backupFile = new File(backupDirectory, "backup-bal-" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date.from(Instant.now())) + ".dat");
        Files.copy(loc, backupFile);
    }

}
