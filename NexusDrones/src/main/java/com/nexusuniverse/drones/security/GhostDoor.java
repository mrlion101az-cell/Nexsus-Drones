package com.nexusuniverse.drones.security;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.UUID;

/**
 * A secret door: remembers a block's original material, and toggles it
 * between that material (closed/solid) and AIR (open/passable). This is
 * shared for everyone once opened -- NOT per-player invisible. True
 * per-player ghosting (solid to intruders, passable to the owner only)
 * needs packet-level block spoofing, which is a phase-2 item if wanted.
 */
public class GhostDoor {

    private final UUID ownerId;
    private final Location location;
    private final Material originalMaterial;
    private boolean open = false;

    public GhostDoor(UUID ownerId, Location location, Material originalMaterial) {
        this.ownerId = ownerId;
        this.location = location;
        this.originalMaterial = originalMaterial;
    }

    public void toggle() {
        Block block = location.getBlock();
        if (open) {
            block.setType(originalMaterial);
            open = false;
        } else {
            block.setType(Material.AIR);
            open = true;
        }
    }

    public boolean isOpen() {
        return open;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Location getLocation() {
        return location;
    }
}
