package org.cloudstudios.cloudregen.region;

import org.bukkit.Location;

import java.util.UUID;

public final class RegionMetadata {
    private final UUID id;
    private final String name;
    private String displayName;
    private String world;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private int regenIntervalSeconds;
    private RegionMode mode;
    private String message;
    private boolean safetyEnabled;
    private boolean broadcastMessage;
    private long totalRegens;
    private long totalBlocksApplied;

    public RegionMetadata(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.displayName = name;
        this.regenIntervalSeconds = 300;
        this.mode = RegionMode.FULL;
        this.safetyEnabled = true;
        this.broadcastMessage = false;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    public int getMinX() { return minX; }
    public void setMinX(int minX) { this.minX = minX; }
    public int getMinY() { return minY; }
    public void setMinY(int minY) { this.minY = minY; }
    public int getMinZ() { return minZ; }
    public void setMinZ(int minZ) { this.minZ = minZ; }
    public int getMaxX() { return maxX; }
    public void setMaxX(int maxX) { this.maxX = maxX; }
    public int getMaxY() { return maxY; }
    public void setMaxY(int maxY) { this.maxY = maxY; }
    public int getMaxZ() { return maxZ; }
    public void setMaxZ(int maxZ) { this.maxZ = maxZ; }
    public int getRegenIntervalSeconds() { return regenIntervalSeconds; }
    public void setRegenIntervalSeconds(int regenIntervalSeconds) { this.regenIntervalSeconds = regenIntervalSeconds; }
    public RegionMode getMode() { return mode; }
    public void setMode(RegionMode mode) { this.mode = mode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isSafetyEnabled() { return safetyEnabled; }
    public void setSafetyEnabled(boolean safetyEnabled) { this.safetyEnabled = safetyEnabled; }
    public boolean isBroadcastMessage() { return broadcastMessage; }
    public void setBroadcastMessage(boolean broadcastMessage) { this.broadcastMessage = broadcastMessage; }
    public long getTotalRegens() { return totalRegens; }
    public void setTotalRegens(long totalRegens) { this.totalRegens = totalRegens; }
    public long getTotalBlocksApplied() { return totalBlocksApplied; }
    public void setTotalBlocksApplied(long totalBlocksApplied) { this.totalBlocksApplied = totalBlocksApplied; }

    public boolean contains(Location location) {
        if (location.getWorld() == null || world == null || !location.getWorld().getName().equalsIgnoreCase(world)) {
            return false;
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
