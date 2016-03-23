package com.dreamz.dhub.listeners;

import com.dreamz.dhub.dHub;
import com.dreamz.dhub.server.Server;
import com.dreamz.dhub.server.ServerManager;
import com.dreamz.dhub.utils.ItemBuilder;
import com.dreamz.dhub.utils.MessageManager;
import com.dreamz.dhub.utils.PlayerUtility;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class PlayerListeners implements Listener {

    private dHub main = dHub.getInstance();
    private ServerManager serverManager = main.getServerManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        e.setJoinMessage(null);

        if (!main.getInHubList().contains(p.getUniqueId())) {
            for (Player all : PlayerUtility.getOnlinePlayers()) {
                if (!main.getInHubList().contains(all.getUniqueId())) {
                    MessageManager.sendMessage(all, "&8Join> &7" + p.getName());
                }
            }

            p.teleport(serverManager.getSpawnLocation());

            p.getInventory().clear();
            p.getInventory().setItem(0, new ItemBuilder(Material.INK_SACK).durability(10).name("&bPlayer Visibility").build());
            p.getInventory().setItem(4, new ItemBuilder(Material.COMPASS).name("&cServer Teleporter").build());
            p.getInventory().setItem(8, new ItemBuilder(Material.FIREWORK).name("&6Jump Stick").build());
            p.getInventory().setHeldItemSlot(4);
            p.updateInventory();

            MessageManager.sendMessage(p, main.getConfig().getString("motd"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);

        for (Player all : PlayerUtility.getOnlinePlayers()) {
            if (!main.getInHubList().contains(all.getUniqueId())) {
                MessageManager.sendMessage(all, "&8Quit> &7" + e.getPlayer().getName());
            }
        }

        for (Server s : serverManager.getServers()) {
            if (s.isInQueue(e.getPlayer().getUniqueId())) {
                serverManager.removeFromServerQueue(e.getPlayer(), s);
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (main.getInHubList().contains(e.getPlayer().getUniqueId())) {
            return;
        }

        if (!e.getPlayer().hasPermission("hub.build")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSheepDye(SheepDyeWoolEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (main.getInHubList().contains(e.getPlayer().getUniqueId())) {
            return;
        }

        if (!e.getPlayer().hasPermission("hub.build")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (main.getInHubList().contains(e.getWhoClicked().getUniqueId())) {
            return;
        }

        if (e.getWhoClicked().getGameMode() != GameMode.CREATIVE)
            e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (main.getInHubList().contains(e.getPlayer().getUniqueId())) {
            return;
        }

        if (e.getTo().getBlockY() <= 0) {
            e.getPlayer().teleport(serverManager.getSpawnLocation());
        }
    }

    @EventHandler
    public void onFoodLoss(FoodLevelChangeEvent e) {
        if (main.getInHubList().contains(e.getEntity().getUniqueId())) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (main.getInHubList().contains(e.getPlayer().getUniqueId())) {
            return;
        }

        e.getItemDrop().remove();
        ItemStack droppedItem = e.getItemDrop().getItemStack().clone();

        if (droppedItem.getType() == Material.INK_SACK) {
            e.getPlayer().getInventory().setItem(0, droppedItem);
        } else if (droppedItem.getType() == Material.COMPASS) {
            e.getPlayer().getInventory().setItem(4, droppedItem);
        } else if (droppedItem.getType() == Material.FIREWORK) {
            e.getPlayer().getInventory().setItem(8, droppedItem);
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        if (main.getInHubList().contains(e.getPlayer().getUniqueId())) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player hit = (Player) e.getEntity();

            if (!main.getInHubList().contains(hit.getUniqueId())) {
                if (e.getDamager() instanceof Player) {
                    Player hitter = (Player) e.getDamager();

                    if (!main.getInHubList().contains(hitter.getUniqueId())) {
                        //Where I fucked with packets. It already contains the valid checks.
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (main.getInHubList().contains(e.getEntity().getUniqueId())) {
                return;
            }

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        e.getPlayer().updateInventory();

        if (main.getInHubList().contains(e.getPlayer().getUniqueId())) {
            return;
        }

        if (!e.getPlayer().hasPermission("hub.build")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onStomp(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock().getType() == Material.SOIL) {
            e.setCancelled(true);
        }
    }
}
