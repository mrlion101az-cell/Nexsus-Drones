package com.nexusuniverse.drones.security;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SecurityManager {

    private final List<SecurityCamera> cameras = new ArrayList<>();
    private final List<LaserTripwire> lasers = new ArrayList<>();
    private final List<GhostDoor> ghostDoors = new ArrayList<>();

    // pending laser first-click, per player
    private final java.util.Map<UUID, Location> pendingLaserPoint = new java.util.HashMap<>();

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
        cameras.forEach(SecurityCamera::tick);
        lasers.forEach(LaserTripwire::tick);
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
