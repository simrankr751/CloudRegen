package org.cloudstudios.cloudregen.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.cloudstudios.cloudregen.config.MessageManager;
import org.cloudstudios.cloudregen.gui.ChatInputService;
import org.cloudstudios.cloudregen.gui.GuiTheme;
import org.cloudstudios.cloudregen.gui.RegionBrowserGui;
import org.cloudstudios.cloudregen.gui.RegionDeleteConfirmGui;
import org.cloudstudios.cloudregen.gui.RegionEditorGui;
import org.cloudstudios.cloudregen.region.RegionMetadata;
import org.cloudstudios.cloudregen.storage.RegionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionEditorListener implements Listener {
    private final RegionEditorGui editorGui;
    private final RegionBrowserGui browserGui;
    private final RegionDeleteConfirmGui confirmGui;
    private final ChatInputService inputService;
    private final RegionRepository repository;
    private final MessageManager messages;

    private final Map<UUID, String> editingRegionByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> browserPageByPlayer = new ConcurrentHashMap<>();
    private final Map<UUID, String> confirmRegionByPlayer = new ConcurrentHashMap<>();

    public RegionEditorListener(RegionEditorGui editorGui, RegionBrowserGui browserGui, RegionDeleteConfirmGui confirmGui, ChatInputService inputService, RegionRepository repository, MessageManager messages) {
        this.editorGui = editorGui;
        this.browserGui = browserGui;
        this.confirmGui = confirmGui;
        this.inputService = inputService;
        this.repository = repository;
        this.messages = messages;
    }

    public void openEditor(Player player, RegionMetadata metadata) {
        editingRegionByPlayer.put(player.getUniqueId(), metadata.getName().toLowerCase());
        editorGui.open(player, metadata);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = event.getView().getTitle();
        if (title == null) {
            return;
        }

        if (title.contains("Region Browser")) {
            handleBrowserClick(player, event);
            return;
        }
        if (title.contains("CloudRegen") && title.contains("Editor")) {
            handleEditorClick(player, event);
            return;
        }
        if (title.equals(RegionDeleteConfirmGui.title())) {
            handleConfirmClick(player, event);
        }
    }

    private void handleBrowserClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.1f);

        int slot = event.getSlot();
        int page = browserPageByPlayer.getOrDefault(player.getUniqueId(), 0);
        List<RegionMetadata> regions = sortedRegions();
        int perPage = GuiTheme.contentSlots().size();
        int maxPage = Math.max(0, (regions.size() - 1) / perPage);

        if (slot == RegionBrowserGui.SLOT_CLOSE) {
            player.closeInventory();
            player.sendMessage(messages.format("prefix") + messages.get("browser-closed"));
            return;
        }
        if (slot == RegionBrowserGui.SLOT_PREV) {
            openBrowser(player, Math.max(0, page - 1));
            return;
        }
        if (slot == RegionBrowserGui.SLOT_NEXT) {
            openBrowser(player, Math.min(maxPage, page + 1));
            return;
        }
        if (slot == RegionBrowserGui.SLOT_REFRESH) {
            openBrowser(player, page);
            return;
        }
        if (slot == RegionBrowserGui.SLOT_CREATE) {
            player.closeInventory();
            player.performCommand("cr save");
            return;
        }

        int contentIndex = GuiTheme.contentSlots().indexOf(slot);
        if (contentIndex < 0) {
            return;
        }
        int regionIndex = page * perPage + contentIndex;
        if (regionIndex < 0 || regionIndex >= regions.size()) {
            return;
        }
        RegionMetadata target = regions.get(regionIndex);
        openEditor(player, target);
        player.sendMessage(messages.format("prefix") + messages.format("edit-opened", "%name%", target.getName()));
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
    }

    private void handleEditorClick(Player player, InventoryClickEvent event) {
        String regionName = editingRegionByPlayer.get(player.getUniqueId());
        if (regionName == null) {
            event.setCancelled(true);
            return;
        }
        RegionMetadata metadata = repository.get(regionName);
        if (metadata == null) {
            event.setCancelled(true);
            player.sendMessage(messages.format("prefix") + messages.get("region-not-found"));
            return;
        }

        RegionEditorGui.EditorAction action = editorGui.handle(event, metadata, inputService);
        switch (action) {
            case NONE -> {
            }
            case SAVE -> {
                try {
                    repository.save(metadata.getName(), metadata, repository.loadSnapshot(metadata.getName()));
                    player.sendMessage(messages.format("prefix") + messages.get("editor-saved"));
                    editorGui.open(player, metadata);
                } catch (IOException ignored) {
                    player.sendMessage(messages.format("prefix") + messages.format("region-save-failed", "%name%", metadata.getName()));
                }
            }
            case BACK -> openBrowser(player, browserPageByPlayer.getOrDefault(player.getUniqueId(), 0));
            case CLOSE -> {
                player.closeInventory();
                player.sendMessage(messages.format("prefix") + messages.get("editor-closed"));
            }
            case DELETE_CONFIRM -> {
                confirmRegionByPlayer.put(player.getUniqueId(), metadata.getName());
                confirmGui.open(player, metadata.getName());
            }
        }
    }

    private void handleConfirmClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        String region = confirmRegionByPlayer.get(player.getUniqueId());
        if (region == null) {
            player.closeInventory();
            return;
        }
        int slot = event.getSlot();
        if (slot == RegionDeleteConfirmGui.SLOT_CONFIRM) {
            repository.delete(region);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 0.9f);
            player.sendMessage(messages.format("prefix") + messages.format("region-deleted", "%name%", region));
            openBrowser(player, browserPageByPlayer.getOrDefault(player.getUniqueId(), 0));
            confirmRegionByPlayer.remove(player.getUniqueId());
            editingRegionByPlayer.remove(player.getUniqueId());
            return;
        }
        if (slot == RegionDeleteConfirmGui.SLOT_CANCEL || slot == RegionDeleteConfirmGui.SLOT_BACK) {
            RegionMetadata metadata = repository.get(region);
            if (metadata != null) {
                openEditor(player, metadata);
            } else {
                openBrowser(player, browserPageByPlayer.getOrDefault(player.getUniqueId(), 0));
            }
            confirmRegionByPlayer.remove(player.getUniqueId());
        }
    }

    public void openBrowser(Player player) {
        openBrowser(player, browserPageByPlayer.getOrDefault(player.getUniqueId(), 0));
    }

    private void openBrowser(Player player, int requestedPage) {
        List<RegionMetadata> regions = sortedRegions();
        int perPage = GuiTheme.contentSlots().size();
        int maxPage = Math.max(0, (regions.size() - 1) / perPage);
        int page = Math.max(0, Math.min(maxPage, requestedPage));
        browserPageByPlayer.put(player.getUniqueId(), page);
        browserGui.open(player, regions, page, maxPage + 1);
    }

    private List<RegionMetadata> sortedRegions() {
        List<RegionMetadata> list = new ArrayList<>(repository.all());
        list.sort(Comparator.comparing(RegionMetadata::getName));
        return list;
    }
}
