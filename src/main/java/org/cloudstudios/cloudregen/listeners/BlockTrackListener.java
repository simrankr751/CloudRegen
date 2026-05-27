package org.cloudstudios.cloudregen.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
        int x = event.getBlock().getX();
        int y = event.getBlock().getY();
        int z = event.getBlock().getZ();
        for (RegionMetadata region : repository.all()) {
            if (!event.getBlock().getWorld().getName().equals(region.getWorld())) continue;
            if (x < region.getMinX() || x > region.getMaxX() || y < region.getMinY() || y > region.getMaxY() || z < region.getMinZ() || z > region.getMaxZ()) continue;
            tracker.trackBroken(region, x, y, z);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        int x = event.getBlock().getX();
        int y = event.getBlock().getY();
        int z = event.getBlock().getZ();
        for (RegionMetadata region : repository.all()) {
            if (!event.getBlock().getWorld().getName().equals(region.getWorld())) continue;
            if (x < region.getMinX() || x > region.getMaxX() || y < region.getMinY() || y > region.getMaxY() || z < region.getMinZ() || z > region.getMaxZ()) continue;
            tracker.trackPlaced(region, x, y, z);
        }
    }
}
