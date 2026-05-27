package org.cloudstudios.cloudregen.scheduler;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class PaperSchedulerAdapter implements SchedulerAdapter {
    @Override
    public void runAsync(JavaPlugin plugin, Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void runSync(JavaPlugin plugin, Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void runAtLocation(JavaPlugin plugin, Location location, Runnable runnable) {
        runSync(plugin, runnable);
    }

    @Override
    public CancellableTask runTimerAsync(JavaPlugin plugin, Runnable runnable, long initialDelayTicks, long periodTicks) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, initialDelayTicks, periodTicks);
        return task::cancel;
    }
}
