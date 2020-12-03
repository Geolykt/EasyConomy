package de.geolykt.easyconomy.minestom;

import java.io.File;

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

    private final CommentedConfigurationNode config;

    public EasyconomyConfiguration(File configfile) throws ConfigurateException {
        config =  HoconConfigurationLoader.builder().file(configfile).build().load();
    }

    public String getEcoFormat() {
        return config.node("ecoformat").getString("$%.2f");
    }

    public int getMaximumBaltopSize() {
        return config.node("maximum-baltop-size").getInt(25);
    }

    public int getBaltopPageSize() {
        return config.node("baltop-page-size").getInt(25);
    }

    public String getBaltopHeader() {
        return config.node("baltop-header").getString(ChatColor.DARK_GREEN + "Here are the %d richest players:");
    }

    public String getBaltopEntry() {
        return config.node("baltop-entry").getString(ChatColor.DARK_GREEN + "  %02d.: " +  ChatColor.BRIGHT_GREEN + "%s - %s");
    }

    public String getSelfBalance() {
        return config.node("balance-self").getString(ChatColor.BRIGHT_GREEN + "Your balance: " + ChatColor.CYAN + "%s");
    }

    public String getOthersBalance() {
        return config.node("balance-other").getString(ChatColor.BRIGHT_GREEN + "Balance of " 
                + ChatColor.DARK_GREEN + "%s" + ChatColor.BRIGHT_GREEN + " is " 
                + ChatColor.CYAN + "%s" + ChatColor.BRIGHT_GREEN + ".");
    }

    public String getNotAPlayer() {
        return config.node("error-not-a-player").getString(ChatColor.RED + "Only players can execute this command.");
    }

    public String getInvalidPlayer() {
        return config.node("error-invalid-player").getString(ChatColor.RED + "You did not specify a valid player.");
    }

    public String getNotPermitted() {
        return config.node("error-unpermitted").getString(ChatColor.DARK_RED + "You are not permitted to use this command.");
    }
}
