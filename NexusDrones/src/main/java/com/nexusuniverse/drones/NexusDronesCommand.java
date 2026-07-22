package com.nexusuniverse.drones;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NexusDronesCommand implements CommandExecutor {

    private final NexusDronesPlugin plugin;

    public NexusDronesCommand(NexusDronesPlugin plugin) {
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
            case "removeall" -> {
                plugin.getDroneManager().removeAll();
                player.sendMessage("§cAll drones and turrets removed.");
            }
            case "count" -> player.sendMessage("§7Active deployables: §e" + plugin.getDroneManager().activeCount());
            default -> sendUsage(player);
        }
        return true;
    }

    private void handleGive(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /nexusdrones give <surveillance|kamikaze|turret>");
            return;
        }

        DeployableType type = switch (args[1].toLowerCase()) {
            case "surveillance" -> DeployableType.SURVEILLANCE_DRONE;
            case "kamikaze" -> DeployableType.KAMIKAZE_DRONE;
            case "turret" -> DeployableType.TURRET;
            default -> null;
        };

        if (type == null) {
            player.sendMessage("§cUnknown type. Options: surveillance, kamikaze, turret");
            return;
        }

        player.getInventory().addItem(plugin.getDeployItems().create(type));
        player.sendMessage("§aGave 1x " + type.name());
    }

    private void sendUsage(Player player) {
        player.sendMessage("§7/nexusdrones give <surveillance|kamikaze|turret>");
        player.sendMessage("§7/nexusdrones count");
        player.sendMessage("§7/nexusdrones removeall");
    }
}
