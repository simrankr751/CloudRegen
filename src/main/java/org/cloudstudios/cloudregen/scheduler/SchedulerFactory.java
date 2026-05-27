package org.cloudstudios.cloudregen.scheduler;

public final class SchedulerFactory {
    private SchedulerFactory() {
    }

    public static SchedulerAdapter create() {
        return FoliaSchedulerAdapter.isFolia() ? new FoliaSchedulerAdapter() : new PaperSchedulerAdapter();
    }
}
