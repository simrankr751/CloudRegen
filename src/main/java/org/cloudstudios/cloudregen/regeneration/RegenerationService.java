package org.cloudstudios.cloudregen.regeneration;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.cloudstudios.cloudregen.config.ConfigManager;
import org.cloudstudios.cloudregen.config.MessageManager;
import org.cloudstudios.cloudregen.region.RegionMetadata;
import org.cloudstudios.cloudregen.region.RegionMode;
import org.cloudstudios.cloudregen.region.RegionSnapshot;
import org.cloudstudios.cloudregen.scheduler.CancellableTask;
import org.cloudstudios.cloudregen.scheduler.SchedulerAdapter;
import org.cloudstudios.cloudregen.tracking.RegionChangeTracker;
import org.cloudstudios.cloudregen.utils.ChunkKey;
import org.cloudstudios.cloudregen.utils.PackedPos;
import org.cloudstudios.cloudregen.utils.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class RegenerationService {
    private final JavaPlugin plugin;
    private final SchedulerAdapter scheduler;
    private final ConfigManager config;
    private final RegionChangeTracker tracker;
    private final MessageManager messages;
    private final Map<UUID, ActiveRegen> active = new ConcurrentHashMap<>();
    private final LongAdder appliedCounter = new LongAdder();
    private final LongAdder completedCounter = new LongAdder();
    private final long startedAt = System.nanoTime();

    public RegenerationService(JavaPlugin plugin, SchedulerAdapter scheduler, ConfigManager config, RegionChangeTracker tracker, MessageManager messages) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.config = config;
        this.tracker = tracker;
        this.messages = messages;
    }

    public int activeCount() { return active.size(); }
    public long totalAppliedBlocks() { return appliedCounter.sum(); }
    public long totalCompletedRegens() { return completedCounter.sum(); }

    public long avgBlocksPerSecond() {
        long elapsedNanos = System.nanoTime() - startedAt;
        if (elapsedNanos <= 0L) return 0L;
        return (long) (appliedCounter.sum() / (elapsedNanos / 1_000_000_000.0));
    }

    public long queuedBlocks() {
        long total = 0L;
        for (ActiveRegen regen : active.values()) total += regen.remaining;
        return total;
    }

    public void regenerate(RegionMetadata metadata, RegionSnapshot snapshot) {
        UUID id = metadata.getId();
        if (active.containsKey(id)) return;
        ActiveRegen regen = new ActiveRegen(metadata, snapshot);
        if (regen.done) return;
        active.put(id, regen);
        regen.task = scheduler.runTimerAsync(plugin, () -> runCycle(regen), 1L, Math.max(1, 20L / config.cyclesPerSecond()));
    }

    private void runCycle(ActiveRegen regen) {
        if (regen.done) {
            finish(regen);
            return;
        }
        int dynamicBudget = config.blocksPerCycle();
        if (config.adaptiveEnabled()) {
            dynamicBudget = Math.max(config.adaptiveMin(), Math.min(config.adaptiveMax(), regen.remaining > config.adaptiveMax() * 4L ? config.adaptiveMax() : config.blocksPerCycle()));
        }
        int chunksBudget = config.maxChunksPerCycle();

        while (dynamicBudget > 0 && regen.cursorChunk < regen.chunkOrder.size() && chunksBudget > 0) {
            long chunkKey = regen.chunkOrder.getLong(regen.cursorChunk);
            ChunkCursor cursor = regen.chunkCursors.get(chunkKey);
            if (cursor == null || cursor.done()) {
                regen.cursorChunk++;
                continue;
            }
            int allowed = Math.min(dynamicBudget, cursor.remaining());
            dynamicBudget -= allowed;
            chunksBudget--;

            int finalAllowed = allowed;
            scheduler.runAtLocation(plugin, cursor.chunkAnchor, () -> {
                applyBatch(regen, cursor, finalAllowed);
                if (regen.metadata.isSafetyEnabled()) {
                    movePlayersSafeInChunk(regen.metadata, ChunkKey.x(cursor.chunkKey), ChunkKey.z(cursor.chunkKey));
                }
            });

            if (cursor.done()) regen.cursorChunk++;
        }

        if (regen.remaining <= 0L || regen.cursorChunk >= regen.chunkOrder.size()) {
            regen.done = true;
            finish(regen);
        }
    }

    private void applyBatch(ActiveRegen regen, ChunkCursor cursor, int amount) {
        int end = Math.min(cursor.index + amount, cursor.packedEntries.size());
        for (int i = cursor.index; i < end; i++) {
            int packed = cursor.packedEntries.getInt(i);
            int localX = RegionSnapshot.unpackLocalX(packed);
            int localZ = RegionSnapshot.unpackLocalZ(packed);
            int y = RegionSnapshot.unpackY(packed);
            int palette = RegionSnapshot.unpackPalette(packed);
            if (palette < 0 || palette >= regen.paletteData.length) continue;
            int worldX = (ChunkKey.x(cursor.chunkKey) << 4) + localX;
            int worldZ = (ChunkKey.z(cursor.chunkKey) << 4) + localZ;
            Block block = cursor.world.getBlockAt(worldX, y, worldZ);
            block.setBlockData(regen.paletteData[palette], false);
            regen.applied++;
            regen.remaining--;
            appliedCounter.increment();
        }
        cursor.index = end;
    }

    private void finish(ActiveRegen regen) {
        if (!active.containsKey(regen.metadata.getId())) return;
        active.remove(regen.metadata.getId());
        if (regen.task != null) regen.task.cancel();
        regen.metadata.setTotalRegens(regen.metadata.getTotalRegens() + 1);
        regen.metadata.setTotalBlocksApplied(regen.metadata.getTotalBlocksApplied() + regen.applied);
        tracker.clear(regen.metadata.getId());
        completedCounter.increment();

        scheduler.runSync(plugin, () -> {
            if (regen.metadata.isSafetyEnabled()) movePlayersSafe(regen.metadata);
            sendRegenMessage(regen.metadata);
            playRegenSound(regen.metadata);
        });
    }

    private void playRegenSound(RegionMetadata metadata) {
        World world = Bukkit.getWorld(metadata.getWorld());
        if (world == null) return;
        for (Player player : world.getPlayers()) {
            if (metadata.isBroadcastMessage() || metadata.contains(player.getLocation())) {
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.65f, 1.25f);
            }
        }
    }

    private void sendRegenMessage(RegionMetadata metadata) {
        if (metadata.getMessage() == null || metadata.getMessage().isBlank()) return;
        String rendered = messages.format("regen-message-format", "%region%", metadata.getDisplayName(), "%message%", metadata.getMessage());
        if (metadata.isBroadcastMessage()) {
            Bukkit.broadcastMessage(rendered);
            return;
        }
        World world = Bukkit.getWorld(metadata.getWorld());
        if (world == null) return;
        for (Player player : world.getPlayers()) {
            if (metadata.contains(player.getLocation())) {
                player.sendMessage(rendered);
                Text.sendActionbar(player, messages.format("actionbar-regen", "%region%", metadata.getDisplayName()));
            }
        }
    }

    private void movePlayersSafeInChunk(RegionMetadata metadata, int chunkX, int chunkZ) {
        World world = Bukkit.getWorld(metadata.getWorld());
        if (world == null) return;
        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        for (Player player : world.getPlayers()) {
            Location loc = player.getLocation();
            if (loc.getBlockX() < minX || loc.getBlockX() > maxX || loc.getBlockZ() < minZ || loc.getBlockZ() > maxZ) continue;
            if (!metadata.contains(loc)) continue;
            if (isLocationSafe(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) continue;
            teleportUpToSafe(player, world, loc);
        }
    }

    private void movePlayersSafe(RegionMetadata metadata) {
        World world = Bukkit.getWorld(metadata.getWorld());
        if (world == null) return;
        for (Player player : world.getPlayers()) {
            if (!metadata.contains(player.getLocation())) continue;
            Location loc = player.getLocation();
            if (!isLocationSafe(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                teleportUpToSafe(player, world, loc);
            }
        }
    }

    private boolean isLocationSafe(World world, int x, int y, int z) {
        if (y < world.getMinHeight() || y > world.getMaxHeight() - 2) return false;
        Material feet = world.getBlockAt(x, y, z).getType();
        Material head = world.getBlockAt(x, y + 1, z).getType();
        return feet.isAir() && head.isAir();
    }

    private void teleportUpToSafe(Player player, World world, Location start) {
        int baseX = start.getBlockX();
        int baseZ = start.getBlockZ();
        int startY = Math.max(start.getBlockY(), world.getMinHeight());
        int top = world.getMaxHeight() - 2;
        int safeY = -1;
        for (int y = startY; y <= top; y++) {
            if (isLocationSafe(world, baseX, y, baseZ)) {
                safeY = y;
                break;
            }
        }
        if (safeY == -1) safeY = Math.min(top, world.getHighestBlockYAt(baseX, baseZ) + 1);
        Location target = new Location(world, baseX + 0.5, safeY, baseZ + 0.5, start.getYaw(), start.getPitch());
        scheduler.runAtLocation(plugin, target, () -> {
            player.teleportAsync(target);
            player.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 0.65f, 1.2f);
            Text.sendActionbar(player, messages.get("actionbar-safety-teleport"));
        });
    }

    private final class ActiveRegen {
        private final RegionMetadata metadata;
        private final Long2ObjectOpenHashMap<ChunkCursor> chunkCursors;
        private final LongList chunkOrder;
        private final BlockData[] paletteData;
        private long remaining;
        private long applied;
        private int cursorChunk;
        private boolean done;
        private CancellableTask task;

        private ActiveRegen(RegionMetadata metadata, RegionSnapshot snapshot) {
            this.metadata = metadata;
            this.chunkCursors = new Long2ObjectOpenHashMap<>(snapshot.chunkPacked().size());
            this.chunkOrder = new LongArrayList(snapshot.chunkPacked().size());
            this.paletteData = new BlockData[snapshot.palette().size()];
            for (int i = 0; i < snapshot.palette().size(); i++) paletteData[i] = Bukkit.createBlockData(snapshot.palette().get(i));
            prepare(snapshot);
        }

        private void prepare(RegionSnapshot snapshot) {
            World world = Bukkit.getWorld(metadata.getWorld());
            if (world == null) {
                done = true;
                return;
            }
            Long2ObjectMap<LongOpenHashSet> placedChanges = tracker.placed(metadata.getId());
            Long2ObjectMap<LongOpenHashSet> brokenChanges = tracker.broken(metadata.getId());
            for (Long2ObjectMap.Entry<IntList> entry : snapshot.chunkPacked().long2ObjectEntrySet()) {
                long chunkKey = entry.getLongKey();
                IntList source = entry.getValue();
                IntList filtered = source;
                if (metadata.getMode() != RegionMode.FULL) {
                    filtered = new it.unimi.dsi.fastutil.ints.IntArrayList(source.size());
                    LongOpenHashSet changes = metadata.getMode() == RegionMode.PLACED ? placedChanges.get(chunkKey) : brokenChanges.get(chunkKey);
                    if (changes == null || changes.isEmpty()) continue;
                    for (int i = 0; i < source.size(); i++) {
                        int packed = source.getInt(i);
                        int wx = (ChunkKey.x(chunkKey) << 4) + RegionSnapshot.unpackLocalX(packed);
                        int y = RegionSnapshot.unpackY(packed);
                        int wz = (ChunkKey.z(chunkKey) << 4) + RegionSnapshot.unpackLocalZ(packed);
                        long worldPacked = PackedPos.pack(wx, y, wz);
                        if (changes.contains(worldPacked)) filtered.add(packed);
                    }
                    if (filtered.isEmpty()) continue;
                }
                ChunkCursor cursor = new ChunkCursor(world, chunkKey, filtered);
                chunkCursors.put(chunkKey, cursor);
                chunkOrder.add(chunkKey);
                remaining += filtered.size();
            }
            done = remaining == 0L;
        }
    }

    private static final class ChunkCursor {
        private final World world;
        private final long chunkKey;
        private final IntList packedEntries;
        private final Location chunkAnchor;
        private int index;

        private ChunkCursor(World world, long chunkKey, IntList packedEntries) {
            this.world = world;
            this.chunkKey = chunkKey;
            this.packedEntries = packedEntries;
            this.chunkAnchor = new Location(world, ChunkKey.x(chunkKey) << 4, 64, ChunkKey.z(chunkKey) << 4);
            this.index = 0;
        }

        private int remaining() { return packedEntries.size() - index; }
        private boolean done() { return index >= packedEntries.size(); }
    }
}
