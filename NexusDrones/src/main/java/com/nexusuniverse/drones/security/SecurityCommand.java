package com.nexusuniverse.drones.security;

import com.nexusuniverse.drones.NexusDronesPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SecurityCommand implements CommandExecutor {

    private final NexusDronesPlugin plugin;
    private final Map<UUID, Location> returnLocation = new HashMap<>();

    public SecurityCommand(NexusDronesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(player, args);
            case "view" -> handleView(player, args);
            case "return" -> handleReturn(player);
            case "count" -> player.sendMessage("§7Security devices active: §e" + plugin.getSecurityManager().deviceCount());
            case "clearall" -> {
                plugin.getSecurityManager().clearAll();
                player.sendMessage("§cAll security devices cleared.");
            }
            default -> sendUsage(player);
        }
        return true;
    }

    private void handleGive(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /nexussecurity give <tablet|camera|laser|ghostdoor>");
            return;
        }

        SecurityToolType type = switch (args[1].toLowerCase()) {
            case "tablet" -> SecurityToolType.SECURITY_TABLET;
            case "camera" -> SecurityToolType.SECURITY_CAMERA;
            case "laser" -> SecurityToolType.LASER_POST;
            case "ghostdoor" -> SecurityToolType.GHOST_DOOR;
            default -> null;
        };

        if (type == null) {
            player.sendMessage("§cUnknown type. Options: tablet, camera, laser, ghostdoor");
            return;
        }

        player.getInventory().addItem(plugin.getSecurityItems().create(type));
        player.sendMessage("§aGave 1x " + type.name());
    }

    private void handleView(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /nexussecurity view <camera name>");
            return;
        }

        SecurityCamera camera = plugin.getSecurityManager().findCamera(player.getUniqueId(), args[1]);
        if (camera == null) {
            player.sendMessage("§cNo camera named \"" + args[1] + "\" found.");
            return;
        }

        returnLocation.put(player.getUniqueId(), player.getLocation());
        player.teleport(camera.getLocation());
        player.sendMessage("§eViewing camera \"" + camera.getName() + "\". Use /nexussecurity return to go back.");
    }

    private void handleReturn(Player player) {
        Location back = returnLocation.remove(player.getUniqueId());
        if (back == null) {
            player.sendMessage("§cNothing to return from.");
            return;
        }
        player.teleport(back);
        player.sendMessage("§eReturned.");
    }

    private void sendUsage(Player player) {
        player.sendMessage("§7/nexussecurity give <tablet|camera|laser|ghostdoor>");
        player.sendMessage("§7/nexussecurity view <camera name>");
        player.sendMessage("§7/nexussecurity return");
        player.sendMessage("§7/nexussecurity count");
        player.sendMessage("§7/nexussecurity clearall");
    }
}
