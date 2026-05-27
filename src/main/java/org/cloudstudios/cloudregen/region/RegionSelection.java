package org.cloudstudios.cloudregen.region;

import org.bukkit.Location;
import org.bukkit.World;

public record RegionSelection(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
    public int minX() { return Math.min(x1, x2); }
    public int minY() { return Math.min(y1, y2); }
    public int minZ() { return Math.min(z1, z2); }
    public int maxX() { return Math.max(x1, x2); }
    public int maxY() { return Math.max(y1, y2); }
    public int maxZ() { return Math.max(z1, z2); }
    public long volume() { return (long) (maxX() - minX() + 1) * (maxY() - minY() + 1) * (maxZ() - minZ() + 1); }
    public boolean contains(Location location) {
        if (location.getWorld() == null || !location.getWorld().getUID().equals(world.getUID())) return false;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= minX() && x <= maxX() && y >= minY() && y <= maxY() && z >= minZ() && z <= maxZ();
    }
}
