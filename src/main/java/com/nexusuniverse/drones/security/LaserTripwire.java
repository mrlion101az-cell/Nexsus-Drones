package com.nexusuniverse.drones.security;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A laser between two fixed points. On tick, draws a particle line (so
 * everyone can see it -- a visible laser is more fun and more fair than
 * an invisible tripwire) and checks every online player's distance to
 * the line segment; anyone but the owner within the trigger distance
 * sets off the alarm, with a per-player cooldown to avoid spam.
 */
public class LaserTripwire {

    private static final double TRIGGER_DISTANCE = 0.6;

    private final UUID ownerId;
    private final Location pointA;
    private final Location pointB;
    private final Map<UUID, Long> lastAlarm = new HashMap<>();

    public LaserTripwire(UUID ownerId, Location pointA, Location pointB) {
        this.ownerId = ownerId;
        this.pointA = pointA;
        this.pointB = pointB;
    }

    public void tick(long cooldownMs) {
        drawBeam();
        checkCrossings(cooldownMs);
    }

    private void drawBeam() {
        Vector direction = pointB.toVector().subtract(pointA.toVector());
        double length = direction.length();
        if (length < 0.01) return;
        Vector step = direction.clone().normalize().multiply(0.5);

        Location cursor = pointA.clone().add(0.5, 0.5, 0.5);
        int steps = (int) (length / 0.5);
        for (int i = 0; i <= steps; i++) {
            pointA.getWorld().spawnParticle(Particle.DUST, cursor, 1,
                    new Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
            cursor.add(step);
        }
    }

    private void checkCrossings(long cooldownMs) {
        Player owner = org.bukkit.Bukkit.getPlayer(ownerId);

        for (Player player : pointA.getWorld().getPlayers()) {
            if (player.getUniqueId().equals(ownerId)) continue;

            double distance = distanceToSegment(player.getLocation().toVector());
            if (distance > TRIGGER_DISTANCE) continue;

            long now = System.currentTimeMillis();
            Long last = lastAlarm.get(player.getUniqueId());
            if (last != null && now - last < cooldownMs) continue;
            lastAlarm.put(player.getUniqueId(), now);

            if (owner != null) {
                owner.sendMessage("§c[Laser Alarm] §f" + player.getName() + " crossed a tripwire!");
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
        }
    }

    private double distanceToSegment(Vector point) {
        Vector a = pointA.toVector().add(new Vector(0.5, 0.5, 0.5));
        Vector b = pointB.toVector().add(new Vector(0.5, 0.5, 0.5));
        Vector ab = b.clone().subtract(a);
        double lengthSquared = ab.lengthSquared();
        if (lengthSquared < 0.0001) return point.distance(a);

        double t = point.clone().subtract(a).dot(ab) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        Vector closest = a.clone().add(ab.multiply(t));
        return point.distance(closest);
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}
