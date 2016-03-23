package com.dreamz.dhub.listeners;

import com.dreamz.dhub.dHub;
import com.dreamz.dhub.server.Server;
import com.dreamz.dhub.server.ServerManager;
import com.dreamz.dhub.utils.Cooldowns;
import com.dreamz.dhub.utils.MessageManager;
import com.dreamz.dhub.utils.PlayerUtility;
import de.blablubbabc.insigns.SignSendEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ServerListeners implements Listener {

    private dHub main = dHub.getInstance();
    private ServerManager serverManager = main.getServerManager();

    @EventHandler
    public void onSignPlace(SignChangeEvent e) {
        Player p = e.getPlayer();

        if (p.hasPermission("hub.build")) {
            if (e.getLine(0).equalsIgnoreCase("[server]")) {
                String name = e.getLine(1);

                Server server = serverManager.getServer(name);

                if (server == null) {
                    MessageManager.sendMessage(p, "&cCould not find that server.");
                    e.getBlock().breakNaturally(new ItemStack(Material.AIR));
                    return;
                }

                server.getServerSignLocations().add(e.getBlock().getLocation());

                e.setLine(0, ChatColor.translateAlternateColorCodes('&', "{1}"));
                e.setLine(1, ChatColor.translateAlternateColorCodes('&', "{2}&b/{3}"));
                e.setLine(2, ChatColor.translateAlternateColorCodes('&', "{4}"));
                e.setLine(3, ChatColor.translateAlternateColorCodes('&', "{5}"));
                e.getBlock().getState().update(true);
                MessageManager.sendMessage(p, "&aServer sign has been added to &b" + serverManager.getServerFromServerSignLocation(e.getBlock().getLocation()).getName() + "&a.");
            }

            if (e.getLine(0).equalsIgnoreCase("[queue]")) {
                String name = e.getLine(1);

                Server server = serverManager.getServer(name);

                if (server == null) {
                    MessageManager.sendMessage(p, "&cCould not find that server.");
                    e.getBlock().breakNaturally(new ItemStack(Material.AIR));
                    return;
                }

                server.getQueueSignLocations().add(e.getBlock().getLocation());

                e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&b&nQueue Info"));
                e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&2U&0|&1M {6}"));
                e.setLine(2, ChatColor.translateAlternateColorCodes('&', "&aDonator {7}"));
                e.setLine(3, ChatColor.translateAlternateColorCodes('&', "&fDefault {8}"));
                e.getBlock().getState().update(true);
                MessageManager.sendMessage(p, "&aQueue sign has been added to &b" + server.getName() + "&a.");
            }
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (b.getState() instanceof Sign) {
            if (!p.hasPermission("hub.build")) {
                e.setCancelled(true);
                return;
            }

            if (serverManager.getServerFromServerSignLocation(b.getLocation()) != null) {
                Server s = serverManager.getServerFromServerSignLocation(b.getLocation());
                s.getServerSignLocations().remove(b.getLocation());

                MessageManager.sendMessage(p, "&aServer sign has been removed from &b" + s.getName() + "&a.");
            } else if (serverManager.getServerFromQueueSignLocation(b.getLocation()) != null) {
                Server s = serverManager.getServerFromQueueSignLocation(b.getLocation());
                s.getQueueSignLocations().remove(b.getLocation());

                MessageManager.sendMessage(p, "&aQueue sign has been removed from &b" + s.getName() + "&a.");
            }
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) e.getClickedBlock().getState();

                if (serverManager.getServerFromServerSignLocation(sign.getLocation()) != null) {
                    e.setCancelled(true);

                    Server server = serverManager.getServerFromServerSignLocation(sign.getLocation());

                    if (Cooldowns.tryCooldown(p.getUniqueId(), "sign-cooldown", 3000)) {
                        if (!server.isInQueue(p.getUniqueId())) {
                            serverManager.addToServerQueue(p, server);
                        } else {
                            serverManager.removeFromServerQueue(p, server);


                            MessageManager.sendMessage(p, "&cYou have left the queue for " + server.getName() + ".");
                        }
                    } else {
                        MessageManager.sendMessage(p, "&cYou can use this again in &c&l" + (Cooldowns.getCooldown(p.getUniqueId(), "sign-cooldown") / 1000) + " &cseconds.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSignUpdate(SignSendEvent e) {
        for (int i = 0; i < 4; i++) {
            String line = e.getLine(i);

            if (serverManager.getServerFromServerSignLocation(e.getLocation()) != null) {
                Server s = serverManager.getServerFromServerSignLocation(e.getLocation());

                if (line.contains("{1}")) {
                    e.setLine(i, e.getLine(i).replace("{1}", ChatColor.translateAlternateColorCodes('&', "&b&n" + s.getName())));
                }

                if (line.contains("{2}")) {
                    e.setLine(i, e.getLine(i).replace("{2}", ChatColor.translateAlternateColorCodes('&', "&f" + s.getPlayerCount())));
                }

                if (line.contains("{3}")) {
                    e.setLine(i, e.getLine(i).replace("{3}", ChatColor.translateAlternateColorCodes('&', "&f" + s.getMaxPlayerCount())));
                }

                if (line.contains("{4}")) {
                    if (s.getServerQueue().isWhitelisted()) {
                        e.setLine(i, e.getLine(i).replace("{4}", ChatColor.translateAlternateColorCodes('&', (s.isInQueue(e.getPlayer().getUniqueId()) ? "&cWhitelisted" : "&aClick to join"))));
                    } else if (s.getServerQueue().isPaused()) {
                        e.setLine(i, e.getLine(i).replace("{4}", ChatColor.translateAlternateColorCodes('&', (s.isInQueue(e.getPlayer().getUniqueId()) ? "&cPaused" : "&aClick to join"))));
                    } else {
                        e.setLine(i, e.getLine(i).replace("{4}", ChatColor.translateAlternateColorCodes('&', (s.isInQueue(e.getPlayer().getUniqueId()) ? "&aYour Position" : "&aClick to join"))));
                    }
                }

                if (line.contains("{5}")) {
                    e.setLine(i, e.getLine(i).replace("{5}", ChatColor.translateAlternateColorCodes('&', (s.isInQueue(e.getPlayer().getUniqueId()) ? "&e" + (PlayerUtility.findPosition(e.getPlayer().getUniqueId(), s.getServerQueue()) + 1) + " of " + (s.getServerQueue().getEntireQueue().size()) : "&athe " + (s.getServerQueue().isEnabled() ? "queue" : "server") + "!"))));
                }
            }

            if (serverManager.getServerFromQueueSignLocation(e.getLocation()) != null) {
                Server s = serverManager.getServerFromQueueSignLocation(e.getLocation());

                if (line.contains("{6}")) {
                    e.setLine(i, e.getLine(i).replace("{6}", ChatColor.translateAlternateColorCodes('&', "&b" + s.getServerQueue().getUltimateQueue().size())));
                }

                if (line.contains("{7}")) {
                    e.setLine(i, e.getLine(i).replace("{7}", ChatColor.translateAlternateColorCodes('&', "&b" + s.getServerQueue().getDonatorQueue().size())));
                }

                if (line.contains("{8}")) {
                    e.setLine(i, e.getLine(i).replace("{8}", ChatColor.translateAlternateColorCodes('&', "&b" + s.getServerQueue().getDefaultQueue().size())));
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (main.getConfig().getBoolean("allow-entities")) {
            e.setCancelled(true);
        }
    }
}
