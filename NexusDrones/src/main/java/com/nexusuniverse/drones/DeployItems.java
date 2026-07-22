package com.nexusuniverse.drones;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles creating "deploy" items (right-click to spawn a drone/turret)
 * and reading back what type an item is, using a PersistentDataContainer
 * tag rather than name/lore matching -- so renaming or reskinning the
 * item later won't break detection.
 */
public class DeployItems {

    private final NexusDronesPlugin plugin;
    private final NamespacedKey typeKey;

    public DeployItems(NexusDronesPlugin plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "deployable_type");
    }

    public ItemStack create(DeployableType type) {
        Material material = switch (type) {
            case SURVEILLANCE_DRONE -> Material.ENDER_EYE;
            case KAMIKAZE_DRONE -> Material.FIRE_CHARGE;
            case TURRET -> Material.CROSSBOW;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String displayName = switch (type) {
            case SURVEILLANCE_DRONE -> "§bSurveillance Drone";
            case KAMIKAZE_DRONE -> "§cKamikaze Drone";
            case TURRET -> "§6Sentry Turret";
        };
        meta.setDisplayName(displayName);

        String lore = switch (type) {
            case SURVEILLANCE_DRONE -> "§7Right-click to deploy. Patrols and spots enemies.";
            case KAMIKAZE_DRONE -> "§7Right-click to deploy. Homes in and detonates.";
            case TURRET -> "§7Right-click to deploy. Stationary, fires on sight.";
        };
        meta.setLore(java.util.List.of(lore));

        meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        return item;
    }

    public DeployableType readType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        String raw = meta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return DeployableType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
