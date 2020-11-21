package dev.wwst.easyconomy.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.io.Files;

import dev.wwst.easyconomy.Easyconomy;

/**
 * Binary adapters for Bank storage.
 * @author Geolykt
 */
public class BinaryAccountStoarge implements Saveable {
    private boolean modified; // Used so the instance doesn't save unnessary amount of times.

    private final File storageLoc;
    private final Map<String, Account> accounts = Collections.synchronizedMap(new HashMap<>());
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

    public void addAccount(@NotNull Account acc) {
        accounts.put(acc.getName(), acc);
        modified = true;
    }

    @Nullable
    public Account getAccount(@NotNull String name) {
        modified = true; // We have to assume that it was modified since the returning account instance could be modified.
        return accounts.get(name);
    }

    /**
     * Adds an account if it is absent
     * @param acc The account to add
     * @return True if the account was already added, false otherwise
     */
    public boolean addIfAbsent(@NotNull Account acc) {
        modified = true;
        return accounts.putIfAbsent(acc.getName(), acc) != null;
    }

    public boolean isAccountExisting(@NotNull String name) {
        return accounts.containsKey(name);
    }

    @Override
    public void save() throws IOException {
        if (modified) {
            synchronized (accounts) {
                synchronized (BinaryAccountStoarge.class) {
                    long time = System.currentTimeMillis();
                    try (FileOutputStream ioStream = new FileOutputStream(storageLoc)) {
                        for (Map.Entry<String, Account> acc : accounts.entrySet()) {
                            acc.getValue().serialize(ioStream);
                        }
                    }
                    logger.info("Saved " + storageLoc.getName() + " within " + (System.currentTimeMillis() - time) + "ms.");
                }
            }
            modified = false;
        }
    }

    public void reload() throws IOException {
        try (FileInputStream ioStream = new FileInputStream(storageLoc)) {
            accounts.clear();
            while (true) {
                Account acc = Account.deserialize(ioStream);
                if (acc == null) {
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
    public Set<String> getAccounts() {
        modified = true; // we have to assume that it's modified
        return accounts.keySet();
    }

    /**
     * Returns the amount of money a given bank has, or 0.0 if the bank does not exist. Does not throw NullPointers.
     * @param name The bank of the name
     * @return The amount of money within a given bank.
     */
    public double getMoney(@NotNull String name) {
        return isAccountExisting(name) ? getAccount(name).getMoney() : 0.0;
    }

    public Account removeAccount(@NotNull String name) {
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
        return isAccountExisting(name) ? getAccount(name).getMoney() : defaultValue;
    }

    public void backup() throws IOException {
        File backupFolder = new File(storageLoc.getParentFile().getParentFile(), "backups");
        backupFolder.mkdir();
        synchronized (BinaryAccountStoarge.class) {
            File backupFile = new File(backupFolder, "backup-" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Date.from(Instant.now())) + ".dat");
            Files.copy(storageLoc, backupFile);
        }
    }
}
