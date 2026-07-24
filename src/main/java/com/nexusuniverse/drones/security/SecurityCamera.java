package com.nexusuniverse.drones.security;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A named, stationary camera. Doesn't spawn an entity by default (keeps
 * server load down for large networks) -- just a location, a name, and a
 * scan radius. Ticks alongside everything else to spot intruders.
 *
 * Cooldown and the ignore list are passed in at tick time rather than
 * stored on the camera itself, since both are per-owner settings shared
 * across all of that owner's cameras (see AlertSettings /
 * SecurityManager's ignore list).
 */
public class SecurityCamera {

    private static final double SCAN_RADIUS = 15.0;

    private final String name;
    private final UUID ownerId;
    private final Location location;
    private long lastAlert = 0;

    public SecurityCamera(String name, UUID ownerId, Location location) {
        this.name = name;
        this.ownerId = ownerId;
        this.location = location;
    }

    public void tick(long cooldownMs, Set<UUID> ignoredPlayers) {
        Player owner = Bukkit.getPlayer(ownerId);

        List<Player> nearby = location.getWorld().getPlayers().stream()
                .filter(p -> !p.getUniqueId().equals(ownerId))
                .filter(p -> !ignoredPlayers.contains(p.getUniqueId()))
                .filter(p -> p.getLocation().distance(location) <= SCAN_RADIUS)
                .toList();

        if (nearby.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (now - lastAlert < cooldownMs) return;
        lastAlert = now;

        if (owner != null) {
            for (Player intruder : nearby) {
                owner.sendMessage("§e[Camera: " + name + "] §fMotion detected -- §c" + intruder.getName());
            }
        }
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Location getLocation() {
        return location;
    }
}
