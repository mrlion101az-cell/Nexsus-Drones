package com.nexusuniverse.drones;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DroneManager {

    private final List<Drone> drones = new ArrayList<>();
    private final List<Turret> turrets = new ArrayList<>();
    private final AlertSettings alertSettings;

    public DroneManager(AlertSettings alertSettings) {
        this.alertSettings = alertSettings;
    }

    public void deploy(DeployableType type, Player owner, Location location) {
        if (type == DeployableType.TURRET) {
            turrets.add(new Turret(owner, location));
        } else {
            drones.add(new Drone(type, owner, location, alertSettings));
        }
    }

    public void tickAll() {
        for (Drone drone : drones) {
            drone.tick();
        }
        drones.removeIf(Drone::isDead);

        for (Turret turret : turrets) {
            turret.tick();
        }
        turrets.removeIf(Turret::isDead);
    }

    public void removeAll() {
        drones.forEach(Drone::remove);
        drones.clear();
        turrets.forEach(Turret::remove);
        turrets.clear();
    }

    public int activeCount() {
        return drones.size() + turrets.size();
    }
}
