package dev.wwst.easyconomy;

import dev.wwst.easyconomy.commands.BalanceCommand;
import dev.wwst.easyconomy.commands.BaltopCommand;
import dev.wwst.easyconomy.commands.EcoCommand;
import dev.wwst.easyconomy.commands.PayCommand;
import dev.wwst.easyconomy.events.JoinEvent;
import dev.wwst.easyconomy.storage.Saveable;
import dev.wwst.easyconomy.utils.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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

    private EasyConomyProvider ecp;
    private MessageTranslator translator;

    public static final String PLUGIN_NAME = "EasyConomy";

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

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        handleConfigUpdateing();
        translator = new MessageTranslator(getConfig().getString("language"), this);

        PluginManager pm = Bukkit.getPluginManager();

        if(!pm.isPluginEnabled("Vault")) {
            getLogger().severe("!!! VAULT IS NOT INSTALLED !!!");
            getLogger().severe("!!! THE VAULT PLUGIN IS NEEDED FOR THIS PLUGIN !!!");
            pm.disablePlugin(this);
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            try {
                ecp = new EasyConomyProvider(getConfig(), this);
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

        getCommand("balance").setExecutor(new BalanceCommand(ecp, translator, this));
        getCommand("eco").setExecutor(new EcoCommand(ecp, translator, this));
        getCommand("pay").setExecutor(new PayCommand(ecp, translator, this));
        getCommand("baltop").setExecutor(new BaltopCommand(ecp, translator));

        pm.registerEvents(new JoinEvent(ecp, this),this);
        getServer().getScheduler().runTaskTimerAsynchronously(this, this::saveData, 
                getConfig().getLong("saving.delay"), getConfig().getLong("saving.period"));
    }

    @Override
    public void onDisable() {
        saveData();
    }

    public void addSaveable(@NotNull Saveable saveable) {
        toSave.add(saveable);
    }

    @NotNull
    public EasyConomyProvider getEcp() {
        return ecp;
    }
}
