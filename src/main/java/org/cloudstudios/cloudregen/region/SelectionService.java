package org.cloudstudios.cloudregen.region;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
import java.util.UUID;

public final class SelectionService {
    private final Map<UUID, SelectionState> states = new Object2ObjectOpenHashMap<>();

    public synchronized void setPos1(UUID playerId, Location location) {
        SelectionState state = states.computeIfAbsent(playerId, ignored -> new SelectionState());
        state.world = location.getWorld();
        state.pos1 = location;
    }

    public synchronized void setPos2(UUID playerId, Location location) {
        SelectionState state = states.computeIfAbsent(playerId, ignored -> new SelectionState());
        state.world = location.getWorld();
        state.pos2 = location;
    }

    public synchronized RegionSelection getSelection(UUID playerId) {
        SelectionState state = states.get(playerId);
        if (state == null || state.world == null || state.pos1 == null || state.pos2 == null) {
            return null;
        }
        World world = state.world;
        return new RegionSelection(world, state.pos1.getBlockX(), state.pos1.getBlockY(), state.pos1.getBlockZ(), state.pos2.getBlockX(), state.pos2.getBlockY(), state.pos2.getBlockZ());
    }

    private static final class SelectionState {
        private World world;
        private Location pos1;
        private Location pos2;
    }
}
