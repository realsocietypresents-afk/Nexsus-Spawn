package com.nexusuniverse.spawn.protection;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Bypass is ON automatically for anyone holding nexusspawn.bypass -- no toggle needed to start
 * using it. This class exists only to let an individual op switch it back OFF for themselves
 * temporarily (e.g. testing what a normal player actually experiences in the zone) without
 * changing anything permission-wide. Same pattern as NexusRealms' own AdminBypassManager.
 *
 * In-memory only, on purpose: a momentary "which mode am I in right now" toggle, not a persistent
 * setting -- everyone with the permission is back to bypass-on (the default) after every restart.
 */
public class AdminBypassManager {

    private final Set<UUID> disabled = new HashSet<>();

    public boolean isBypassing(Player player) {
        if (!player.hasPermission("nexusspawn.bypass")) return false;
        return !disabled.contains(player.getUniqueId());
    }

    /** @return the new state -- true means bypass is now ON for this player. */
    public boolean toggle(UUID playerId) {
        if (disabled.contains(playerId)) {
            disabled.remove(playerId);
            return true;
        }
        disabled.add(playerId);
        return false;
    }
}
