package org.cloudstudios.cloudregen.storage;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.cloudstudios.cloudregen.config.ConfigManager;
import org.cloudstudios.cloudregen.region.RegionSnapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class BinaryRegionIO {
    private static final int MAGIC = 0x4352474E;
    private static final int VERSION = 1;
    private final ConfigManager config;

    public BinaryRegionIO(ConfigManager config) {
        this.config = config;
    }

    public void write(File file, RegionSnapshot snapshot) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new FileOutputStream(file), new java.util.zip.Deflater(config.compressionLevel())), config.writeBufferSize()))) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(snapshot.palette().size());
            for (String state : snapshot.palette()) {
                out.writeUTF(state);
            }
            out.writeInt(snapshot.chunkPacked().size());
            for (Long2ObjectMap.Entry<IntList> entry : snapshot.chunkPacked().long2ObjectEntrySet()) {
                out.writeLong(entry.getLongKey());
                IntList list = entry.getValue();
                out.writeInt(list.size());
                for (int i = 0; i < list.size(); i++) {
                    out.writeInt(list.getInt(i));
                }
            }
        }
    }

    public RegionSnapshot read(File file) throws IOException {
        RegionSnapshot snapshot = new RegionSnapshot();
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new InflaterInputStream(new FileInputStream(file)), config.writeBufferSize()))) {
            int magic = in.readInt();
            int version = in.readInt();
            if (magic != MAGIC || version != VERSION) {
                throw new IOException("Invalid snapshot format");
            }
            int paletteSize = in.readInt();
            for (int i = 0; i < paletteSize; i++) {
                snapshot.palette().add(in.readUTF());
            }
            int chunkCount = in.readInt();
            for (int i = 0; i < chunkCount; i++) {
                long key = in.readLong();
                int entries = in.readInt();
                for (int j = 0; j < entries; j++) {
                    snapshot.addChunkEntry(key, in.readInt());
                }
            }
        }
        return snapshot;
    }
}
