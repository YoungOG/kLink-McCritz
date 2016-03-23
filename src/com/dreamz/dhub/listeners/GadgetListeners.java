package com.dreamz.dhub.listeners;

import com.dreamz.dhub.dHub;
import com.dreamz.dhub.utils.Cooldowns;
import com.dreamz.dhub.utils.ItemBuilder;
import com.dreamz.dhub.utils.MessageManager;
import com.dreamz.dhub.utils.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;

public class GadgetListeners implements Listener {

    private dHub main = dHub.getInstance();
    private ArrayList<UUID> vanishList = new ArrayList<>();

    @EventHandler
    public void onJumpStick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (p.getItemInHand() == null) return;
        if (!p.getItemInHand().hasItemMeta()) return;

        if (p.getItemInHand().getType() == Material.FIREWORK) {
            if (e.getAction().toString().contains("LEFT") || e.getAction().toString().contains("RIGHT")) {
                e.setCancelled(true);

                if (main.getInHubList().contains(p.getUniqueId())) {
                    return;
                }

                if ((!p.hasMetadata("hopper") || (p.getMetadata("hopper").get(0)).asInt() == 1) && !p.isFlying()) {
                    p.getWorld().playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
                    p.setVelocity(new Vector(p.getLocation().getDirection().getX() * 1.6D, 0.69D, p.getLocation().getDirection().getZ() * 1.6D));

                    if (!p.hasMetadata("hopper")) {
                        p.setMetadata("hopper", new FixedMetadataValue(main, 1));
                    } else {
                        p.setMetadata("hopper", new FixedMetadataValue(main, 2));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if (p.hasMetadata("hopper") && p.isOnGround()) {
            p.removeMetadata("hopper", main);
        }
    }

    @EventHandler
    public void onVanishItem(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction().toString().contains("LEFT") || e.getAction().toString().contains("RIGHT")) {
            if (p.getItemInHand().getType() == Material.INK_SACK && p.getItemInHand().hasItemMeta()) {
                if (main.getInHubList().contains(p.getUniqueId())) {
                    return;
                }

                if (Cooldowns.tryCooldown(p.getUniqueId(), "vanish-cooldown", 5000)) {
                    if (!vanishList.contains(p.getUniqueId())) {
                        vanishList.add(p.getUniqueId());
                        ItemStack vanishItem = new ItemBuilder(Material.INK_SACK).durability(8).name("&bPlayer Visibility (&aon&b)").build();
                        p.setItemInHand(vanishItem);
                        MessageManager.sendMessage(p, "&bYou have vanished all players.");

                        for (Player players : PlayerUtility.getOnlinePlayers()) {
                            p.hidePlayer(players);
                        }
                    } else {
                        vanishList.remove(p.getUniqueId());
                        ItemStack vanishItem = new ItemBuilder(Material.INK_SACK).durability(10).name("&bPlayer Visibility (&coff&b)").build();
                        p.setItemInHand(vanishItem);
                        MessageManager.sendMessage(p, "&bYou have un-vanished all player.");

                        for (Player players : PlayerUtility.getOnlinePlayers()) {
                            p.showPlayer(players);
                        }
                    }
                } else {
                    MessageManager.sendMessage(p, "&cYou can use this again in &c&l" + (Cooldowns.getCooldown(p.getUniqueId(), "vanish-cooldown") / 1000) + " &cseconds.");
                }
            }
        }
    }

    @EventHandler
    public void onServerSelector(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction().toString().contains("RIGHT")) {
            if (main.getInHubList().contains(p.getUniqueId())) {
                return;
            }

            if (p.getItemInHand().getType() == Material.COMPASS && p.getItemInHand().hasItemMeta()) {
                dHub.getInstance().getServerManager().showServerSelectionInventory(p);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        for (UUID id : vanishList) {
            Bukkit.getPlayer(id).hidePlayer(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (vanishList.contains(p.getUniqueId())) {
            vanishList.remove(p.getUniqueId());
        }
    }
}
