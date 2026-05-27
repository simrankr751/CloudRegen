package org.cloudstudios.cloudregen;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.cloudstudios.cloudregen.commands.CloudRegenCommand;
import org.cloudstudios.cloudregen.config.ConfigManager;
import org.cloudstudios.cloudregen.config.MessageManager;
import org.cloudstudios.cloudregen.config.PlaceholderService;
import org.cloudstudios.cloudregen.gui.ChatInputService;
import org.cloudstudios.cloudregen.gui.RegionBrowserGui;
import org.cloudstudios.cloudregen.gui.RegionDeleteConfirmGui;
import org.cloudstudios.cloudregen.gui.RegionEditorGui;
import org.cloudstudios.cloudregen.listeners.BlockTrackListener;
import org.cloudstudios.cloudregen.listeners.ChatInputListener;
import org.cloudstudios.cloudregen.listeners.RegionEditorListener;
import org.cloudstudios.cloudregen.listeners.WandListener;
import org.cloudstudios.cloudregen.placeholder.CloudRegenPlaceholderExpansion;
import org.cloudstudios.cloudregen.regeneration.RegenerationService;
import org.cloudstudios.cloudregen.region.RegionMetadata;
import org.cloudstudios.cloudregen.region.SelectionService;
import org.cloudstudios.cloudregen.scheduler.SchedulerAdapter;
import org.cloudstudios.cloudregen.scheduler.SchedulerFactory;
import org.cloudstudios.cloudregen.storage.BinaryRegionIO;
import org.cloudstudios.cloudregen.storage.MetadataIO;
import org.cloudstudios.cloudregen.storage.RegionRepository;
import org.cloudstudios.cloudregen.storage.SnapshotCaptureService;
import org.cloudstudios.cloudregen.tracking.RegionChangeTracker;

import java.io.IOException;

public final class CloudRegenPlugin extends JavaPlugin {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_AQUA = "\u001B[36m";
    private static final String ANSI_CYAN = "\u001B[96m";
    private static final String ANSI_GREEN = "\u001B[92m";
    private static final String ANSI_GRAY = "\u001B[90m";

    private ConfigManager configManager;
    private MessageManager messageManager;
    private SchedulerAdapter scheduler;
    private RegionRepository regionRepository;
    private RegenerationService regenerationService;
    private CloudRegenPlaceholderExpansion placeholderExpansion;
    private final Object2LongOpenHashMap<String> nextAutoRunEpoch = new Object2LongOpenHashMap<>();

    @Override
    public void onEnable() {
        printStartupBanner();
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this, configManager);
        this.scheduler = SchedulerFactory.create();

        SelectionService selectionService = new SelectionService();
        ChatInputService chatInputService = new ChatInputService();
        RegionChangeTracker tracker = new RegionChangeTracker();
        BinaryRegionIO binary = new BinaryRegionIO(configManager);
        MetadataIO metadataIO = new MetadataIO();
        this.regionRepository = new RegionRepository(getDataFolder(), binary, metadataIO);
        this.messageManager.setPlaceholderService(new PlaceholderService(regionRepository, this::secondsUntilRegen));

        SnapshotCaptureService captureService = new SnapshotCaptureService();
        this.regenerationService = new RegenerationService(this, scheduler, configManager, tracker, messageManager);

        RegionEditorGui editorGui = new RegionEditorGui(messageManager);
        RegionBrowserGui browserGui = new RegionBrowserGui();
        RegionDeleteConfirmGui confirmGui = new RegionDeleteConfirmGui();
        RegionEditorListener editorListener = new RegionEditorListener(editorGui, browserGui, confirmGui, chatInputService, regionRepository, messageManager);

        CloudRegenCommand command = new CloudRegenCommand(this, configManager, messageManager, selectionService, chatInputService, regionRepository, captureService, regenerationService, editorGui, editorListener, scheduler);
        if (getCommand("cr") != null) {
            getCommand("cr").setExecutor(command);
            getCommand("cr").setTabCompleter(command);
        }

