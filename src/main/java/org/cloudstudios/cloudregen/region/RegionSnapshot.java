package org.cloudstudios.cloudregen.region;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;

public final class RegionSnapshot {
    private final List<String> palette;
    private final Long2ObjectOpenHashMap<IntList> chunkPacked;

    public RegionSnapshot() {
        this.palette = new ArrayList<>();
        this.chunkPacked = new Long2ObjectOpenHashMap<>();
    }

    public List<String> palette() {
        return palette;
    }

    public Long2ObjectOpenHashMap<IntList> chunkPacked() {
        return chunkPacked;
    }

    public void addChunkEntry(long chunkKey, int value) {
        IntList list = chunkPacked.get(chunkKey);
        if (list == null) {
            list = new IntArrayList(1024);
            chunkPacked.put(chunkKey, list);
        }
        list.add(value);
    }

    public static int packLocalState(int localX, int y, int localZ, int paletteId) {
        return ((localX & 0xF) << 28) | ((localZ & 0xF) << 24) | ((y + 1024) & 0x7FF) << 13 | (paletteId & 0x1FFF);
    }

    public static int unpackLocalX(int packed) {
        return (packed >>> 28) & 0xF;
    }

    public static int unpackLocalZ(int packed) {
        return (packed >>> 24) & 0xF;
    }

    public static int unpackY(int packed) {
        return ((packed >>> 13) & 0x7FF) - 1024;
    }

    public static int unpackPalette(int packed) {
        return packed & 0x1FFF;
    }
}
