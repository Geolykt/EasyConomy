package dev.wwst.easyconomy.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.wwst.easyconomy.Easyconomy;

/**
 * Binary adapters for Bank storage.
 * @author Geolykt
 */
public class BinaryAccountStoarge implements Saveable {

    private final File storageLoc;
    private final HashMap<String, Account> accounts = new HashMap<>();
    private final Logger logger;

    public BinaryAccountStoarge(@NotNull String path, @NotNull Easyconomy plugin) throws IOException {
        File storageFolder = new File(plugin.getDataFolder() + "/storage");
        if (!storageFolder.exists())
            storageFolder.mkdirs();
        this.storageLoc = new File(plugin.getDataFolder() + "/storage", path);
        reload();
        plugin.addSaveable(this);
        logger = plugin.getLogger();
    }

    public void addAccount(@NotNull Account acc) {
        accounts.put(acc.getName(), acc);
    }

    @Nullable
    public Account getAccount(@NotNull String name) {
        return accounts.get(name);
    }

    /**
     * Adds an account if it is absent
     * @param acc The account to add
     * @return True if the account was already added, false otherwise
     */
    public boolean addIfAbsent(@NotNull Account acc) {
        return accounts.putIfAbsent(acc.getName(), acc) != null;
    }

    public boolean isAccountExisting(@NotNull String name) {
        return accounts.containsKey(name);
    }

    @Override
    public void save() throws IOException {
        long time = System.currentTimeMillis();
        try (FileOutputStream ioStream = new FileOutputStream(storageLoc)) {
            for (Map.Entry<String, Account> acc : accounts.entrySet()) {
                acc.getValue().serialize(ioStream);
            }
        }
        logger.info("Saved " + storageLoc.getName() + " within " + (System.currentTimeMillis() - time) + "ms.");
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
    }

    @NotNull
    public Set<String> getAccounts() {
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
}
