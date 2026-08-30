package com.nexusuniverse.spawn;

import com.nexusuniverse.spawn.config.NexusSpawnConfig;
import com.nexusuniverse.spawn.protection.AdminBypassManager;
import com.nexusuniverse.spawn.protection.ProtectionZone;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NexusSpawnCommand implements CommandExecutor {

    private final NexusSpawnConfig config;
    private final ProtectionZone zone;
    private final AdminBypassManager bypass;

    public NexusSpawnCommand(NexusSpawnConfig config, ProtectionZone zone, AdminBypassManager bypass) {
        this.config = config;
        this.zone = zone;
        this.bypass = bypass;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7Usage: /nexusspawn <reload|bypass|status>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                config.reload();
                sender.sendMessage("§aNexusSpawn config reloaded -- radius/center/allow-list changes apply immediately.");
            }
            case "bypass" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cConsole always bypasses -- this toggle is for players only.");
                    return true;
                }
                if (!player.hasPermission("nexusspawn.bypass")) {
                    sender.sendMessage("§cYou don't have nexusspawn.bypass.");
                    return true;
                }
                boolean nowBypassing = bypass.toggle(player.getUniqueId());
                sender.sendMessage(nowBypassing
                        ? "§aBypass is now ON -- you build/break/interact freely inside the zone."
                        : "§eBypass is now OFF -- you're protected by the zone just like a normal player.");
            }
            case "status" -> {
                World world = zone.world();
                sender.sendMessage("§7--- NexusSpawn Status ---");
                sender.sendMessage("§7Enabled: " + (config.enabled() ? "§atrue" : "§cfalse"));
                sender.sendMessage("§7World: §f" + (world != null ? world.getName() : "§c(none found)"));
                sender.sendMessage("§7Radius: §f" + config.radiusChunks() + " chunks");
                sender.sendMessage("§7Center: §f" + (config.useWorldSpawnAsCenter()
                        ? "world spawn"
                        : config.centerX() + ", " + config.centerZ() + " (fixed)"));
                sender.sendMessage("§7Allowed inside zone: §f"
                        + (config.allowDoors() ? "doors " : "")
                        + (config.allowButtons() ? "buttons " : "")
                        + (config.allowPressurePlates() ? "pressure-plates " : "")
                        + (config.allowChests() ? "chests " : "")
                        + (config.allowEnderChests() ? "ender-chests " : "")
                        + (config.allowShulkerBoxes() ? "shulker-boxes " : ""));
            }
            default -> sender.sendMessage("§cUnknown subcommand. Usage: /nexusspawn <reload|bypass|status>");
        }
        return true;
    }
}
