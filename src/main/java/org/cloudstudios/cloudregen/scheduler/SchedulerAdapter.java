package org.cloudstudios.cloudregen.scheduler;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public interface SchedulerAdapter {
    void runAsync(JavaPlugin plugin, Runnable runnable);
    void runSync(JavaPlugin plugin, Runnable runnable);
    void runAtLocation(JavaPlugin plugin, Location location, Runnable runnable);
    CancellableTask runTimerAsync(JavaPlugin plugin, Runnable runnable, long initialDelayTicks, long periodTicks);
}
