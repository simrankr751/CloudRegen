package org.cloudstudios.cloudregen.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.cloudstudios.cloudregen.region.RegionMetadata;
import org.cloudstudios.cloudregen.storage.RegionRepository;
import org.cloudstudios.cloudregen.tracking.RegionChangeTracker;

public final class BlockTrackListener implements Listener {
    private final RegionRepository repository;
    private final RegionChangeTracker tracker;

    public BlockTrackListener(RegionRepository repository, RegionChangeTracker tracker) {
        this.repository = repository;
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        trackBlock(event.getBlock(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        trackBlock(event.getBlock(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            trackBlock(block, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            trackBlock(block, false);
        }
    }

    private void trackBlock(Block block, boolean placed) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        for (RegionMetadata region : repository.all()) {
            if (!block.getWorld().getName().equals(region.getWorld())) continue;
            if (x < region.getMinX() || x > region.getMaxX() || y < region.getMinY() || y > region.getMaxY() || z < region.getMinZ() || z > region.getMaxZ()) continue;
            if (placed) {
                tracker.trackPlaced(region, x, y, z);
            } else {
                tracker.trackBroken(region, x, y, z);
            }
        }
    }
}
