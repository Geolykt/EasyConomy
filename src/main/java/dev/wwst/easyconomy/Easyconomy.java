package dev.wwst.easyconomy;

import dev.wwst.easyconomy.commands.BalanceCommand;
import dev.wwst.easyconomy.commands.BaltopCommand;
import dev.wwst.easyconomy.commands.EcoCommand;
import dev.wwst.easyconomy.commands.GivemoneyCommand;
import dev.wwst.easyconomy.commands.PayCommand;
import dev.wwst.easyconomy.commands.SetmoneyCommand;
import dev.wwst.easyconomy.eco.VaultEconomyProvider;
import dev.wwst.easyconomy.events.JoinEvent;
import dev.wwst.easyconomy.utils.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.api.Saveable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Weiiswurst
 */
public final class Easyconomy extends JavaPlugin {

    private final List<Saveable> toSave = new ArrayList<>();

    private VaultEconomyProvider ecp;
    private MessageTranslator translator;

    private void handleConfigUpdateing() {
        switch (getConfig().getInt("CONFIG_VERSION_NEVER_CHANGE_THIS")) {
        // FALL-THROUGHS are wanted here as we want to perform incremental changes
        case 1:
        case 2:
        case 3:
        case 4:
            getConfig().addDefault("storage-location-player", "balances.dat");
            getConfig().addDefault("storage-location-bank", "banks.dat");
            getConfig().addDefault("saving.delay", 100l); // 5 seconds delay
            getConfig().addDefault("saving.period", 1200l); // save every minute
        case 5:
            // Things to do when the config version is bumped to 6
        }
    }

    private void saveData() {
        Set<Saveable> erroringSaveables = new HashSet<>();
        for(Saveable saveable : toSave) {
            try {
                saveable.save();
            } catch (IOException e) {
                e.printStackTrace();
                erroringSaveables.add(saveable);
            }
        }
        if (toSave.removeAll(erroringSaveables)) {
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private boolean isLoaded = false;
    @Override
    public void onLoad() {
        getDataFolder().mkdirs();
        PluginManager pm = Bukkit.getPluginManager();
        saveDefaultConfig();
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
        } catch (ClassNotFoundException expected) {
            getLogger().severe("!!! VAULT IS NOT INSTALLED !!!");
            getLogger().severe("!!! THE VAULT PLUGIN IS NEEDED FOR THIS PLUGIN !!!");
            pm.disablePlugin(this);
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            try {
                ecp = new VaultEconomyProvider(getConfig(), this);
            } catch (IOException e) {
                e.printStackTrace();
                getLogger().severe("!!! Failed important FIO Operation !!!");
                pm.disablePlugin(this);
                return;
            }
            Bukkit.getServicesManager().register(Economy.class, ecp,this, ServicePriority.Normal);
        } else {
            getLogger().severe("!!! YOU ALREADY HAVE AN ECONOMY PLUGIN !!!");
            getLogger().severe(String.format("!!! REMOVE OR DISABLE THE ECONOMY OF %s !!!",rsp.getProvider().getName()));
            pm.disablePlugin(this);
            return;
        }
        isLoaded = true;
    }

    @Override
    public void onEnable() {
        getLogger().info("EasyConomy advanced is a fork that is maintained by Geolykt. "
                + "Do not bother Weiiswurst about any help for this and ask tristellar#9022 / Geolykt instead.");
        if (!isLoaded) {
            return;
        }
        File backupFolder = new File(ecp.getStorage().getStorageFile().getParentFile().getParentFile(), "backups");
        backupFolder.mkdir();
        handleConfigUpdateing();
        translator = new MessageTranslator(getConfig().getString("language"), this);

        String perm = getConfig().getString("permissions.balance", null);
        if ("".equals(perm)) {
            perm = null;
        }
        getCommand("balance").setPermission(perm);
        getCommand("balance").setPermissionMessage(translator.getMessageAndReplace("general.noPerms", true, perm));

        perm = getConfig().getString("permissions.pay", null);
        if ("".equals(perm)) {
            perm = null;
        }
        getCommand("pay").setPermission(perm);
        getCommand("pay").setPermissionMessage(translator.getMessageAndReplace("general.noPerms", true, perm));

        perm = getConfig().getString("permissions.baltop", null);
        if ("".equals(perm)) {
            perm = null;
        }
        getCommand("baltop").setPermission(perm);
        getCommand("baltop").setPermissionMessage(translator.getMessageAndReplace("general.noPerms", true, perm));

        perm = getConfig().getString("permissions.modify", null);
        if ("".equals(perm)) {
            perm = "op";
        }
        String permessage = translator.getMessageAndReplace("general.noPerms", true, perm);
        getCommand("givemoney").setPermission(perm);
        getCommand("givemoney").setPermissionMessage(permessage);
        getCommand("takemoney").setPermission(perm);
        getCommand("takemoney").setPermissionMessage(permessage);
        getCommand("setmoney").setPermission(perm);
        getCommand("setmoney").setPermissionMessage(permessage);

        getCommand("balance").setExecutor(new BalanceCommand(ecp, translator, this));
        getCommand("eco").setExecutor(new EcoCommand(ecp, translator, this, backupFolder));
        getCommand("pay").setExecutor(new PayCommand(ecp, translator, this));
        getCommand("baltop").setExecutor(new BaltopCommand(ecp, translator));
        getCommand("givemoney").setExecutor(new GivemoneyCommand(ecp, translator, false));
        getCommand("takemoney").setExecutor(new GivemoneyCommand(ecp, translator, true));
        getCommand("setmoney").setExecutor(new SetmoneyCommand(ecp, translator));

        if (getConfig().getInt("startingBalance") != 0) {
            Bukkit.getPluginManager().registerEvents(new JoinEvent(ecp, this),this);
        }
        getServer().getScheduler().runTaskTimerAsynchronously(this, this::saveData, 
                getConfig().getLong("saving.delay"), getConfig().getLong("saving.period"));
    }

    @Override
    public void onDisable() {
        if (!isLoaded) {
            return;
        }
        saveData();
    }

    public void addSaveable(@NotNull Saveable saveable) {
        toSave.add(saveable);
    }

    @NotNull
    public VaultEconomyProvider getEcp() {
        return ecp;
    }
}
