package org.cloudstudios.cloudregen.commands;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.cloudstudios.cloudregen.config.ConfigManager;
import org.cloudstudios.cloudregen.config.MessageManager;
import org.cloudstudios.cloudregen.gui.ChatInputService;
import org.cloudstudios.cloudregen.gui.RegionEditorGui;
import org.cloudstudios.cloudregen.listeners.RegionEditorListener;
import org.cloudstudios.cloudregen.region.RegionMetadata;
import org.cloudstudios.cloudregen.region.RegionSelection;
import org.cloudstudios.cloudregen.region.SelectionService;
import org.cloudstudios.cloudregen.regeneration.RegenerationService;
import org.cloudstudios.cloudregen.scheduler.SchedulerAdapter;
import org.cloudstudios.cloudregen.storage.RegionRepository;
import org.cloudstudios.cloudregen.storage.SnapshotCaptureService;
import org.cloudstudios.cloudregen.utils.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class CloudRegenCommand implements CommandExecutor, TabCompleter {
    private final ConfigManager config;
    private final MessageManager messages;
    private final SelectionService selectionService;
    private final ChatInputService chatInput;
    private final RegionRepository repository;
    private final SnapshotCaptureService captureService;
    private final RegenerationService regenerationService;
    private final RegionEditorGui editorGui;
    private final RegionEditorListener editorListener;
    private final SchedulerAdapter scheduler;
    private final JavaPlugin plugin;

    public CloudRegenCommand(JavaPlugin plugin, ConfigManager config, MessageManager messages, SelectionService selectionService, ChatInputService chatInput, RegionRepository repository, SnapshotCaptureService captureService, RegenerationService regenerationService, RegionEditorGui editorGui, RegionEditorListener editorListener, SchedulerAdapter scheduler) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.selectionService = selectionService;
        this.chatInput = chatInput;
        this.repository = repository;
        this.captureService = captureService;
        this.regenerationService = regenerationService;
        this.editorGui = editorGui;
        this.editorListener = editorListener;
        this.scheduler = scheduler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            handleHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "wand" -> giveWand(player);
            case "save" -> handleSave(player, args);
            case "redefine" -> handleRedefine(player, args);
            case "editor" -> handleEditor(player);
            case "list" -> handleList(player);
            case "delete" -> handleDelete(player, args);
            case "force" -> handleForce(player, args);
            case "edit" -> handleEdit(player, args);
            case "reload" -> handleReload(player);
            case "debug" -> handleDebug(player);
            default -> {
                messages.sendPrefixed(player, "unknown-subcommand");
                handleHelp(player);
            }
        }
        return true;
    }

    private void handleHelp(Player player) {
        for (String line : messages.getList("help")) {
            player.sendMessage(line);
        }
        Text.sendActionbar(player, messages.get("actionbar-help"));
    }

    private void giveWand(Player player) {
        Material mat = config.wandMaterial() == null ? Material.BLAZE_ROD : config.wandMaterial();
        ItemStack wand = new ItemStack(mat);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(Text.color(config.wandName()));
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);
        messages.sendPrefixed(player, "wand-given");
        Text.sendActionbar(player, messages.get("actionbar-wand-given"));
    }

    private void handleSave(Player player, String[] args) {
        RegionSelection selection = selectionService.getSelection(player.getUniqueId());
        if (selection == null) {
            messages.sendPrefixed(player, "need-selection");
            return;
        }
        if (args.length >= 2) {
            saveRegion(player, args[1], selection, false);
            return;
        }
        messages.sendPrefixed(player, "save-name-prompt");
        chatInput.request(player.getUniqueId(), System.currentTimeMillis() + 60000, text -> {
            if ("cancel".equalsIgnoreCase(text)) {
                return;
            }
            saveRegion(player, text, selection, false);
        });
    }

    private void handleRedefine(Player player, String[] args) {
        RegionSelection selection = selectionService.getSelection(player.getUniqueId());
        if (selection == null) {
            messages.sendPrefixed(player, "need-selection");
            return;
        }
        if (args.length < 2) {
            messages.sendPrefixed(player, "redefine-usage");
            return;
        }
        String name = args[1];
        if (repository.get(name) == null) {
            messages.sendPrefixed(player, "region-not-found");
            return;
        }
        saveRegion(player, name, selection, true);
    }

    private void handleEditor(Player player) {
        if (repository.all().isEmpty()) {
            messages.sendPrefixed(player, "editor-empty");
            return;
        }
        editorListener.openBrowser(player);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
        messages.sendPrefixed(player, "editor-opened");
    }

    private void handleList(Player player) {
        List<RegionMetadata> all = new ArrayList<>(repository.all());
        player.sendMessage(messages.format("list-header", "%count%", String.valueOf(all.size())));
        for (RegionMetadata metadata : all) {
            player.sendMessage(messages.format("list-entry", "%name%", metadata.getName(), "%mode%", metadata.getMode().name(), "%interval%", String.valueOf(metadata.getRegenIntervalSeconds())));
        }
        if (all.isEmpty()) {
            messages.sendPrefixed(player, "list-empty");
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            messages.sendPrefixed(player, "delete-usage");
            return;
        }
        if (repository.get(args[1]) == null) {
            messages.sendPrefixed(player, "region-not-found");
            return;
        }
        repository.delete(args[1]);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 0.9f);
        messages.sendPrefixed(player, "region-deleted", "%name%", args[1]);
    }

    private void handleForce(Player player, String[] args) {
        if (args.length < 2) {
            messages.sendPrefixed(player, "force-usage");
            return;
        }
        RegionMetadata metadata = repository.get(args[1]);
        if (metadata == null) {
            messages.sendPrefixed(player, "region-not-found");
            return;
        }
        scheduler.runAsync(plugin, () -> {
            try {
                regenerationService.regenerate(metadata, repository.loadSnapshot(metadata.getName()));
            } catch (IOException ignored) {
                messages.sendPrefixed(player, "region-load-failed", "%name%", metadata.getName());
            }
        });
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.1f);
        messages.sendPrefixed(player, "region-forced", "%name%", metadata.getName());
        Text.sendTitle(player, messages.format("title-force", "%name%", metadata.getDisplayName()), messages.get("subtitle-force"));
    }

    private void handleEdit(Player player, String[] args) {
        if (args.length < 2) {
            messages.sendPrefixed(player, "edit-usage");
            return;
        }
        RegionMetadata metadata = repository.get(args[1]);
        if (metadata == null) {
            messages.sendPrefixed(player, "region-not-found");
            return;
        }
        editorListener.openEditor(player, metadata);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
        messages.sendPrefixed(player, "edit-opened", "%name%", metadata.getName());
    }

    private void handleReload(Player player) {
        config.reload();
        messages.reload();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.6f);
        if (messages.usedLanguageFallback()) {
            messages.sendPrefixed(player, "language-fallback");
        }
        messages.sendPrefixed(player, "reloaded");
    }

    private void handleDebug(Player player) {
        player.sendMessage(messages.get("debug-header"));
        player.sendMessage(messages.format("debug-active", "%value%", String.valueOf(regenerationService.activeCount())));
        player.sendMessage(messages.format("debug-queued", "%value%", String.valueOf(regenerationService.queuedBlocks())));
        player.sendMessage(messages.format("debug-applied", "%value%", String.valueOf(regenerationService.totalAppliedBlocks())));
        player.sendMessage(messages.format("debug-regens", "%value%", String.valueOf(regenerationService.totalCompletedRegens())));
        player.sendMessage(messages.format("debug-rate", "%value%", String.valueOf(regenerationService.avgBlocksPerSecond())));
        player.sendMessage(messages.format("debug-budget", "%value%", String.valueOf(config.blocksPerCycle())));
        messages.sendPrefixed(player, "debug-complete");
    }

    private void saveRegion(Player player, String name, RegionSelection selection, boolean redefine) {
        RegionMetadata metadata = repository.get(name);
        if (metadata == null) {
            metadata = new RegionMetadata(UUID.randomUUID(), name);
        }
        metadata.setWorld(selection.world().getName());
        metadata.setMinX(selection.minX());
        metadata.setMinY(selection.minY());
        metadata.setMinZ(selection.minZ());
        metadata.setMaxX(selection.maxX());
        metadata.setMaxY(selection.maxY());
        metadata.setMaxZ(selection.maxZ());

        final RegionMetadata targetMetadata = metadata;
        final var snapshot = captureService.capture(selection);
        scheduler.runAsync(plugin, () -> {
            try {
                repository.save(name, targetMetadata, snapshot);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
                if (redefine) {
                    messages.sendPrefixed(player, "region-redefined", "%name%", name);
                    Text.sendTitle(player, messages.format("title-redefined", "%name%", name), messages.get("subtitle-redefined"));
                } else {
                    messages.sendPrefixed(player, "region-saved", "%name%", name);
                    Text.sendTitle(player, messages.format("title-saved", "%name%", name), messages.get("subtitle-saved"));
                }
                Text.sendActionbar(player, messages.format("actionbar-saved", "%name%", name));
            } catch (IOException ex) {
                messages.sendPrefixed(player, "region-save-failed", "%name%", name);
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("help", "wand", "save", "redefine", "editor", "edit", "force", "reload", "list", "delete", "debug");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("force") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("redefine"))) {
            return repository.all().stream().map(RegionMetadata::getName).toList();
        }
        return List.of();
    }
}
