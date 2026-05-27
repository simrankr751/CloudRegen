package org.cloudstudios.cloudregen.scheduler;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public final class FoliaSchedulerAdapter implements SchedulerAdapter {
    @Override
    public void runAsync(JavaPlugin plugin, Runnable runnable) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> runnable.run());
    }

    @Override
    public void runSync(JavaPlugin plugin, Runnable runnable) {
        plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> runnable.run());
    }

    @Override
    public void runAtLocation(JavaPlugin plugin, Location location, Runnable runnable) {
        plugin.getServer().getRegionScheduler().run(plugin, location, task -> runnable.run());
    }

    @Override
    public CancellableTask runTimerAsync(JavaPlugin plugin, Runnable runnable, long initialDelayTicks, long periodTicks) {
        io.papermc.paper.threadedregions.scheduler.ScheduledTask task = plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, t -> runnable.run(), initialDelayTicks * 50L, periodTicks * 50L, TimeUnit.MILLISECONDS);
        return task::cancel;
    }

    public static boolean isFolia() {
        try {
            Method method = Class.forName("org.bukkit.Bukkit").getMethod("getGlobalRegionScheduler");
            return method != null;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
