package org.cloudstudios.cloudregen.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.cloudstudios.cloudregen.utils.Text;

public final class RegionDeleteConfirmGui {
    public static final int SLOT_CONFIRM = 21;
    public static final int SLOT_CANCEL = 23;
    public static final int SLOT_BACK = 49;

    public static String title() {
        return Text.color("&8✦ &cConfirm Deletion");
    }

    public void open(Player player, String regionName) {
        Inventory inv = Bukkit.createInventory(null, 54, title());
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, GuiItemBuilder.of(i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8 ? Material.RED_STAINED_GLASS_PANE : Material.BLACK_STAINED_GLASS_PANE)
                    .name("&8 ")
                    .lore("&8")
                    .build());
        }

        inv.setItem(4, GuiItemBuilder.of(Material.BARRIER)
                .name("&8✦ &cDelete Region")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7You are about to delete:",
                        "&f" + regionName,
                        "&8",
                        "&cThis action is permanent.",
                        GuiTheme.DIVIDER
                )
                .build());

        inv.setItem(SLOT_CONFIRM, GuiItemBuilder.of(Material.LIME_WOOL)
                .name("&8✦ &aConfirm Delete")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Permanently remove this region",
                        "&7and its saved snapshot data.",
                        GuiTheme.DIVIDER
                )
                .build());

        inv.setItem(SLOT_CANCEL, GuiItemBuilder.of(Material.RED_WOOL)
                .name("&8✦ &cCancel")
                .lore(
                        GuiTheme.DIVIDER,
                        "&7Abort deletion and return",
                        "&7to the editor safely.",
                        GuiTheme.DIVIDER
                )
                .build());

        inv.setItem(SLOT_BACK, GuiItemBuilder.of(Material.ARROW)
                .name("&8✦ &bBack to Editor")
                .lore(GuiTheme.DIVIDER, "&7Return without deleting.", GuiTheme.DIVIDER)
                .build());

        player.openInventory(inv);
    }
}
