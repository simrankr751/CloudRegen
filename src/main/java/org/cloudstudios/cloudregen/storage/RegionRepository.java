package org.cloudstudios.cloudregen.storage;

import org.cloudstudios.cloudregen.region.RegionMetadata;
import org.cloudstudios.cloudregen.region.RegionSnapshot;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionRepository {
    private final File root;
    private final BinaryRegionIO binary;
    private final MetadataIO metadataIO;
    private final Map<String, RegionMetadata> metadataCache = new ConcurrentHashMap<>();

    public RegionRepository(File dataFolder, BinaryRegionIO binary, MetadataIO metadataIO) {
        this.root = new File(dataFolder, "regions");
        this.binary = binary;
        this.metadataIO = metadataIO;
        if (!root.exists()) {
            root.mkdirs();
        }
        load();
    }

    public Collection<RegionMetadata> all() {
        return metadataCache.values();
    }

    public RegionMetadata get(String name) {
        return metadataCache.get(name.toLowerCase());
    }

    public void save(String name, RegionMetadata metadata, RegionSnapshot snapshot) throws IOException {
        File folder = new File(root, name.toLowerCase());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        binary.write(new File(folder, "snapshot.bin"), snapshot);
        metadataIO.write(new File(folder, "meta.yml"), metadata);
        metadataCache.put(name.toLowerCase(), metadata);
    }

    public RegionSnapshot loadSnapshot(String name) throws IOException {
        return binary.read(new File(new File(root, name.toLowerCase()), "snapshot.bin"));
    }

    public boolean delete(String name) {
        File folder = new File(root, name.toLowerCase());
        metadataCache.remove(name.toLowerCase());
        return deleteRecursive(folder);
    }

    private void load() {
        File[] folders = root.listFiles(File::isDirectory);
        if (folders == null) {
            return;
        }
        for (File folder : folders) {
            File metaFile = new File(folder, "meta.yml");
            if (metaFile.exists()) {
                RegionMetadata metadata = metadataIO.read(metaFile);
                metadataCache.put(metadata.getName().toLowerCase(), metadata);
            }
        }
    }

    private boolean deleteRecursive(File file) {
        if (!file.exists()) return false;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursive(child);
            }
        }
        return file.delete();
    }
}
