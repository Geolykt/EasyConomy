package de.geolykt.easyconomy.minestom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import net.minestom.server.chat.ChatColor;

/**
 * Wrapper to obtain configuration values for the extension. 
 * The configuration values are mapped within a HOCON (Human-Optimized Config Object Notation) file
 *  so the configurations can be altered by the user easily.
 * @author Geolykt
 * @since 1.1.0
 *
 */
public class EasyconomyConfiguration {

    private final @NotNull CommentedConfigurationNode config;

    public EasyconomyConfiguration(File configfile) throws RuntimeException {
        if (!configfile.exists()) {
            try {
                configfile.createNewFile();
                InputStream defaultConf = getClass().getResourceAsStream("/config.conf");
                FileOutputStream fio = new FileOutputStream(configfile);
                defaultConf.transferTo(fio);
                fio.flush();
                fio.close();
                defaultConf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            CommentedConfigurationNode t =  HoconConfigurationLoader.builder().file(configfile).build().load();
            if (t == null) {
                throw new IllegalStateException("For some reason or another the main config node is null.");
            }
            config = t;
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull String getEcoFormat() {
        return getString("ecoformat", "$%.2f");
    }

    public int getMaximumBaltopSize() {
        return config.node("maximum-baltop-size").getInt(25);
    }

    public int getBaltopPageSize() {
        return config.node("baltop-page-size").getInt(25);
    }

    public @NotNull String getBaltopHeader() {
        return getString("baltop-header", ChatColor.DARK_GREEN + "Here are the %d richest players:");
    }

    public @NotNull String getBaltopEntry() {
        return getString("baltop-entry", ChatColor.DARK_GREEN + "  %02d.: " +  ChatColor.BRIGHT_GREEN + "%s - %s");
    }

    public @NotNull String getSelfBalance() {
        return getString("balance-self", ChatColor.BRIGHT_GREEN + "Your balance: " + ChatColor.CYAN + "%s");
    }

    public @NotNull String getOthersBalance() {
        return getString("balance-other", ChatColor.BRIGHT_GREEN + "Balance of " 
                + ChatColor.DARK_GREEN + "%s" + ChatColor.BRIGHT_GREEN + " is " 
                + ChatColor.CYAN + "%s" + ChatColor.BRIGHT_GREEN + ".");
    }

    public @NotNull String getNotAPlayer() {
        return getString("error-not-a-player", ChatColor.DARK_RED + "Only players can execute this command.");
    }

    public @NotNull String getInvalidPlayer() {
        return getString("error-invalid-player", ChatColor.DARK_RED + "You did not specify a valid player.");
    }

    public @NotNull String getNotPermitted() {
        return getString("error-unpermitted", ChatColor.DARK_RED + "You are not permitted to use this command.");
    }

    public @NotNull String getAdminPermission() {
        return getString("permission-admin", "easyconomy.admin");
    }

    private @NotNull String getString(@NotNull String node, @NotNull String defaultValue) {
        CommentedConfigurationNode cfgNode = config.node(node);
        String str = cfgNode.getString(defaultValue);
        if (str == null) {
            str = defaultValue;
        }
        return str;
    }
}
