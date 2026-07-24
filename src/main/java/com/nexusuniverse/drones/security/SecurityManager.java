package com.nexusuniverse.drones.security;

import com.nexusuniverse.drones.AlertSettings;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SecurityManager {

    private final List<SecurityCamera> cameras = new ArrayList<>();
    private final List<LaserTripwire> lasers = new ArrayList<>();
    private final List<GhostDoor> ghostDoors = new ArrayList<>();
    private final AlertSettings alertSettings;

    // pending laser first-click, per player
    private final java.util.Map<UUID, Location> pendingLaserPoint = new java.util.HashMap<>();

    // per camera-owner set of players their cameras won't alert on
    private final java.util.Map<UUID, Set<UUID>> ignoredByOwner = new java.util.HashMap<>();

    public SecurityManager(AlertSettings alertSettings) {
        this.alertSettings = alertSettings;
    }

    public void addCamera(SecurityCamera camera) {
        cameras.add(camera);
    }

    public void addLaser(LaserTripwire laser) {
        lasers.add(laser);
    }

    public void addGhostDoor(GhostDoor door) {
        ghostDoors.add(door);
    }

    public void tickAll() {
        cameras.forEach(c -> c.tick(alertSettings.getCooldownMs(c.getOwnerId()), getIgnored(c.getOwnerId())));
        lasers.forEach(l -> l.tick(alertSettings.getCooldownMs(l.getOwnerId())));
    }

    /** Toggles ignore status and returns the new state (true = now ignored). */
    public boolean toggleIgnore(UUID ownerId, UUID targetId) {
        Set<UUID> ignored = ignoredByOwner.computeIfAbsent(ownerId, id -> new HashSet<>());
        if (ignored.remove(targetId)) {
            return false;
        }
        ignored.add(targetId);
        return true;
    }

    public Set<UUID> getIgnored(UUID ownerId) {
        return ignoredByOwner.getOrDefault(ownerId, Set.of());
    }

    public List<SecurityCamera> camerasFor(UUID ownerId) {
        return cameras.stream().filter(c -> c.getOwnerId().equals(ownerId)).toList();
    }

    public SecurityCamera findCamera(UUID ownerId, String name) {
        return cameras.stream()
                .filter(c -> c.getOwnerId().equals(ownerId) && c.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public GhostDoor findGhostDoor(Location location) {
        return ghostDoors.stream()
                .filter(d -> d.getLocation().equals(location))
                .findFirst().orElse(null);
    }

    public void setPendingLaserPoint(UUID playerId, Location location) {
        pendingLaserPoint.put(playerId, location);
    }

    public Location takePendingLaserPoint(UUID playerId) {
        return pendingLaserPoint.remove(playerId);
    }

    public int deviceCount() {
        return cameras.size() + lasers.size() + ghostDoors.size();
    }

    public void clearAll() {
        cameras.clear();
        lasers.clear();
        ghostDoors.clear();
        pendingLaserPoint.clear();
    }
}
