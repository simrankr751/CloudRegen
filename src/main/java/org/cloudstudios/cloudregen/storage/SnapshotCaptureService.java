package org.cloudstudios.cloudregen.storage;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.cloudstudios.cloudregen.region.RegionSelection;
import org.cloudstudios.cloudregen.region.RegionSnapshot;
import org.cloudstudios.cloudregen.utils.ChunkKey;

public final class SnapshotCaptureService {
    public RegionSnapshot capture(RegionSelection selection) {
        World world = selection.world();
        RegionSnapshot snapshot = new RegionSnapshot();
        Object2IntOpenHashMap<String> paletteIds = new Object2IntOpenHashMap<>(512);
        paletteIds.defaultReturnValue(-1);

        int minChunkX = selection.minX() >> 4;
        int maxChunkX = selection.maxX() >> 4;
        int minChunkZ = selection.minZ() >> 4;
        int maxChunkZ = selection.maxZ() >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                Chunk chunk = world.getChunkAt(cx, cz);
                if (!chunk.isLoaded()) {
                    world.loadChunk(cx, cz, true);
                }
                long chunkKey = ChunkKey.of(cx, cz);
                int startX = Math.max(selection.minX(), cx << 4);
                int endX = Math.min(selection.maxX(), (cx << 4) + 15);
                int startZ = Math.max(selection.minZ(), cz << 4);
                int endZ = Math.min(selection.maxZ(), (cz << 4) + 15);
                for (int y = selection.minY(); y <= selection.maxY(); y++) {
                    for (int x = startX; x <= endX; x++) {
                        int localX = x & 15;
                        for (int z = startZ; z <= endZ; z++) {
                            int localZ = z & 15;
                            BlockData data = world.getBlockAt(x, y, z).getBlockData();
                            String encoded = data.getAsString(false);
                            int paletteId = paletteIds.getInt(encoded);
                            if (paletteId == -1) {
                                paletteId = snapshot.palette().size();
                                if (paletteId > 8191) {
                                    throw new IllegalStateException("Palette overflow for region snapshot");
                                }
                                snapshot.palette().add(encoded);
                                paletteIds.put(encoded, paletteId);
                            }
                            snapshot.addChunkEntry(chunkKey, RegionSnapshot.packLocalState(localX, y, localZ, paletteId));
                        }
                    }
                }
            }
        }
        return snapshot;
    }
}
