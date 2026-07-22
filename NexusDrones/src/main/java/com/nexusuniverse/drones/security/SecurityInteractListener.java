package com.nexusuniverse.drones.security;

import com.nexusuniverse.drones.NexusDronesPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SecurityInteractListener implements Listener {

    private final NexusDronesPlugin plugin;

    public SecurityInteractListener(NexusDronesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        SecurityToolType type = plugin.getSecurityItems().readType(item);

        // handle bare-hand toggle of an existing ghost door first, regardless of held item
        if (type == null && event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
            Block clicked = event.getClickedBlock();
            if (clicked != null) {
                GhostDoor door = plugin.getSecurityManager().findGhostDoor(clicked.getLocation());
                if (door == null) {
                    // also check the block if it's currently open (registered at same loc but block is AIR)
                }
                if (door != null && door.getOwnerId().equals(player.getUniqueId())) {
                    door.toggle();
                    player.sendMessage(door.isOpen() ? "§5Door opened." : "§5Door closed.");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (type == null) return;

        switch (type) {
            case SECURITY_CAMERA -> handleCamera(event, player);
            case LASER_POST -> handleLaser(event, player);
            case GHOST_DOOR -> handleGhostDoor(event, player);
            case SECURITY_TABLET -> handleTablet(event, player);
        }
    }

    private void handleCamera(PlayerInteractEvent event, Player player) {
        Block clicked = event.getClickedBlock();
        Location location = clicked != null
                ? clicked.getLocation().add(0.5, 1.5, 0.5)
                : player.getLocation();

        String name = "cam" + (plugin.getSecurityManager().camerasFor(player.getUniqueId()).size() + 1);
        plugin.getSecurityManager().addCamera(new SecurityCamera(name, player.getUniqueId(), location));
        player.sendMessage("§bMounted camera §f\"" + name + "\"§b.");
        consumeOne(player, event);
        event.setCancelled(true);
    }

    private void handleLaser(PlayerInteractEvent event, Player player) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            player.sendMessage("§cAim at a block to place a laser post.");
            return;
        }

        UUID id = player.getUniqueId();
        Location pending = plugin.getSecurityManager().takePendingLaserPoint(id);

        if (pending == null) {
            plugin.getSecurityManager().setPendingLaserPoint(id, clicked.getLocation());
            player.sendMessage("§cFirst laser post set. Right-click another block to connect it.");
        } else {
            plugin.getSecurityManager().addLaser(new LaserTripwire(id, pending, clicked.getLocation()));
            player.sendMessage("§cLaser connected.");
            consumeOne(player, event);
        }
        event.setCancelled(true);
    }

    private void handleGhostDoor(PlayerInteractEvent event, Player player) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            player.sendMessage("§cAim at a placed block to register it as a secret door.");
            return;
        }

        if (plugin.getSecurityManager().findGhostDoor(clicked.getLocation()) != null) {
            player.sendMessage("§cThat block is already a registered door.");
            return;
        }

        plugin.getSecurityManager().addGhostDoor(
                new GhostDoor(player.getUniqueId(), clicked.getLocation(), clicked.getType()));
        player.sendMessage("§5Registered as a secret door. Sneak + right-click it to toggle.");
        consumeOne(player, event);
        event.setCancelled(true);
    }

    private void handleTablet(PlayerInteractEvent event, Player player) {
        var cameras = plugin.getSecurityManager().camerasFor(player.getUniqueId());
        if (cameras.isEmpty()) {
            player.sendMessage("§eYou have no cameras deployed yet.");
            return;
        }
        player.sendMessage("§eYour cameras:");
        for (SecurityCamera camera : cameras) {
            player.sendMessage("§7 - §f" + camera.getName() + " §7(/nexussecurity view " + camera.getName() + ")");
        }
        event.setCancelled(true);
    }

    private void consumeOne(Player player, PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }
}
