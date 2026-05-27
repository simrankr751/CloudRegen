package org.cloudstudios.cloudregen.gui;

import java.util.List;

public final class GuiTheme {
    public static final String PRIMARY = "&b";
    public static final String ACCENT = "&3";
    public static final String TEXT = "&f";
    public static final String MUTED = "&7";
    public static final String DIM = "&8";
    public static final String GOOD = "&a";
    public static final String BAD = "&c";
    public static final String WARN = "&6";
    public static final String DIVIDER = "&8━━━━━━━━━━━━━━━━━━";

    private GuiTheme() {
    }

    public static List<Integer> contentSlots() {
        return List.of(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        );
    }
}
