package com.nexusuniverse.drones;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class DeployListener implements Listener {

    private final NexusDronesPlugin plugin;

    public DeployListener(NexusDronesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        DeployableType type = plugin.getDeployItems().readType(item);
        if (type == null) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Location spawnLocation = player.getLocation();

        plugin.getDroneManager().deploy(type, player, spawnLocation);
        player.sendMessage("§aDeployed " + type.name() + ".");

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }
}
