package com.dreamz.dhub.listeners;

import com.dreamz.dhub.dHub;
import com.dreamz.dhub.server.Server;
import com.dreamz.dhub.server.ServerManager;
import com.dreamz.dhub.utils.Cooldowns;
import com.dreamz.dhub.utils.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListeners implements Listener {

    private dHub main = dHub.getInstance();
    private ServerManager serverManager = main.getServerManager();

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem() == null) return;
        if (!e.getCurrentItem().hasItemMeta()) return;

        if (e.getInventory().getTitle().contains("Server Selector")) {
            e.setCancelled(true);

            if (Cooldowns.tryCooldown(p.getUniqueId(), "gui-cooldown", 3000)) {
                Server s = serverManager.getServerFromItem(e.getCurrentItem());

                if (s != null) {
                    if (!s.isInQueue(p.getUniqueId())) {
                        serverManager.addToServerQueue(p, s);
                    } else {
                        serverManager.removeFromServerQueue(p, s);

                        MessageManager.sendMessage(p, "&cYou have left the queue for " + s.getName() + ".");
                    }

                    if (!s.getServerQueue().isEnabled()) {
                        p.closeInventory();
                    }
                } else {
                    e.getInventory().remove(e.getCurrentItem());
                    p.closeInventory();
                }
            } else {
                MessageManager.sendMessage(p, "&cYou can use this again in &c&l" + (Cooldowns.getCooldown(p.getUniqueId(), "gui-cooldown") / 1000) + " &cseconds.");
                p.closeInventory();
            }
        }
    }
}
