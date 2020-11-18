package dev.wwst.easyconomy.utils;

import dev.wwst.easyconomy.Easyconomy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Weiiswurst
 */
public class MessageTranslator {

    private static final String[] translations = new String[] {
            "de",
            "en",
            "fr"
    };

    private final Easyconomy plugin;
    private final String language;
    private final String prefix;
    private final YamlConfiguration cfg;


    private final HashMap<String, String> messages;

    public MessageTranslator(@NotNull String language, @NotNull Easyconomy invokingPlugin) {
        this.plugin = invokingPlugin;
        prefix = invokingPlugin.getConfig().getString("prefix");
        messages = new HashMap<>();
        this.language = language;

        saveDefaults();

        File languageFile = new File("plugins/EasyConomy/messages_"+language+".yml");
        if(!languageFile.exists()) {
            plugin.getLogger().log(Level.SEVERE, "!!! The language chosen by you, "+language+", cannot be resolved!");
            plugin.getLogger().log(Level.SEVERE, "!!! Create a file called messages_"+language+".yml in the EasyConomy folder to start!");
            plugin.getLogger().log(Level.SEVERE, "!!! For now, the ENGLISH language file will be loaded!");
            languageFile =new File("plugins/EasyConomy/messages_en.yml");
            System.out.println(languageFile.exists());
            System.out.println(languageFile.getAbsolutePath());
            System.out.println(languageFile.getName());
        }
        cfg = YamlConfiguration.loadConfiguration(languageFile);
        Map<String, Object> values = cfg.getValues(true);
        for(String key : values.keySet()) {
            messages.put(key, values.get(key).toString());
        }
        plugin.getLogger().log(Level.INFO, "Language loaded: messages_"+language+".yml");
    }

    // Loading custom language files for addons
    public void loadMessageFile(@NotNull String path) {
        File languageFile = new File(path);
        if(!languageFile.exists()) {
            plugin.getLogger().log(Level.SEVERE, "Could not find a config file at "+path);
            return;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(languageFile);
        Map<String, Object> values = cfg.getValues(true);
        for(String key : values.keySet()) {
            messages.put(key, values.get(key).toString());
        }
        plugin.getLogger().log(Level.INFO, "Custom language file loaded: "+path);
    }

    private void saveDefaults() {
        for(String translation : translations) {
            plugin.saveResource("messages_"+translation+".yml", true);
            plugin.getLogger().log(Level.INFO, "Default language exported: messages_"+translation+".yml");
        }
    }

    @NotNull
    public String getMessageAndReplace(@NotNull String key, boolean addPrefix, @NotNull Object... replacements) {
        if(!messages.containsKey(key)) {
            return ChatColor.YELLOW+key+ ChatColor.RED +" not found!";
        }
        String message = (addPrefix ? prefix : "") + String.format(messages.get(key), replacements);

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @NotNull
    public String getMessage(@NotNull String key, boolean addPrefix) {
        return getMessageAndReplace(key, addPrefix, null, "");
    }

    @NotNull
    public String getMessage(@NotNull String key) {
        return getMessageAndReplace(key, false, null, "");
    }

    @NotNull
    public YamlConfiguration getConfiguration() {
        return cfg;
    }

    @NotNull
    public String getLanguage() {
        return language;
    }
}