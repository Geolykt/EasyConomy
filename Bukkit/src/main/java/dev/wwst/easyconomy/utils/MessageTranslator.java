/*
 * EasyconomyAdvanced, a lightweight economy plugin
 * Copyright (C) Weiiswurst
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
package dev.wwst.easyconomy.utils;

import dev.wwst.easyconomy.Easyconomy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Weiiswurst, Geolykt
 */
public class MessageTranslator {

    /**
     * The base translations included the in the plugin jar that need to be extracted.
     */
    private static final String[] TRANSLATIONS = new String[] {
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

        String filename = "messages_" + language + ".yml";
        File languageFile = new File(invokingPlugin.getDataFolder(), filename);
        if(!languageFile.exists()) {
            plugin.getLogger().severe("!!! The language chosen by you, " + language + ", cannot be resolved!");
            plugin.getLogger().severe("!!! Create a file called messages_" + language + ".yml in the EasyConomy folder to start!");
            plugin.getLogger().severe("!!! For now, the ENGLISH language file will be loaded!");
            languageFile = new File(invokingPlugin.getDataFolder(), "messages_en.yml");
        }
        cfg = YamlConfiguration.loadConfiguration(languageFile);
        Map<String, Object> values = cfg.getValues(true);
        for(String key : values.keySet()) {
            messages.put(key, values.get(key).toString());
        }
        plugin.getLogger().info("Language loaded: " + filename);
    }

    // Loading custom language files for add-ons
    @Deprecated(forRemoval = true, since = "1.2.0")
    public void loadMessageFile(@NotNull String path) {
        File languageFile = new File(path);
        if(!languageFile.exists()) {
            plugin.getLogger().severe("Could not find a config file at "+path);
            return;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(languageFile);
        Map<String, Object> values = cfg.getValues(true);
        for(String key : values.keySet()) {
            messages.put(key, values.get(key).toString());
        }
        plugin.getLogger().info("Custom language file loaded: "+path);
    }

    private void saveDefaults() {
        for(String translation : TRANSLATIONS) {
            String filename = "messages_" + translation + ".yml";
            if (new File(plugin.getDataFolder(), filename).exists()) {
                plugin.saveResource(filename, true);
                plugin.getLogger().info("Default language exported: messages_"+translation+".yml");
            }
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