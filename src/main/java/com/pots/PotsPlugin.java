package com.pots;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PotsPlugin extends JavaPlugin {

    private boolean countdownRunning = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getCommand("pots") != null) {
            getCommand("pots").setExecutor(new PotsCommand(this));
        }
        getServer().getPluginManager().registerEvents(new PotsListener(), this);
    }

    public boolean isCountdownRunning() {
        return countdownRunning;
    }

    /** Duration in seconds for a level: level I = 8 minutes, level II = 1 minute 30 seconds by default. */
    public int durationSeconds(int level) {
        if (level == 2) {
            return Math.max(1, getConfig().getInt("duration-seconds.level-2", 90));
        }
        return Math.max(1, getConfig().getInt("duration-seconds.level-1", 480));
    }

    /** Fixed duration in seconds for potions that ignore levels (Weaving = 180), or -1 if the potion uses levels. */
    public int fixedDurationSeconds(PotsGui.Entry entry) {
        int fallback = entry.id().equals("weaving") ? 180 : -1;
        return getConfig().getInt("fixed-duration-seconds." + entry.id(), fallback);
    }

    /** Runs the countdown, then gives every online player the chosen effects. */
    public void startCountdown(Player starter, List<PotsGui.Pick> chosen) {
        if (countdownRunning) {
            starter.sendMessage(Component.text("A countdown is already running.", NamedTextColor.RED));
            return;
        }
        if (chosen.isEmpty()) {
            starter.sendMessage(Component.text("Select at least one potion first.", NamedTextColor.RED));
            return;
        }

        countdownRunning = true;
        final List<PotsGui.Pick> picks = new ArrayList<>(chosen);
        final int seconds = Math.max(1, getConfig().getInt("countdown-seconds", 3));

        new BukkitRunnable() {
            int left = seconds;

            @Override
            public void run() {
                if (left > 0) {
                    Title title = Title.title(
                            Component.text(String.valueOf(left), NamedTextColor.GOLD),
                            Component.text("Potions incoming", NamedTextColor.GRAY),
                            Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ofMillis(100)));
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.showTitle(title);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    }
                    left--;
                    return;
                }

                cancel();
                countdownRunning = false;
                applyToEveryone(picks);
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    @SuppressWarnings("deprecation")
    private void applyToEveryone(List<PotsGui.Pick> picks) {
        StringBuilder names = new StringBuilder();
        for (PotsGui.Pick pick : picks) {
            if (names.length() > 0) names.append(", ");
            names.append(pick.entry().name());
            if (fixedDurationSeconds(pick.entry()) <= 0) {
                names.append(' ').append(PotsGui.roman(pick.level()));
            }
        }
        Component message = Component.text("Everyone got potted with: ", NamedTextColor.GREEN)
                .append(Component.text(names.toString(), NamedTextColor.WHITE));

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (PotsGui.Pick pick : picks) {
                PotionEffectType type = pick.entry().type();
                if (type.equals(PotionEffectType.INSTANT_HEALTH)) {
                    p.setHealth(p.getMaxHealth());
                } else {
                    int fixed = fixedDurationSeconds(pick.entry());
                    int duration = (fixed > 0 ? fixed : durationSeconds(pick.level())) * 20;
                    int amplifier = fixed > 0 ? 0 : pick.level() - 1; // level I = 0, level II = 1
                    p.addPotionEffect(new PotionEffect(type, duration, amplifier));
                }
            }
            p.playSound(p.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1f, 1f);
            p.sendMessage(message);
        }
    }
}
