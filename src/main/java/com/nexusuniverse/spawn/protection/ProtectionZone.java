package com.nexusuniverse.spawn.protection;

import com.nexusuniverse.spawn.config.NexusSpawnConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * A single square (chessboard-distance) zone in CHUNK coordinates, centered on either the
 * world's own actual spawn point or a fixed configured x/z -- same "radius in chunks" convention
 * the rest of the Nexus plugin family already uses for its own spawn-protection zones, so a
 * server admin who's used one of those already knows how this one behaves.
 *
 * Deliberately re-reads the config's radius/center/world on every check rather than caching them
 * at construction, so /nexusspawn reload takes effect immediately without needing to rebuild
 * this object.
 */
public class ProtectionZone {

    private final NexusSpawnConfig config;

    public ProtectionZone(NexusSpawnConfig config) {
        this.config = config;
    }

    /** The world this zone actually applies to -- the configured world name, or the server's first/default world if left blank. */
    public World world() {
        String configuredName = config.world();
        if (configuredName != null && !configuredName.isBlank()) {
            World named = Bukkit.getWorld(configuredName);
            if (named != null) return named;
        }
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
    }

    public boolean contains(Block block) {
        return contains(block.getWorld(), block.getX(), block.getZ());
    }

    public boolean contains(Location location) {
        if (location.getWorld() == null) return false;
        return contains(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    public boolean contains(World world, int blockX, int blockZ) {
        World zoneWorld = world();
        if (zoneWorld == null || !zoneWorld.equals(world)) return false;

        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        int[] center = centerChunk(zoneWorld);

        int dx = Math.abs(chunkX - center[0]);
        int dz = Math.abs(chunkZ - center[1]);
        return Math.max(dx, dz) <= config.radiusChunks();
    }

    /** [chunkX, chunkZ] of the configured center point, in this world. */
    private int[] centerChunk(World world) {
        if (config.useWorldSpawnAsCenter()) {
            Location spawn = world.getSpawnLocation();
            return new int[]{spawn.getBlockX() >> 4, spawn.getBlockZ() >> 4};
        }
        return new int[]{config.centerX() >> 4, config.centerZ() >> 4};
    }
}
