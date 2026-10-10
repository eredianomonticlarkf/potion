package com.pots;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class PotsListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof PotsGui gui)) return;

        // Nothing can be taken out of or moved around in this GUI
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.isOp()) {
            player.closeInventory();
            return;
        }
        if (event.getClickedInventory() == null
                || event.getClickedInventory() != event.getView().getTopInventory()) return;

        gui.handleClick(player, event.getRawSlot(), event.getClick());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof PotsGui) {
            event.setCancelled(true);
        }
    }
}
