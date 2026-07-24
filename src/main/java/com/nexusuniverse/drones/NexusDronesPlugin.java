package com.nexusuniverse.drones;

import com.nexusuniverse.drones.security.SecurityCommand;
import com.nexusuniverse.drones.security.SecurityInteractListener;
import com.nexusuniverse.drones.security.SecurityItems;
import com.nexusuniverse.drones.security.SecurityManager;
import org.bukkit.plugin.java.JavaPlugin;

public class NexusDronesPlugin extends JavaPlugin {

    private DroneManager droneManager;
    private DeployItems deployItems;
    private SecurityManager securityManager;
    private SecurityItems securityItems;
    private AlertSettings alertSettings;

    @Override
    public void onEnable() {
        this.alertSettings = new AlertSettings();
        this.droneManager = new DroneManager(alertSettings);
        this.deployItems = new DeployItems(this);
        this.securityManager = new SecurityManager(alertSettings);
        this.securityItems = new SecurityItems(this);

        getCommand("nexusdrones").setExecutor(new NexusDronesCommand(this));
        getCommand("nexussecurity").setExecutor(new SecurityCommand(this));

        getServer().getPluginManager().registerEvents(new DeployListener(this), this);
        getServer().getPluginManager().registerEvents(new SecurityInteractListener(this), this);

        // central tick loop: every 2 ticks (10/sec) is smooth enough for
        // hover/patrol/chase movement and laser/camera scanning without
        // hammering the server
        getServer().getScheduler().runTaskTimer(this, () -> {
            droneManager.tickAll();
            securityManager.tickAll();
        }, 2L, 2L);

        getLogger().info("NexusDrones enabled -- drones, turrets, and security devices are live.");
    }

    @Override
    public void onDisable() {
        if (droneManager != null) {
            droneManager.removeAll();
        }
        if (securityManager != null) {
            securityManager.clearAll();
        }
    }

    public DroneManager getDroneManager() {
        return droneManager;
    }

    public DeployItems getDeployItems() {
        return deployItems;
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    public SecurityItems getSecurityItems() {
        return securityItems;
    }

    public AlertSettings getAlertSettings() {
        return alertSettings;
    }
}
