package com.pots;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PotsCommand implements CommandExecutor {

    private final PotsPlugin plugin;

    public PotsCommand(PotsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }
        if (!player.isOp()) {
            player.sendMessage(Component.text("Only operators can use this command.", NamedTextColor.RED));
            return true;
        }
        if (plugin.isCountdownRunning()) {
            player.sendMessage(Component.text("A countdown is already running.", NamedTextColor.RED));
            return true;
        }
        new PotsGui(plugin).open(player);
        return true;
    }
}
