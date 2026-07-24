package com.nexusuniverse.drones.security;

import com.nexusuniverse.drones.NexusDronesPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class SecurityItems {

    private final NamespacedKey typeKey;

    public SecurityItems(NexusDronesPlugin plugin) {
        this.typeKey = new NamespacedKey(plugin, "security_tool_type");
    }

    public ItemStack create(SecurityToolType type) {
        Material material = switch (type) {
            case SECURITY_TABLET -> Material.CLOCK;
            case SECURITY_CAMERA -> Material.SPYGLASS;
            case LASER_POST -> Material.END_ROD;
            case GHOST_DOOR -> Material.REDSTONE_TORCH;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String displayName = switch (type) {
            case SECURITY_TABLET -> "§eSecurity Tablet";
            case SECURITY_CAMERA -> "§bSecurity Camera";
            case LASER_POST -> "§cLaser Post";
            case GHOST_DOOR -> "§5Ghost Door Marker";
        };
        meta.setDisplayName(displayName);

        String lore = switch (type) {
            case SECURITY_TABLET -> "§7Right-click to view your camera network.";
            case SECURITY_CAMERA -> "§7Right-click a block to mount a camera.";
            case LASER_POST -> "§7Right-click two blocks to connect a laser beam.";
            case GHOST_DOOR -> "§7Right-click an existing block to register it as a secret door.";
        };
        meta.setLore(List.of(lore));

        meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
        return item;
    }

    public SecurityToolType readType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        String raw = meta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return SecurityToolType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
