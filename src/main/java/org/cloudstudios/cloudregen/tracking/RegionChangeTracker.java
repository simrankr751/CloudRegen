package org.cloudstudios.cloudregen.tracking;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.cloudstudios.cloudregen.region.RegionMetadata;
import org.cloudstudios.cloudregen.utils.ChunkKey;
import org.cloudstudios.cloudregen.utils.PackedPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionChangeTracker {
    private final Map<UUID, Long2ObjectOpenHashMap<LongOpenHashSet>> placed = new ConcurrentHashMap<>();
    private final Map<UUID, Long2ObjectOpenHashMap<LongOpenHashSet>> broken = new ConcurrentHashMap<>();

    public void trackPlaced(RegionMetadata region, int x, int y, int z) {
        track(placed, region.getId(), x, y, z);
    }

    public void trackBroken(RegionMetadata region, int x, int y, int z) {
        track(broken, region.getId(), x, y, z);
    }

    private void track(Map<UUID, Long2ObjectOpenHashMap<LongOpenHashSet>> map, UUID regionId, int x, int y, int z) {
        Long2ObjectOpenHashMap<LongOpenHashSet> chunks = map.computeIfAbsent(regionId, ignored -> new Long2ObjectOpenHashMap<>());
        long chunk = ChunkKey.of(x >> 4, z >> 4);
        LongOpenHashSet set = chunks.computeIfAbsent(chunk, ignored -> new LongOpenHashSet());
        set.add(PackedPos.pack(x, y, z));
    }

    public Long2ObjectOpenHashMap<LongOpenHashSet> placed(UUID regionId) {
        return placed.getOrDefault(regionId, new Long2ObjectOpenHashMap<>());
    }

    public Long2ObjectOpenHashMap<LongOpenHashSet> broken(UUID regionId) {
        return broken.getOrDefault(regionId, new Long2ObjectOpenHashMap<>());
    }

    public void clear(UUID regionId) {
        placed.remove(regionId);
        broken.remove(regionId);
    }
}
