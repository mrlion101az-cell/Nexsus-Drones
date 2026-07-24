package com.nexusuniverse.drones;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

/**
 * A drone: an invisible, gravity-disabled ArmorStand whose position we
 * drive manually every tick, rather than relying on Minecraft AI/pathing.
 * Two behaviors depending on type: SURVEILLANCE (patrol + spot) or
 * KAMIKAZE (home in on a target and explode on proximity).
 */
public class Drone {

    private static final double SCAN_RADIUS = 20.0;
    private static final double PATROL_SPEED = 0.15;
    private static final double CHASE_SPEED = 0.25;
    private static final double DETONATE_DISTANCE = 2.0;
    private static final double HOVER_HEIGHT = 6.0;

    private final DeployableType type;
    private final UUID ownerId;
    private ArmorStand entity;
    private boolean dead = false;

    // patrol state
    private double patrolAngle = 0;
    private Location patrolCenter;

    public Drone(DeployableType type, Player owner, Location spawnLocation) {
        this.type = type;
        this.ownerId = owner.getUniqueId();
        this.patrolCenter = spawnLocation.clone().add(0, HOVER_HEIGHT, 0);
        spawn(spawnLocation.clone().add(0, HOVER_HEIGHT, 0));
    }

    private void spawn(Location location) {
        entity = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        entity.setInvisible(true);
        entity.setGravity(false);
        entity.setInvulnerable(true);
        entity.setSilent(false);
        entity.setSmall(true);
        entity.setMarker(false);
        entity.setCustomNameVisible(false);
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

        Player owner = entity.getServer().getPlayer(ownerId);
        Location current = entity.getLocation();

        if (type == DeployableType.SURVEILLANCE_DRONE) {
            tickSurveillance(current, owner);
        } else if (type == DeployableType.KAMIKAZE_DRONE) {
            tickKamikaze(current, owner);
        }
    }

    private void tickSurveillance(Location current, Player owner) {
        // simple circular patrol around the spawn point
        patrolAngle += 0.05;
        double x = patrolCenter.getX() + Math.cos(patrolAngle) * 4;
        double z = patrolCenter.getZ() + Math.sin(patrolAngle) * 4;
        Location next = new Location(current.getWorld(), x, patrolCenter.getY(), z);
        entity.teleport(next);

        List<Player> nearby = current.getWorld().getPlayers().stream()
                .filter(p -> !p.getUniqueId().equals(ownerId))
                .filter(p -> p.getLocation().distance(current) <= SCAN_RADIUS)
                .toList();

        for (Player spotted : nearby) {
            spotted.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, false, false));
            if (owner != null) {
                owner.sendMessage("§b[Surveillance] §fSpotted §e" + spotted.getName() +
                        "§f near §7(" + spotted.getLocation().getBlockX() + ", " +
                        spotted.getLocation().getBlockZ() + ")");
            }
        }
    }

    private void tickKamikaze(Location current, Player owner) {
        Player target = current.getWorld().getPlayers().stream()
                .filter(p -> !p.getUniqueId().equals(ownerId))
                .filter(p -> p.getLocation().distance(current) <= SCAN_RADIUS)
                .min((a, b) -> Double.compare(
                        a.getLocation().distance(current), b.getLocation().distance(current)))
                .orElse(null);

        if (target == null) {
            // no target: hold position, gentle hover bob
            return;
        }

        Location targetLoc = target.getLocation().clone().add(0, 1, 0);
        double distance = targetLoc.distance(current);

        if (distance <= DETONATE_DISTANCE) {
            detonate(current);
            return;
        }

        Vector direction = targetLoc.toVector().subtract(current.toVector()).normalize();
        Location next = current.clone().add(direction.multiply(CHASE_SPEED));
        entity.teleport(next);
    }

    private void detonate(Location location) {
        location.getWorld().createExplosion(location, 3.0f, false, true);
        remove();
    }

    public DeployableType getType() {
        return type;
    }
}
