package com.nexusuniverse.drones;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-owner cooldown for every owner-facing chat alert in the plugin --
 * Surveillance Drone "spotted" messages, Security Camera motion alerts,
 * and Laser Tripwire alarms all read from this instead of each having
 * their own hardcoded constant. Per-owner (not global) so one player
 * adjusting their own cooldown can't change how often anyone else's
 * devices alert.
 */
public class AlertSettings {

    private static final long DEFAULT_COOLDOWN_MS = 10_000; // 10 seconds
    private static final long MIN_COOLDOWN_MS = 1_000; // 1 second floor -- no zero/negative spam

    private final Map<UUID, Long> cooldownByOwner = new HashMap<>();

    public long getCooldownMs(UUID ownerId) {
        return cooldownByOwner.getOrDefault(ownerId, DEFAULT_COOLDOWN_MS);
    }

    public int getCooldownSeconds(UUID ownerId) {
        return (int) (getCooldownMs(ownerId) / 1000);
    }

    public void setCooldownSeconds(UUID ownerId, int seconds) {
        cooldownByOwner.put(ownerId, Math.max(MIN_COOLDOWN_MS, seconds * 1000L));
    }
}
