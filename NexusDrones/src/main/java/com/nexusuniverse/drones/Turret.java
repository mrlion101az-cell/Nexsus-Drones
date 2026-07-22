package com.nexusuniverse.drones;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * A stationary turret: an ArmorStand posed with a crossbow, holding its
 * position, rotating to face the nearest enemy in range, and firing real
 * Arrow projectiles on a cooldown.
 */
public class Turret {

    private static final double SCAN_RADIUS = 25.0;
    private static final double ARROW_SPEED = 3.0;
    private static final int FIRE_COOLDOWN_TICKS = 30; // 1.5s at 20tps

    private final UUID ownerId;
    private ArmorStand entity;
    private boolean dead = false;
    private int cooldown = 0;

    public Turret(Player owner, Location spawnLocation) {
        this.ownerId = owner.getUniqueId();
        spawn(spawnLocation.clone().add(0, 1, 0));
    }

    private void spawn(Location location) {
        entity = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        entity.setInvisible(false);
        entity.setGravity(false);
        entity.setInvulnerable(true);
        entity.setSmall(false);
        entity.setMarker(false);
        entity.setBasePlate(true);

        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            equipment.setItemInMainHand(new ItemStack(Material.CROSSBOW));
        }
    }

    public boolean isDead() {
        return dead || entity == null || entity.isDead();
    }

    public void remove() {
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
        dead = true;
    }

    public void tick() {
        if (isDead()) return;
        if (cooldown > 0) {
            cooldown--;
        }

        Location current = entity.getLocation();

        Player target = current.getWorld().getPlayers().stream()
                .filter(p -> !p.getUniqueId().equals(ownerId))
                .filter(p -> p.getLocation().distance(current) <= SCAN_RADIUS)
                .min((a, b) -> Double.compare(
                        a.getLocation().distance(current), b.getLocation().distance(current)))
                .orElse(null);

        if (target == null) return;

        Location targetLoc = target.getLocation().clone().add(0, 1, 0);
        Vector direction = targetLoc.toVector().subtract(current.toVector()).normalize();

        // face the target
        Location facing = current.clone();
        facing.setDirection(direction);
        entity.setRotation(facing.getYaw(), 0);

        if (cooldown <= 0) {
            fire(current.clone().add(0, 1.2, 0), direction);
            cooldown = FIRE_COOLDOWN_TICKS;
        }
    }

    private void fire(Location origin, Vector direction) {
        Arrow arrow = origin.getWorld().spawnArrow(origin, direction, (float) ARROW_SPEED, 0f);
        arrow.setShooter(null);
    }
}
