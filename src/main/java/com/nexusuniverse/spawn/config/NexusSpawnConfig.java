package com.nexusuniverse.spawn.config;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class NexusSpawnConfig {

    private final JavaPlugin plugin;

    public NexusSpawnConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        // copyDefaults(true) + saveConfig() merges in anything a later update adds to an
        // already-existing config.yml on disk, instead of a new key silently never showing up --
        // same pattern every other Nexus plugin's config class uses.
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public boolean enabled() {
        return plugin.getConfig().getBoolean("protection.enabled", true);
    }

    /** Blank ("") means "use the server's first/default world." */
    public String world() {
        return plugin.getConfig().getString("protection.world", "");
    }

    public int radiusChunks() {
        return Math.max(0, plugin.getConfig().getInt("protection.radius-chunks", 300));
    }

    public boolean useWorldSpawnAsCenter() {
        return plugin.getConfig().getBoolean("protection.center.use-world-spawn", true);
    }

    public int centerX() {
        return plugin.getConfig().getInt("protection.center.x", 0);
    }

    public int centerZ() {
        return plugin.getConfig().getInt("protection.center.z", 0);
    }

    public boolean allowDoors() {
        return plugin.getConfig().getBoolean("protection.allow.doors", true);
    }

    public boolean allowButtons() {
        return plugin.getConfig().getBoolean("protection.allow.buttons", true);
    }

    public boolean allowPressurePlates() {
        return plugin.getConfig().getBoolean("protection.allow.pressure-plates", true);
    }

    public boolean allowChests() {
        return plugin.getConfig().getBoolean("protection.allow.chests", true);
    }

    public boolean allowEnderChests() {
        return plugin.getConfig().getBoolean("protection.allow.ender-chests", true);
    }

    public boolean allowShulkerBoxes() {
        return plugin.getConfig().getBoolean("protection.allow.shulker-boxes", true);
    }

    public boolean denyBuild() {
        return plugin.getConfig().getBoolean("protection.deny-build", true);
    }

    public boolean denyBreak() {
        return plugin.getConfig().getBoolean("protection.deny-break", true);
    }

    public boolean denyExplosionDamage() {
        return plugin.getConfig().getBoolean("protection.deny-explosion-damage", true);
    }

    public String buildDeniedMessage() {
        return colorize(plugin.getConfig().getString("messages.build-denied", "&cYou can't build here -- this area is protected."));
    }

    public String breakDeniedMessage() {
        return colorize(plugin.getConfig().getString("messages.break-denied", "&cYou can't break blocks here -- this area is protected."));
    }

    public String interactDeniedMessage() {
        return colorize(plugin.getConfig().getString("messages.interact-denied", "&cYou can't do that here -- this area is protected."));
    }

    private String colorize(String raw) {
        return raw == null ? "" : ChatColor.translateAlternateColorCodes('&', raw);
    }
}
