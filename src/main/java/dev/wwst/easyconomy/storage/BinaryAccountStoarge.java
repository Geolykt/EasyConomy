package dev.wwst.easyconomy.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dev.wwst.easyconomy.Easyconomy;

/**
 * Binary adapters for Bank storage.
 * @author Geolykt
 */
public class BinaryAccountStoarge implements Saveable {

    private final File storageLoc;
    private final HashMap<String, Account> accounts = new HashMap<>();

    public BinaryAccountStoarge(File file, Easyconomy plugin) throws IOException {
        file.createNewFile();
        this.storageLoc = file;
        reload();
        plugin.addSaveable(this);
    }

    public void addAccount(Account acc) {
        accounts.put(acc.getName(), acc);
    }

    public Account getAccount(String name) {
        return accounts.get(name);
    }

    /**
     * Adds an account if it is absent
     * @param acc The account to add
     * @return True if the account was already added, false otherwise
     */
    public boolean addIfAbsent(Account acc) {
        return accounts.putIfAbsent(acc.getName(), acc) != null;
    }

    public boolean isAccountExisting(String name) {
        return accounts.containsKey(name);
    }

    @Override
    public void save() throws IOException {
        try (FileOutputStream ioStream = new FileOutputStream(storageLoc)) {
            for (Map.Entry<String, Account> acc : accounts.entrySet()) {
                acc.getValue().serialize(ioStream);
            }
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
    }

    public Set<String> getAccounts() {
        return accounts.keySet();
    }

    /**
     * Returns the amount of money a given bank has, or 0.0 if the bank does not exist. Does not throw NullPointers.
     * @param name The bank of the name
     * @return The amount of money within a given bank.
     */
    public double getMoney(String name) {
        return isAccountExisting(name) ? getAccount(name).getMoney() : 0.0;
    }

    public Account removeAccount(String name) {
        return accounts.remove(name);
    }

    /**
     * Returns the amount of money a given bank has, or defaultValue if the bank does not exist. 
     *  Does not throw NullPointers.
     * @param name The bank of the name
     * @param defaultValue The default value to return
     * @return The amount of money within a given bank.
     */
    public double getBalanceOrDefault(String name, double defaultValue) {
        return isAccountExisting(name) ? getAccount(name).getMoney() : defaultValue;
    }
}
