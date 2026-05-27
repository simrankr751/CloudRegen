package org.cloudstudios.cloudregen.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.cloudstudios.cloudregen.region.RegionMetadata;
import org.cloudstudios.cloudregen.utils.Text;

import java.util.List;

public final class RegionBrowserGui {
    public static final String TITLE = "&8✦ &bCloudRegen &8» &fRegion Browser";
    public static final int SLOT_PREV = 45;
    public static final int SLOT_CLOSE = 46;
    public static final int SLOT_REFRESH = 47;
    public static final int SLOT_PAGE_INFO = 49;
    public static final int SLOT_CREATE = 52;
    public static final int SLOT_NEXT = 53;

    public String title() {
        return Text.color(TITLE);
    }

    public void open(Player player, List<RegionMetadata> regions, int page, int totalPages) {
        Inventory inv = Bukkit.createInventory(null, 54, title());
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, GuiItemBuilder.of(i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8 ? Material.GRAY_STAINED_GLASS_PANE : Material.BLACK_STAINED_GLASS_PANE)
                    .name("&8 ")
                    .lore("&8")
                    .build());
        }

        inv.setItem(4, GuiItemBuilder.of(Material.NETHER_STAR)
                .name("&8✦ &bCloudRegen &8» &fRegion Manager")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Browse, edit and manage regions",
                        "&7from a single premium interface.",
                        "&8",
                        "&7Total Regions: &f" + regions.size(),
                        GuiTheme.DIVIDER
                )
                .build());

        List<Integer> slots = GuiTheme.contentSlots();
        int start = page * slots.size();
        for (int i = 0; i < slots.size(); i++) {
            int index = start + i;
            if (index >= regions.size()) {
                break;
            }
            RegionMetadata region = regions.get(index);
            inv.setItem(slots.get(i), GuiItemBuilder.of(Material.BOOK)
                    .name("&8✦ &b" + region.getDisplayName())
                    .lore(
                            GuiTheme.DIVIDER,
                            "&7Region: &f" + region.getName(),
                            "&7Mode: &f" + region.getMode().name(),
                            "&7Interval: &f" + region.getRegenIntervalSeconds() + "s",
                            "&7Audience: " + (region.isBroadcastMessage() ? "&6Server" : "&aRegion Players"),
                            "&8",
                            "&fClick to open editor",
                            GuiTheme.DIVIDER
                    )
                    .build());
        }

        inv.setItem(SLOT_PREV, GuiItemBuilder.of(Material.ARROW)
                .name("&8✦ &bPrevious Page")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Move to the previous page",
                        "&7of region entries.",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_NEXT, GuiItemBuilder.of(Material.ARROW)
                .name("&8✦ &bNext Page")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Move to the next page",
                        "&7of region entries.",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_PAGE_INFO, GuiItemBuilder.of(Material.PAPER)
                .name("&8✦ &fPage &b" + (page + 1) + "&7/&b" + Math.max(1, totalPages))
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Navigation controls are on",
                        "&7the bottom bar.",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_CREATE, GuiItemBuilder.of(Material.EMERALD)
                .name("&8✦ &aCreate Region")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Starts the save workflow",
                        "&7using your current selection.",
                        "&8",
                        "&fRuns: &b/cr save",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_REFRESH, GuiItemBuilder.of(Material.COMPASS)
                .name("&8✦ &bRefresh")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Reload this browser view",
                        "&7with latest region data.",
                        GuiTheme.DIVIDER
                )
                .build());
        inv.setItem(SLOT_CLOSE, GuiItemBuilder.of(Material.BARRIER)
                .name("&8✦ &cClose")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Close this menu.",
                        GuiTheme.DIVIDER
                )
                .build());

        player.openInventory(inv);
    }
}
