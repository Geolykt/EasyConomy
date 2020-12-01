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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.Files;

import de.geolykt.easyconomy.api.Bank;
import de.geolykt.easyconomy.api.BankStorageEngine;
import de.geolykt.easyconomy.api.PlaceholderBank;
import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.eco.Account;

/**
 * Binary adapters for Bank storage.
 * @author Geolykt
 */
public class BinaryAccountStoarge implements BankStorageEngine {
    private boolean modified; // Used so the instance doesn't save unnessary amount of times.

    private final File storageLoc;
    private final Map<String, Bank> accounts = Collections.synchronizedMap(new HashMap<>());
    private final Logger logger;

    public BinaryAccountStoarge(@NotNull String path, @NotNull Easyconomy plugin) throws IOException {
        File storageFolder = new File(plugin.getDataFolder() + "/storage");
        if (!storageFolder.exists())
            storageFolder.mkdirs();
        this.storageLoc = new File(plugin.getDataFolder() + "/storage", path);
        this.storageLoc.createNewFile();
        reload();
        plugin.addSaveable(this);
        logger = plugin.getLogger();
    }

    @Override
    public void add(@NotNull Bank acc) {
        accounts.put(acc.getName(), acc);
        modified = true;
    }

    @Nullable
    @Override
    public Bank get(@NotNull String name) {
        modified = true; // We have to assume that it was modified since the returning account instance could be modified.
        return accounts.get(name);
    }

    /**
     * Adds an account if it is absent
     * @param acc The account to add
     * @return True if the account was already added, false otherwise
     */
    public boolean addIfAbsent(@NotNull Bank acc) {
        modified = true;
        return accounts.putIfAbsent(acc.getName(), acc) != null;
    }

    @Override
    public boolean has(@NotNull String name) {
        return accounts.containsKey(name);
    }

    @Override
    public void save() throws IOException {
        if (modified) {
            synchronized (accounts) {
                synchronized (BinaryAccountStoarge.class) {
                    long time = System.currentTimeMillis();
                    try (FileOutputStream ioStream = new FileOutputStream(storageLoc)) {
                        for (Map.Entry<String, Bank> acc : accounts.entrySet()) {
                            acc.getValue().serialize(ioStream);
                        }
                    }
                    logger.info("Saved " + storageLoc.getName() + " within " + (System.currentTimeMillis() - time) + "ms.");
                }
            }
            modified = false;
        }
    }

    @Nullable
    private static Bank deserializeBank(@NotNull InputStream ioStream) throws IOException {
        byte[] data = new byte[4];
        if (ioStream.read(data) == -1) {
            return null;
        }
        data = new byte[ByteBuffer.wrap(data).getInt()];
        ioStream.read(data);
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte[] cstrName = new byte[buffer.get()];
        buffer.get(cstrName);
        String name = new String(cstrName, StandardCharsets.UTF_8);
        double money = buffer.getDouble();

        if (!buffer.hasRemaining()) {
            return new PlaceholderBank(name, money);
        } else {
            HashSet<UUID> members = new HashSet<>();
            while (buffer.hasRemaining()) {
                members.add(new UUID(buffer.getLong(), buffer.getLong()));
            }
            return new Account(name, money, members);
        }
    }

    public void reload() throws IOException {
        try (FileInputStream ioStream = new FileInputStream(storageLoc)) {
            accounts.clear();
            while (true) {
                Bank acc = deserializeBank(ioStream);
                if (acc == null) { // returns null if the stream is closed.
                    break;
                }
                accounts.put(acc.getName(), acc);
            }
        }
        modified = false;
    }

    @NotNull
    /**
     * Returns a Set of the names of the Bank accounts stored within the instance.
     *  Manipulating the set manipulates the bank accounts stored within the instance, so great care should be taken!
     * @return A set of names of the bank accounts stored in the instance
     */
    @Override
    public Set<String> getBanks() {
        modified = true; // we have to assume that it's modified
        return accounts.keySet();
    }

    /**
     * Returns the amount of money a given bank has, or 0.0 if the bank does not exist. Does not throw NullPointers.
     * @param name The bank of the name
     * @return The amount of money within a given bank.
     */
    public double getMoney(@NotNull String name) {
        return has(name) ? get(name).getMoney() : 0.0;
    }

    public Bank removeAccount(@NotNull String name) {
        modified = true;
        return accounts.remove(name);
    }

    /**
     * Returns the amount of money a given bank has, or defaultValue if the bank does not exist. 
     *  Does not throw NullPointers.
     * @param name The bank of the name
     * @param defaultValue The default value to return
     * @return The amount of money within a given bank.
     */
    public double getBalanceOrDefault(@NotNull String name, double defaultValue) {
        return has(name) ? get(name).getMoney() : defaultValue;
    }

    public void backup(@NotNull File backupDir) throws IOException {
        synchronized (BinaryAccountStoarge.class) {
            File backupFile = new File(backupDir, "backup-bank-" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date.from(Instant.now())) + ".dat");
            Files.copy(storageLoc, backupFile);
        }
    }
}
