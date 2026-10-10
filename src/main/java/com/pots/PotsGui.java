package com.pots;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PotsGui implements InventoryHolder {

    /** One selectable potion. */
    public record Entry(String id, String name, PotionEffectType type) {}

    /** A potion the op picked, with its level (1 or 2). */
    public record Pick(Entry entry, int level) {}

    public static final List<Entry> ENTRIES = List.of(
            new Entry("speed", "Speed", PotionEffectType.SPEED),
            new Entry("strength", "Strength", PotionEffectType.STRENGTH),
            new Entry("regeneration", "Regeneration", PotionEffectType.REGENERATION),
            new Entry("resistance", "Resistance", PotionEffectType.RESISTANCE),
            new Entry("fire_resistance", "Fire Resistance", PotionEffectType.FIRE_RESISTANCE),
            new Entry("absorption", "Absorption", PotionEffectType.ABSORPTION),
            new Entry("jump_boost", "Jump Boost", PotionEffectType.JUMP_BOOST),
            new Entry("night_vision", "Night Vision", PotionEffectType.NIGHT_VISION),
            new Entry("invisibility", "Invisibility", PotionEffectType.INVISIBILITY),
            new Entry("water_breathing", "Water Breathing", PotionEffectType.WATER_BREATHING),
            new Entry("haste", "Haste", PotionEffectType.HASTE),
            new Entry("health_boost", "Health Boost", PotionEffectType.HEALTH_BOOST),
            new Entry("saturation", "Saturation", PotionEffectType.SATURATION),
            new Entry("slow_falling", "Slow Falling", PotionEffectType.SLOW_FALLING),
            new Entry("instant_health", "Instant Health", PotionEffectType.INSTANT_HEALTH),
            new Entry("luck", "Luck", PotionEffectType.LUCK),
            new Entry("dolphins_grace", "Dolphin's Grace", PotionEffectType.DOLPHINS_GRACE),
            new Entry("conduit_power", "Conduit Power", PotionEffectType.CONDUIT_POWER),
            new Entry("weaving", "Weaving", PotionEffectType.WEAVING)
    );

    private static final int[] EFFECT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };
    private static final int CLEAR_SLOT = 38;
    private static final int START_SLOT = 40;
    private static final int CLOSE_SLOT = 42;

    private final PotsPlugin plugin;
    private final Inventory inventory;
    /** Selected potions mapped to their level (1 or 2). */
    private final Map<Entry, Integer> selected = new LinkedHashMap<>();

    public PotsGui(PotsPlugin plugin) {
        this.plugin = plugin;
        this.inventory = plugin.getServer().createInventory(this, 45,
                Component.text("Select Pots", NamedTextColor.DARK_PURPLE));
        render();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void handleClick(Player player, int slot, ClickType click) {
        for (int i = 0; i < ENTRIES.size(); i++) {
            if (EFFECT_SLOTS[i] != slot) continue;

            Entry entry = ENTRIES.get(i);
            boolean fixed = plugin.fixedDurationSeconds(entry) > 0;
            if (!fixed && (click.isRightClick() || click.isShiftClick())) {
                // Switch between level I and II (selects the potion if it was not selected)
                int current = selected.getOrDefault(entry, 1);
                selected.put(entry, current == 1 ? 2 : 1);
            } else if (selected.containsKey(entry)) {
                selected.remove(entry);
            } else {
                selected.put(entry, 1);
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            render();
            return;
        }

        if (slot == CLEAR_SLOT) {
            selected.clear();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 0.8f);
            render();
        } else if (slot == CLOSE_SLOT) {
            player.closeInventory();
        } else if (slot == START_SLOT) {
            if (selected.isEmpty()) {
                player.sendMessage(Component.text("Select at least one potion first.", NamedTextColor.RED));
                return;
            }
            List<Pick> picks = new ArrayList<>();
            for (Map.Entry<Entry, Integer> e : selected.entrySet()) {
                picks.add(new Pick(e.getKey(), e.getValue()));
            }
            player.closeInventory();
            plugin.startCountdown(player, picks);
        }
    }

    private void render() {
        inventory.clear();

        ItemStack filler = simple(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), List.of());
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);

        for (int i = 0; i < ENTRIES.size(); i++) {
            inventory.setItem(EFFECT_SLOTS[i], potionItem(ENTRIES.get(i)));
        }

        inventory.setItem(CLEAR_SLOT, simple(Material.RED_DYE,
                Component.text("Clear selection", NamedTextColor.RED),
                List.of(Component.text("Unselect every potion", NamedTextColor.GRAY))));

        inventory.setItem(START_SLOT, simple(Material.LIME_DYE,
                Component.text("Start", NamedTextColor.GREEN),
                List.of(
                        Component.text(selected.size() + " potion(s) selected", NamedTextColor.GRAY),
                        Component.text("Click to start the countdown", NamedTextColor.GRAY))));

        inventory.setItem(CLOSE_SLOT, simple(Material.BARRIER,
                Component.text("Close", NamedTextColor.RED), List.of()));
    }

    private ItemStack potionItem(Entry entry) {
        boolean on = selected.containsKey(entry);
        int level = selected.getOrDefault(entry, 1);
        int fixedSeconds = plugin.fixedDurationSeconds(entry);
        boolean fixed = fixedSeconds > 0;

        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setColor(entry.type().getColor());
        meta.displayName(plain(Component.text(
                entry.name() + (on && !fixed ? " " + roman(level) : ""),
                on ? NamedTextColor.GREEN : NamedTextColor.GRAY)));

        List<Component> lore = new ArrayList<>();
        if (!fixed) {
            lore.add(plain(Component.text("Level: " + roman(level), NamedTextColor.DARK_GRAY)));
        }
        lore.add(plain(Component.text(
                "Duration: " + formatDuration(fixed ? fixedSeconds : plugin.durationSeconds(level)),
                NamedTextColor.DARK_GRAY)));
        lore.add(Component.empty());
        lore.add(plain(on
                ? Component.text("Selected - left click to unselect", NamedTextColor.GREEN)
                : Component.text("Left click to select", NamedTextColor.YELLOW)));
        if (!fixed) {
            lore.add(plain(Component.text("Right click: switch level I / II", NamedTextColor.AQUA)));
        }
        meta.lore(lore);

        meta.setEnchantmentGlintOverride(on);
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack simple(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plain(name));
        List<Component> plainLore = new ArrayList<>();
        for (Component c : lore) plainLore.add(plain(c));
        meta.lore(plainLore);
        item.setItemMeta(meta);
        return item;
    }

    private static Component plain(Component c) {
        return c.decoration(TextDecoration.ITALIC, false);
    }

    static String roman(int n) {
        return n == 2 ? "II" : "I";
    }

    static String formatDuration(int seconds) {
        if (seconds < 60) return seconds + "s";
        int m = seconds / 60;
        int s = seconds % 60;
        return s == 0 ? m + "m" : m + "m " + s + "s";
    }
}
