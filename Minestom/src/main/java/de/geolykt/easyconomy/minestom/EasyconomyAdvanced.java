package de.geolykt.easyconomy.minestom;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import de.geolykt.easyconomy.api.BankStorageEngine;
import de.geolykt.easyconomy.api.EasyconomyEcoAPI;
import de.geolykt.easyconomy.api.PlayerDataStorage;
import de.geolykt.easyconomy.api.Saveable;
import de.geolykt.easyconomy.minestom.commands.AdministratorPermissions;
import de.geolykt.easyconomy.minestom.commands.BalanceCommand;
import de.geolykt.easyconomy.minestom.commands.GivemoneyCommand;
import de.geolykt.easyconomy.minestom.impl.BankDataEngine;
import de.geolykt.easyconomy.minestom.impl.DefaultEconomyProvider;
import de.geolykt.easyconomy.minestom.impl.PlayerDataEngine;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.extensions.Extension;
import net.minestom.server.utils.time.TimeUnit;

public class EasyconomyAdvanced extends Extension {

    private final Set<Saveable> toSave = new HashSet<Saveable>();
    private static EasyconomyAdvanced instance;
    private EasyconomyEcoAPI economy;

    private void saveAll() {
        Set<Saveable> erroringSaveables = new HashSet<>();
        for (Saveable saveable : toSave) {
            getLogger().info("Saving " + saveable.getClass().getSimpleName() + "...");
            try {
                saveable.save();
            } catch (IOException e) {
                e.printStackTrace();
                erroringSaveables.add(saveable);
            }
        }
        if (toSave.removeAll(erroringSaveables)) {
            // TODO disable extension?
        }
    }

    @Override
    public void preInitialize() {
        if (instance != null) {
            throw new RuntimeException();
        }
        instance = this;
        // TODO provide service
    }

    @Override
    public void initialize() {
        if (economy == null) {
            // FIXME Minestom makes use of StorageManager, we should too!
            File parent = new File(MinecraftServer.getExtensionManager().getExtensionFolder(), "easyconomy");
            parent.mkdir();
            PlayerDataStorage pds = new PlayerDataEngine(new File(parent, "players.dat"));
            BankStorageEngine bds = new BankDataEngine(new File(parent, "banks.dat"));
            registerEconomy(new DefaultEconomyProvider(this, pds, bds));
        }
        // TODO provide commands
        MinecraftServer.getSchedulerManager().buildTask(this::saveAll)
            .repeat(10, TimeUnit.MINUTE)
            .delay(10, TimeUnit.MINUTE).schedule();
        MinecraftServer.getSchedulerManager().buildShutdownTask(this::saveAll).schedule();
        MinecraftServer.getConnectionManager()
                .addPlayerInitialization((Player p) -> getEconomy().createPlayer(p.getUuid()));
        MinecraftServer.getCommandManager().register(new BalanceCommand(this));
        MinecraftServer.getCommandManager().register(new GivemoneyCommand(this, new AdministratorPermissions()));
    }

    @Override
    public void postInitialize() {
        getLogger().info("EasyconomyAdvanced is making use of " + economy.getClass().getSimpleName() 
                + " as the active economy.");
    }

    @Override
    public void preTerminate() {
        getLogger().info("Saving easyconomy saveables...");
        this.saveAll();
        toSave.clear();
    }

    public void addSaveable(@NotNull Saveable saveable) {
        toSave.add(saveable);
    }

    public static void registerEconomy(@NotNull EasyconomyEcoAPI eco) {
        if (instance.economy != null) {
            instance.saveAll();
            instance.toSave.remove(instance.economy.getBankStorage());
            instance.toSave.remove(instance.economy.getPlayerDataStorage());
        }
        instance.economy = eco;
        instance.toSave.add(eco.getBankStorage());
        instance.toSave.add(eco.getPlayerDataStorage());
    }

    public static @NotNull EasyconomyEcoAPI getEconomy() {
        return instance.economy;
    }

    @Override
    public void terminate() {
        // TODO
    }
}
