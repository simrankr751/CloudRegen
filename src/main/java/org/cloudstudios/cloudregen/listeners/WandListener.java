package org.cloudstudios.cloudregen.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.cloudstudios.cloudregen.config.ConfigManager;
import org.cloudstudios.cloudregen.config.MessageManager;
import org.cloudstudios.cloudregen.region.SelectionService;
import org.cloudstudios.cloudregen.utils.Text;

public final class WandListener implements Listener {
    private final SelectionService selectionService;
    private final ConfigManager config;
    private final MessageManager messages;

    public WandListener(SelectionService selectionService, ConfigManager config, MessageManager messages) {
        this.selectionService = selectionService;
        this.config = config;
        this.messages = messages;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != config.wandMaterial()) {
            return;
        }
        Player player = event.getPlayer();
        Location clicked = event.getClickedBlock() == null ? player.getLocation() : event.getClickedBlock().getLocation();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            selectionService.setPos1(player.getUniqueId(), clicked);
            Text.sendActionbar(player, messages.format("selection-pos1-actionbar", "%x%", String.valueOf(clicked.getBlockX()), "%y%", String.valueOf(clicked.getBlockY()), "%z%", String.valueOf(clicked.getBlockZ())));
            player.sendMessage(messages.format("selection-pos1", "%x%", String.valueOf(clicked.getBlockX()), "%y%", String.valueOf(clicked.getBlockY()), "%z%", String.valueOf(clicked.getBlockZ())));
            event.setCancelled(true);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            selectionService.setPos2(player.getUniqueId(), clicked);
            Text.sendActionbar(player, messages.format("selection-pos2-actionbar", "%x%", String.valueOf(clicked.getBlockX()), "%y%", String.valueOf(clicked.getBlockY()), "%z%", String.valueOf(clicked.getBlockZ())));
            player.sendMessage(messages.format("selection-pos2", "%x%", String.valueOf(clicked.getBlockX()), "%y%", String.valueOf(clicked.getBlockY()), "%z%", String.valueOf(clicked.getBlockZ())));
            event.setCancelled(true);
        }
    }
}
