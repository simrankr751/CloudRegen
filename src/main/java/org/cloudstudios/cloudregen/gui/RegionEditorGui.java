package org.cloudstudios.cloudregen.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.cloudstudios.cloudregen.config.MessageManager;
import org.cloudstudios.cloudregen.region.RegionMetadata;
import org.cloudstudios.cloudregen.region.RegionMode;
import org.cloudstudios.cloudregen.utils.Text;

public final class RegionEditorGui {
    public static final int SLOT_DISPLAY = 19;
    public static final int SLOT_INTERVAL = 20;
    public static final int SLOT_MODE = 21;
    public static final int SLOT_MESSAGE = 22;
    public static final int SLOT_SAFETY = 23;
    public static final int SLOT_AUDIENCE = 24;
    public static final int SLOT_TELEPORT = 25;

    public static final int SLOT_BACK = 45;
    public static final int SLOT_CLOSE = 46;
    public static final int SLOT_DELETE = 50;
    public static final int SLOT_SAVE = 53;

    private final MessageManager messages;

    public RegionEditorGui(MessageManager messages) {
        this.messages = messages;
    }

    public static String title(String regionName) {
        return Text.color("&8✦ &bCloudRegen &8» &fEditor &8| &f" + regionName);
    }

    public void open(Player player, RegionMetadata metadata) {
        Inventory inv = Bukkit.createInventory(null, 54, title(metadata.getName()));
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, GuiItemBuilder.of(i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8 ? Material.GRAY_STAINED_GLASS_PANE : Material.BLACK_STAINED_GLASS_PANE)
                    .name("&8 ")
                    .lore("&8")
                    .build());
        }

        inv.setItem(4, GuiItemBuilder.of(Material.NETHER_STAR)
                .name("&8✦ &bCloudRegen &8» &fRegion Editor")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Configure this region with",
                        "&7a clean, premium workflow.",
                        "&8",
                        "&7Editing: &f" + metadata.getName(),
                        GuiTheme.DIVIDER
                )
                .build());

        inv.setItem(SLOT_DISPLAY, GuiItemBuilder.of(Material.NAME_TAG)
                .name("&8✦ &bDisplay Name")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Name shown in player-facing",
                        "&7messages and UI screens.",
                        "&8",
                        "&bCurrent:",
                        "&f" + metadata.getDisplayName(),
                        "&8",
                        "&7Click to edit",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_INTERVAL, GuiItemBuilder.of(Material.CLOCK)
                .name("&8✦ &bRegen Interval")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7How frequently this region",
                        "&7auto-regenerates.",
                        "&8",
                        "&bCurrent:",
                        "&f" + metadata.getRegenIntervalSeconds() + " seconds",
                        "&8",
                        "&7Click to edit",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_MODE, GuiItemBuilder.of(Material.TNT)
                .name("&8✦ &bRegeneration Mode")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Controls which changes",
                        "&7get restored.",
                        "&8",
                        "&bCurrent:",
                        "&f" + metadata.getMode().name(),
                        "&8",
                        "&7Click to cycle",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_MESSAGE, GuiItemBuilder.of(Material.OAK_SIGN)
                .name("&8✦ &bRegen Message")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Message sent on region reset.",
                        "&7Supports '&' color codes.",
                        "&8",
                        "&bCurrent:",
                        "&f" + (metadata.getMessage() == null ? "Disabled" : metadata.getMessage()),
                        "&8",
                        "&7Click to edit",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_SAFETY, GuiItemBuilder.of(Material.COMPASS)
                .name(metadata.isSafetyEnabled() ? "&8✦ &aSafety Teleport" : "&8✦ &cSafety Teleport")
                .lore(
                        GuiTheme.DIVIDER,
                        metadata.isSafetyEnabled() ? "&7Players are moved to safe air" : "&7Players are not auto-moved",
                        metadata.isSafetyEnabled() ? "&7positions during regeneration." : "&7during regeneration.",
                        "&8",
                        metadata.isSafetyEnabled() ? "&a✔ ENABLED" : "&c✘ DISABLED",
                        "&8",
                        metadata.isSafetyEnabled() ? "&7Click to disable" : "&7Click to enable",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_AUDIENCE, GuiItemBuilder.of(Material.REDSTONE_TORCH)
                .name("&8✦ &bMessage Audience")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Choose who receives",
                        "&7regen notifications.",
                        "&8",
                        "&bCurrent:",
                        metadata.isBroadcastMessage() ? "&6Server-wide" : "&aPlayers in region",
                        "&8",
                        "&7Click to toggle",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_TELEPORT, GuiItemBuilder.of(Material.ENDER_PEARL)
                .name("&8✦ &bTeleport to Region")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Teleport to region Pos1",
                        "&7for testing and checks.",
                        "&8",
                        "&7Click to teleport",
                        GuiTheme.DIVIDER
                )
                .build());

        inv.setItem(SLOT_BACK, GuiItemBuilder.of(Material.ARROW)
                .name("&8✦ &bBack")
                .lore(GuiTheme.DIVIDER, "&7Return to region browser.", GuiTheme.DIVIDER)
                .build());
        inv.setItem(SLOT_CLOSE, GuiItemBuilder.of(Material.BARRIER)
                .name("&8✦ &cClose")
                .lore(GuiTheme.DIVIDER, "&7Close this editor.", GuiTheme.DIVIDER)
                .build());
        inv.setItem(SLOT_DELETE, GuiItemBuilder.of(Material.BARRIER)
                .name("&8✦ &cDelete Region")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Open confirmation menu",
                        "&7to permanently delete this region.",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_SAVE, GuiItemBuilder.of(Material.EMERALD_BLOCK)
                .name("&8✦ &aSave Changes")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Persist all editor changes",
                        "&7to this region profile.",
                        GuiTheme.DIVIDER
                )
                .build());

        player.openInventory(inv);
    }

    public EditorAction handle(InventoryClickEvent event, RegionMetadata metadata, ChatInputService inputService) {
        if (event.getCurrentItem() == null) {
            return EditorAction.NONE;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == SLOT_DISPLAY) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.1f);
            player.closeInventory();
            inputService.request(player.getUniqueId(), System.currentTimeMillis() + 60000, text -> {
                if (!"cancel".equalsIgnoreCase(text)) {
                    metadata.setDisplayName(Text.color(text));
                    player.sendMessage(messages.format("prefix") + messages.get("editor-display-updated"));
                }
            });
            sendInputPrompt(player, "Display Name", "Enter a new display name in chat.");
            return EditorAction.NONE;
        }
        if (slot == SLOT_INTERVAL) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.1f);
            player.closeInventory();
            inputService.request(player.getUniqueId(), System.currentTimeMillis() + 60000, text -> {
                if ("cancel".equalsIgnoreCase(text)) {
                    return;
                }
                try {
                    metadata.setRegenIntervalSeconds(Math.max(5, Integer.parseInt(text)));
                    player.sendMessage(messages.format("prefix") + messages.format("editor-interval-updated", "%value%", String.valueOf(metadata.getRegenIntervalSeconds())));
                } catch (Exception ignored) {
                    player.sendMessage(messages.format("prefix") + messages.get("invalid-number"));
                }
            });
            sendInputPrompt(player, "Regen Interval", "Enter interval in seconds.");
            return EditorAction.NONE;
        }
        if (slot == SLOT_MODE) {
            RegionMode[] values = RegionMode.values();
            metadata.setMode(values[(metadata.getMode().ordinal() + 1) % values.length]);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.6f, 1.4f);
            player.sendMessage(messages.format("prefix") + messages.format("editor-mode-updated", "%value%", metadata.getMode().name()));
            open(player, metadata);
            return EditorAction.NONE;
        }
        if (slot == SLOT_MESSAGE) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.1f);
            player.closeInventory();
            inputService.request(player.getUniqueId(), System.currentTimeMillis() + 60000, text -> {
                if (!"cancel".equalsIgnoreCase(text)) {
                    metadata.setMessage("null".equalsIgnoreCase(text) ? null : Text.color(text));
                    player.sendMessage(messages.format("prefix") + messages.get("editor-message-updated"));
                }
            });
            sendInputPrompt(player, "Regen Message", "Enter message, or type 'null' to disable.");
            return EditorAction.NONE;
        }
        if (slot == SLOT_SAFETY) {
            metadata.setSafetyEnabled(!metadata.isSafetyEnabled());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, metadata.isSafetyEnabled() ? 1.5f : 0.8f);
            player.sendMessage(messages.format("prefix") + messages.format("editor-safety-updated", "%value%", String.valueOf(metadata.isSafetyEnabled())));
            open(player, metadata);
            return EditorAction.NONE;
        }
        if (slot == SLOT_AUDIENCE) {
            metadata.setBroadcastMessage(!metadata.isBroadcastMessage());
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.2f);
            player.sendMessage(messages.format("prefix") + messages.format("editor-audience-updated", "%value%", metadata.isBroadcastMessage() ? "server" : "region"));
            open(player, metadata);
            return EditorAction.NONE;
        }
        if (slot == SLOT_TELEPORT) {
            var world = Bukkit.getWorld(metadata.getWorld());
            if (world != null) {
                Location target = new Location(world, metadata.getMinX() + 0.5, metadata.getMinY() + 1, metadata.getMinZ() + 0.5);
                player.teleportAsync(target);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
                player.sendMessage(messages.format("prefix") + messages.get("editor-teleported"));
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
                player.sendMessage(messages.format("prefix") + messages.get("region-world-missing"));
            }
            return EditorAction.NONE;
        }
        if (slot == SLOT_SAVE) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
            return EditorAction.SAVE;
        }
        if (slot == SLOT_BACK) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.1f);
            return EditorAction.BACK;
        }
        if (slot == SLOT_DELETE) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
            return EditorAction.DELETE_CONFIRM;
        }
        if (slot == SLOT_CLOSE) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 0.9f);
            return EditorAction.CLOSE;
        }
        return EditorAction.NONE;
    }

    private void sendInputPrompt(Player player, String field, String body) {
        player.sendMessage(Component.empty());
        player.sendMessage(Text.component("&8━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(Text.component("&8✦ &bCloudRegen &8» &fEditor Input"));
        player.sendMessage(Text.component("&7Field: &b" + field));
        player.sendMessage(Text.component("&7" + body));
        player.sendMessage(Text.component("&8"));
        player.sendMessage(Text.component("&7Type &ccancel &7to abort."));
        player.sendMessage(Text.component("&8━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(Component.empty());
    }

    public enum EditorAction {
        NONE,
        SAVE,
        BACK,
        CLOSE,
        DELETE_CONFIRM
    }
}
