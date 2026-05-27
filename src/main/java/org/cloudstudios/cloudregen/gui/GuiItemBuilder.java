package org.cloudstudios.cloudregen.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.cloudstudios.cloudregen.utils.Text;

import java.util.ArrayList;
import java.util.List;

public final class GuiItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;
    private final List<String> lore = new ArrayList<>();

    private GuiItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public static GuiItemBuilder of(Material material) {
        return new GuiItemBuilder(material);
    }

    public GuiItemBuilder name(String name) {
        meta.setDisplayName(Text.color(name));
        return this;
    }

    public GuiItemBuilder lore(String... lines) {
        for (String line : lines) {
            lore.add(Text.color(line));
        }
        return this;
    }

    public ItemStack build() {
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
