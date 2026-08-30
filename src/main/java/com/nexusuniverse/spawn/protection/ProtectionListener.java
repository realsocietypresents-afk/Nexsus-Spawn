package com.nexusuniverse.spawn.protection;

import com.nexusuniverse.spawn.config.NexusSpawnConfig;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Iterator;
import java.util.List;

/**
 * The whole plugin's enforcement, in one listener. Two very different rules:
 *
 *  1. Building and breaking (block place/break, and the bucket-empty/fill equivalents of placing/
 *     removing a liquid) are ALWAYS denied inside the zone for anyone without nexusspawn.bypass --
 *     there's no allow-list for these, matching the direct request ("I pretty much don't want them
 *     to place or break anything").
 *  2. Everyday amenity interactions (doors/trapdoors/gates, buttons, pressure plates, chests/
 *     trapped chests/barrels, ender chests, shulker boxes) are explicitly ALLOWED even inside the
 *     zone, each independently toggleable in config.yml. Any OTHER right-click-a-block interaction
 *     not on that list (crafting tables, furnaces, anvils, beds, etc) is denied by default, since
 *     this is meant to be a locked-down "look but don't set up shop" area -- not a general-purpose
 *     land claim.
 *
 * Explosions get a third, narrower rule: the explosion itself (and any entity damage it deals)
 * still happens, but any block inside the zone is stripped from the event's block list so it
 * survives -- "forces protection" needs to cover griefing via TNT/creepers too, not just direct
 * player break/place.
 */
public class ProtectionListener implements Listener {

    private final NexusSpawnConfig config;
    private final ProtectionZone zone;
    private final AdminBypassManager bypass;

    public ProtectionListener(NexusSpawnConfig config, ProtectionZone zone, AdminBypassManager bypass) {
        this.config = config;
        this.zone = zone;
        this.bypass = bypass;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!config.enabled() || !config.denyBreak()) return;
        if (bypass.isBypassing(event.getPlayer())) return;
        if (!zone.contains(event.getBlock())) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(config.breakDeniedMessage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!config.enabled() || !config.denyBuild()) return;
        if (bypass.isBypassing(event.getPlayer())) return;
        if (!zone.contains(event.getBlockPlaced())) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(config.buildDeniedMessage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!config.enabled() || !config.denyBuild()) return;
        if (bypass.isBypassing(event.getPlayer())) return;
        // the block actually about to become water/lava/etc is the one adjacent to the clicked
        // block in the direction the player is facing, not the clicked block itself
        Block target = event.getBlock().getRelative(event.getBlockFace());
        if (!zone.contains(target)) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(config.buildDeniedMessage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!config.enabled() || !config.denyBreak()) return;
        if (bypass.isBypassing(event.getPlayer())) return;
        if (!zone.contains(event.getBlock())) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(config.breakDeniedMessage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!config.enabled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (bypass.isBypassing(event.getPlayer())) return;
        if (!zone.contains(block)) return;
        if (isAllowedInteraction(block)) return;

        event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
        // still let them use whatever's in their hand normally (eating, drinking, etc) as long as
        // it isn't itself a block placement -- that's separately covered by onPlace/onBucketEmpty
        event.getPlayer().sendMessage(config.interactDeniedMessage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!config.enabled() || !config.denyExplosionDamage()) return;
        stripZoneBlocks(event.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!config.enabled() || !config.denyExplosionDamage()) return;
        stripZoneBlocks(event.blockList());
    }

    private void stripZoneBlocks(List<Block> blocks) {
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            if (zone.contains(it.next())) it.remove();
        }
    }

    private boolean isAllowedInteraction(Block block) {
        Material type = block.getType();

        if (config.allowDoors() && (isTagged(Tag.DOORS, type) || isTagged(Tag.TRAPDOORS, type) || isTagged(Tag.FENCE_GATES, type))) {
            return true;
        }
        if (config.allowButtons() && isTagged(Tag.BUTTONS, type)) {
            return true;
        }
        if (config.allowPressurePlates() && isTagged(Tag.PRESSURE_PLATES, type)) {
            return true;
        }
        if (config.allowChests() && (type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.BARREL)) {
            return true;
        }
        if (config.allowEnderChests() && type == Material.ENDER_CHEST) {
            return true;
        }
        if (config.allowShulkerBoxes() && block.getState() instanceof ShulkerBox) {
            return true;
        }
        return false;
    }

    private boolean isTagged(Tag<Material> tag, Material type) {
        return tag.isTagged(type);
    }
}
