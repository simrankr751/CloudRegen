package org.cloudstudios.cloudregen.storage;

import org.bukkit.configuration.file.YamlConfiguration;
import org.cloudstudios.cloudregen.region.RegionMetadata;
import org.cloudstudios.cloudregen.region.RegionMode;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class MetadataIO {
    public void write(File file, RegionMetadata metadata) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("id", metadata.getId().toString());
        yaml.set("name", metadata.getName());
        yaml.set("display-name", metadata.getDisplayName());
        yaml.set("world", metadata.getWorld());
        yaml.set("min.x", metadata.getMinX());
        yaml.set("min.y", metadata.getMinY());
        yaml.set("min.z", metadata.getMinZ());
        yaml.set("max.x", metadata.getMaxX());
        yaml.set("max.y", metadata.getMaxY());
        yaml.set("max.z", metadata.getMaxZ());
        yaml.set("regen.interval", metadata.getRegenIntervalSeconds());
        yaml.set("regen.mode", metadata.getMode().name());
        yaml.set("regen.message", metadata.getMessage());
        yaml.set("regen.safety", metadata.isSafetyEnabled());
        yaml.set("regen.broadcast-message", metadata.isBroadcastMessage());
        yaml.set("stats.regens", metadata.getTotalRegens());
        yaml.set("stats.blocks", metadata.getTotalBlocksApplied());
        yaml.save(file);
    }

    public RegionMetadata read(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        RegionMetadata metadata = new RegionMetadata(UUID.fromString(yaml.getString("id")), yaml.getString("name"));
        metadata.setDisplayName(yaml.getString("display-name", metadata.getName()));
        metadata.setWorld(yaml.getString("world"));
        metadata.setMinX(yaml.getInt("min.x"));
        metadata.setMinY(yaml.getInt("min.y"));
        metadata.setMinZ(yaml.getInt("min.z"));
        metadata.setMaxX(yaml.getInt("max.x"));
        metadata.setMaxY(yaml.getInt("max.y"));
        metadata.setMaxZ(yaml.getInt("max.z"));
        metadata.setRegenIntervalSeconds(yaml.getInt("regen.interval", 300));
        metadata.setMode(RegionMode.valueOf(yaml.getString("regen.mode", "FULL")));
        metadata.setMessage(yaml.getString("regen.message"));
        metadata.setSafetyEnabled(yaml.getBoolean("regen.safety", true));
        metadata.setBroadcastMessage(yaml.getBoolean("regen.broadcast-message", false));
        metadata.setTotalRegens(yaml.getLong("stats.regens", 0));
        metadata.setTotalBlocksApplied(yaml.getLong("stats.blocks", 0));
        return metadata;
    }
}
