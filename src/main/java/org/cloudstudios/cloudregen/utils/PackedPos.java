package org.cloudstudios.cloudregen.utils;

public final class PackedPos {
    private PackedPos() {
    }

    public static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    public static int x(long packed) {
        int raw = (int) (packed >> 38);
        return raw >= 0x2000000 ? raw - 0x4000000 : raw;
    }

    public static int y(long packed) {
        int raw = (int) (packed & 0xFFF);
        return raw >= 0x800 ? raw - 0x1000 : raw;
    }

    public static int z(long packed) {
        int raw = (int) ((packed >> 12) & 0x3FFFFFF);
        return raw >= 0x2000000 ? raw - 0x4000000 : raw;
    }
}
