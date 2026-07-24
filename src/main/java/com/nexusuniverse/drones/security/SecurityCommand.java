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
            case "ignore" -> handleIgnore(player, args);
            case "cooldown" -> handleCooldown(player, args);
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

    private void handleIgnore(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /nexussecurity ignore <player>");
            return;
        }

        UUID targetId;
        String targetName;
        Player onlineTarget = org.bukkit.Bukkit.getPlayerExact(args[1]);
        if (onlineTarget != null) {
            targetId = onlineTarget.getUniqueId();
            targetName = onlineTarget.getName();
        } else {
            // offline lookup by name so you can ignore someone who isn't on right now --
            // note this is name-based (deprecated API), so it won't track a later name change
            @SuppressWarnings("deprecation")
            var offline = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
            targetId = offline.getUniqueId();
            targetName = args[1];
        }

        if (targetId.equals(player.getUniqueId())) {
            player.sendMessage("§cYour own cameras never alert on you -- no need to ignore yourself.");
            return;
        }

        boolean nowIgnored = plugin.getSecurityManager().toggleIgnore(player.getUniqueId(), targetId);
        player.sendMessage(nowIgnored
                ? "§aYour cameras will now ignore §f" + targetName + "§a."
                : "§aYour cameras will alert on §f" + targetName + " §aagain.");
    }

    private void handleCooldown(Player player, String[] args) {
        if (args.length < 2) {
            int current = plugin.getAlertSettings().getCooldownSeconds(player.getUniqueId());
            player.sendMessage("§7Your current alert cooldown: §e" + current + "s");
            player.sendMessage("§7Usage: /nexussecurity cooldown <seconds>");
            return;
        }
        try {
            int seconds = Integer.parseInt(args[1]);
            plugin.getAlertSettings().setCooldownSeconds(player.getUniqueId(), seconds);
            int applied = plugin.getAlertSettings().getCooldownSeconds(player.getUniqueId());
            player.sendMessage("§aAlert cooldown set to " + applied
                    + "s. Applies to your cameras, lasers, and surveillance drones.");
        } catch (NumberFormatException e) {
            player.sendMessage("§cSeconds must be a whole number.");
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage("§7/nexussecurity give <tablet|camera|laser|ghostdoor>");
        player.sendMessage("§7/nexussecurity view <camera name>");
        player.sendMessage("§7/nexussecurity return");
        player.sendMessage("§7/nexussecurity ignore <player>");
        player.sendMessage("§7/nexussecurity cooldown <seconds>");
        player.sendMessage("§7/nexussecurity count");
        player.sendMessage("§7/nexussecurity clearall");
    }
}