        getServer().getPluginManager().registerEvents(new WandListener(selectionService, configManager, messageManager), this);
        getServer().getPluginManager().registerEvents(new BlockTrackListener(regionRepository, tracker), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(chatInputService, messageManager), this);
        getServer().getPluginManager().registerEvents(editorListener, this);

        seedAutoSchedule();
        scheduler.runTimerAsync(this, this::runAutoRegenCycle, 20L, 20L);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderExpansion = new CloudRegenPlaceholderExpansion(this);
            this.placeholderExpansion.register();
            getLogger().info("PlaceholderAPI hook enabled: %cr_time_<region>%");
        }
    }

    @Override
    public void onDisable() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
            placeholderExpansion = null;
        }
    }

    public long secondsUntilRegen(String regionKeyRaw) {
        String regionKey = regionKeyRaw == null ? "" : regionKeyRaw.trim().toLowerCase();
        RegionMetadata metadata = regionRepository.get(regionKey);
        if (metadata == null) {
            return -1L;
        }
        long now = System.currentTimeMillis() / 1000L;
        if (!nextAutoRunEpoch.containsKey(regionKey)) {
            nextAutoRunEpoch.put(regionKey, now + Math.max(1, metadata.getRegenIntervalSeconds()));
        }
        long next = nextAutoRunEpoch.getLong(regionKey);
        return Math.max(0L, next - now);
    }

    private void printStartupBanner() {
        getLogger().info(ANSI_GRAY + "============================================================" + ANSI_RESET);
        getLogger().info(ANSI_AQUA + "   ____ _                 _ ____                              " + ANSI_RESET);
        getLogger().info(ANSI_AQUA + "  / ___| | ___  _   _  __| |  _ \\ ___  __ _  ___ _ __       " + ANSI_RESET);
        getLogger().info(ANSI_CYAN + " | |   | |/ _ \\| | | |/ _` | |_) / _ \\/ _` |/ _ \\ '_ \\      " + ANSI_RESET);
        getLogger().info(ANSI_CYAN + " | |___| | (_) | |_| | (_| |  _ <  __/ (_| |  __/ | | |     " + ANSI_RESET);
        getLogger().info(ANSI_GREEN + "  \\____|_|\\___/ \\__,_|\\__,_|_| \\_\\___|\\__, |\\___|_| |_|     " + ANSI_RESET);
        getLogger().info(ANSI_GREEN + "                                          |___/               " + ANSI_RESET);
        getLogger().info(ANSI_GRAY + "  CloudRegen started with performance profile: EXTREME" + ANSI_RESET);
        getLogger().info(ANSI_GRAY + "============================================================" + ANSI_RESET);
    }

    private void seedAutoSchedule() {
        long now = System.currentTimeMillis() / 1000L;
        for (RegionMetadata metadata : regionRepository.all()) {
            nextAutoRunEpoch.put(metadata.getName().toLowerCase(), now + Math.max(1, metadata.getRegenIntervalSeconds()));
        }
    }

    private void runAutoRegenCycle() {
        long now = System.currentTimeMillis() / 1000L;
        for (RegionMetadata metadata : regionRepository.all()) {
            String key = metadata.getName().toLowerCase();
            if (!nextAutoRunEpoch.containsKey(key)) {
                nextAutoRunEpoch.put(key, now + Math.max(1, metadata.getRegenIntervalSeconds()));
            }
            long next = nextAutoRunEpoch.getLong(key);
            if (now < next) continue;
            nextAutoRunEpoch.put(key, now + Math.max(1, metadata.getRegenIntervalSeconds()));
            scheduler.runAsync(this, () -> {
                try {
                    regenerationService.regenerate(metadata, regionRepository.loadSnapshot(metadata.getName()));
                } catch (IOException ignored) {
                }
            });
        }
    }
}
