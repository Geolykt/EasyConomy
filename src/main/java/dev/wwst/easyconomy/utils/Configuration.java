package dev.wwst.easyconomy.utils;

import com.google.common.io.Files;
import dev.wwst.easyconomy.Easyconomy;
import dev.wwst.easyconomy.storage.Saveable;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Configuration implements Saveable {

    private final File file;
    private final FileConfiguration customFile;

    private static final int CURRENT_CONFIG_VERSION = 5;

    public Configuration(@NotNull Easyconomy plugin) throws IOException, InvalidConfigurationException {
        plugin.getLogger().log(Level.INFO, "Loading Configuration");

        file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists()) {
            plugin.saveResource("config.yml",false);
        }
        customFile = YamlConfiguration.loadConfiguration(file);
        int configVer = customFile.getInt("CONFIG_VERSION_NEVER_CHANGE_THIS");
        if(CURRENT_CONFIG_VERSION > configVer) {
            try {
                Files.copy(file, new File(plugin.getDataFolder(), "config_OLD_VERSION.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            plugin.saveResource("config.yml",true);
            plugin.getLogger().warning("!!! IT SEEMS LIKE YOU UPDATED EASYCONOMY !!!");
            plugin.getLogger().warning("!!! YOUR OLD config.yml WAS COPIED TO config_OLD_VERSION.yml !!!");
            plugin.getLogger().warning("!!! A NEW config.yml WITH UPDATED VALUES WAS PASTED TO config.yml INSTEAD !!!");
            plugin.getLogger().warning("!!! STOP THE SERVER TO CHANGE VALUES IN THE NEW config.yml !!!");
            customFile.load(file);
        }
    }

    /*
     ** Saves the current FileConfiguration to the file on the disk
     */
    @Override
    public void save() {
        customFile.options().copyDefaults(true);
        try {
            customFile.save(file);
        } catch (IOException e) { e.printStackTrace();}
    }

    public void write(@NotNull String path, @NotNull Object object) {
        if(customFile.contains(path)) {
            customFile.set(path, object);
        } else {
            customFile.addDefault(path, object);
        }
        save();
    }

    public void reload() throws IOException, InvalidConfigurationException {
        customFile.load(file);
    }

}