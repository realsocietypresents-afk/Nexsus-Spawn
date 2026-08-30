package com.nexusuniverse.spawn;

import com.nexusuniverse.spawn.config.NexusSpawnConfig;
import com.nexusuniverse.spawn.protection.AdminBypassManager;
import com.nexusuniverse.spawn.protection.ProtectionListener;
import com.nexusuniverse.spawn.protection.ProtectionZone;
import org.bukkit.plugin.java.JavaPlugin;

public class NexusSpawnPlugin extends JavaPlugin {

    private NexusSpawnConfig config;
    private ProtectionZone zone;
    private AdminBypassManager bypass;

    @Override
    public void onEnable() {
        this.config = new NexusSpawnConfig(this);
        this.zone = new ProtectionZone(config);
        this.bypass = new AdminBypassManager();

        getServer().getPluginManager().registerEvents(new ProtectionListener(config, zone, bypass), this);
        getCommand("nexusspawn").setExecutor(new NexusSpawnCommand(config, zone, bypass));

        getLogger().info("NexusSpawn enabled -- protecting a " + config.radiusChunks()
                + "-chunk radius " + (config.useWorldSpawnAsCenter() ? "around world spawn" : "around a fixed center point")
                + ". Build/break are always blocked there; doors/buttons/pressure-plates/chests/"
                + "ender-chests/shulker-boxes stay usable per config.yml's allow list.");
    }
}
