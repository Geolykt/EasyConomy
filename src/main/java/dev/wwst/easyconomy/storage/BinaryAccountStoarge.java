package dev.wwst.easyconomy.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dev.wwst.easyconomy.Easyconomy;

public class BinaryAccountStoarge implements Saveable {

    private final File location;
    private final HashMap<String, Account> accounts = new HashMap<>();

    public BinaryAccountStoarge(File file, Easyconomy plugin) throws IOException {
        file.createNewFile();
        this.location = file;
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
        try (FileOutputStream ioStream = new FileOutputStream(location)) {
            for (Map.Entry<String, Account> acc : accounts.entrySet()) {
                acc.getValue().serialize(ioStream);
            }
        }
    }

    public void reload() throws IOException {
        try (FileInputStream ioStream = new FileInputStream(location)) {
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
}
