package com.dreamz.dhub.commands;

import com.dreamz.dhub.dHub;
import com.dreamz.dhub.server.Server;
import com.dreamz.dhub.server.ServerManager;
import com.dreamz.dhub.utils.ItemBuilder;
import com.dreamz.dhub.utils.MessageManager;
import com.dreamz.dhub.utils.command.BaseCommand;
import com.dreamz.dhub.utils.command.CommandUsageBy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.UUID;

public class dHubCommand extends BaseCommand {

    private dHub main = dHub.getInstance();
    private ServerManager serverManager = main.getServerManager();

    public dHubCommand() {
        super("dhub", "hub.staff", CommandUsageBy.ANYONE, "serv");
        setUsage("&cImproper usage! /dhub help");
        setMinArgs(0);
        setMaxArgs(5);
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageManager.sendMessage(sender, "&7&m----------&r&7[ &bServer Command &7]&m----------");
            MessageManager.sendMessage(sender, "&r &r&3/dhub &7- Displays this page.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub setspawn &7- Sets the server spawn.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub create &7(&bname&7) (&bslots&7) (&bmaterial&7) (&bslot&7) - Creates a server.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub remove &7(&bserver&7) - Removes a server and its signs.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub icon &7(&bserver&7) (&bmaterial&7) - Set a servers GUI icon.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub slot &7(&bserver&7) (&bslot&7) - Set a servers GUI slot.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub maxslots &7(&bserver&7) (&bcount&7) - Set a servers max slots.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub list &7- Lists servers available.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub show &7(&bserver&7) - Displays information about a server.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub queue &7(&bserver&7) - Toggle a servers queue on/off.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub pause &7(&bserver&7) - Pause the queue of a server.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub whitelist &7(&bserver&7) - Toggle a servers whitelist on/off.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub reloadgui &7- Reloads the server selection GUI.");
            MessageManager.sendMessage(sender, "&r &r&3/dhub toggle &7(&bplayer&7) - Toggles \"inHub\" status for player.");
            MessageManager.sendMessage(sender, "&7&m------------------------------------");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("setspawn")) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;

                    serverManager.setSpawnLocation(p.getLocation());

                    MessageManager.sendMessage(p, "&aSpawn location set at X: &e" + p.getLocation().getBlockX() + " &bY: &e" + p.getLocation().getBlockY() + " &bZ: &e" + p.getLocation().getBlockZ());
                } else {
                    MessageManager.sendMessage(sender, "&cOnly players can set the spawn.");
                }
            } else if (args[0].equalsIgnoreCase("reloadgui")) {
                serverManager.reloadInventories();

                MessageManager.sendMessage(sender, "&aServer Selection GUI reloaded!");
            } else if (args[0].equalsIgnoreCase("list")) {
                if (serverManager.getServers().size() < 1) {
                    MessageManager.sendMessage(sender, "&cThere are no servers available.");
                    return;
                }

                for (Server s : serverManager.getServers()) {
                    MessageManager.sendMessage(sender, "&aServer&7: &b" + s.getName());
                    MessageManager.sendMessage(sender, "&aMax Player Count&7: &b" + s.getMaxPlayerCount());
                    MessageManager.sendMessage(sender, "&aServer Icon&7: &b" + s.getServerIcon());
                    MessageManager.sendMessage(sender, "&aServer Slot&7: &b" + s.getServerSlot());
                    MessageManager.sendMessage(sender, "&aQueue&7: &b" + s.getServerQueue().isEnabled());

                    if (s.getServerQueue().isEnabled()) {
                        MessageManager.sendMessage(sender, "&r &r &aPaused&7: &b" + s.getServerQueue().isPaused());
                        MessageManager.sendMessage(sender, "&r &r &aWhitelisted&7: &b" + s.getServerQueue().isWhitelisted());
                        MessageManager.sendMessage(sender, "&r &r &aUltimate Queue&7: &b" + s.getServerQueue().getUltimateQueue().size());
                        MessageManager.sendMessage(sender, "&r &r &aDonator Queue&7: &b" + s.getServerQueue().getDonatorQueue().size());
                        MessageManager.sendMessage(sender, "&r &r &aDefault Queue&7: &b" + s.getServerQueue().getDefaultQueue().size());
                    }

                    MessageManager.sendMessage(sender, "&aServer Sign Locations &7(&b" + s.getServerSignLocations().size() + "&7):");

                    for (Location loc : s.getServerSignLocations()) {
                        MessageManager.sendMessage(sender, "  &7- &b" + loc.getWorld().getName() + "&7, &aX&7: &b" + loc.getBlockX() + "&7, &aY&7: &b" + loc.getBlockY() + "&7, &aZ&7: &b" + loc.getBlockZ());
                    }

                    MessageManager.sendMessage(sender, "&aQueue Sign Locations &7(&b" + s.getQueueSignLocations().size() + "&7):");

                    for (Location loc : s.getQueueSignLocations()) {
                        MessageManager.sendMessage(sender, "  &7- &b" + loc.getWorld().getName() + "&7, &aX&7: &b" + loc.getBlockX() + "&7, &aY&7: &b" + loc.getBlockY() + "&7, &aZ&7: &b" + loc.getBlockZ());
                    }
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                MessageManager.sendMessage(sender, "&7&m----------&r&7[ &bServer Command &7]&m----------");
                MessageManager.sendMessage(sender, "&r &r&3/dhub &7- Displays this page.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub setspawn &7- Sets the server spawn.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub create &7(&bname&7) (&bslots&7) (&bmaterial&7) (&bslot&7) - Creates a server.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub remove &7(&bserver&7) - Removes a server and its signs.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub icon &7(&bserver&7) (&bmaterial&7) - Set a servers GUI icon.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub slot &7(&bserver&7) (&bslot&7) - Set a servers GUI slot.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub maxslots &7(&bserver&7) (&bcount&7) - Set a servers max slots.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub list &7- Lists servers available.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub show &7(&bserver&7) - Displays information about a server.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub queue &7(&bserver&7) - Toggle a servers queue on/off.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub pause &7(&bserver&7) - Pause the queue of a server.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub whitelist &7(&bserver&7) - Toggle a servers whitelist on/off.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub reloadgui &7- Reloads the server selection GUI.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub toggle &7(&bplayer&7) - Toggles \"inHub\" status for player.");
                MessageManager.sendMessage(sender, "&r &r&3/dhub toggle &7(&bplayer&7) (&bon&7/&boff&7) - Toggles \"inHub\" status for player.");
                MessageManager.sendMessage(sender, "&7&m------------------------------------");
            } else {
                MessageManager.sendMessage(sender, getUsage());
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("toggle")) {
                Player target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                if (main.getInHubList().contains(target.getUniqueId())) {
                    main.getInHubList().remove(target.getUniqueId());

                    target.getInventory().clear();
                    target.getInventory().setItem(0, new ItemBuilder(Material.INK_SACK).durability(10).name("&bPlayer Visibility").build());
                    target.getInventory().setItem(4, new ItemBuilder(Material.COMPASS).name("&cServer Teleporter").build());
                    target.getInventory().setItem(8, new ItemBuilder(Material.FIREWORK).name("&6Jump Stick").build());
                    target.getInventory().setHeldItemSlot(4);
                    target.updateInventory();
                } else {
                    main.getInHubList().add(target.getUniqueId());

                    target.getInventory().clear();
                    target.updateInventory();
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                serverManager.removeServer(sender, server);
            } else if (args[0].equalsIgnoreCase("show")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                MessageManager.sendMessage(sender, "&aServer&7: &b" + server.getName());
                MessageManager.sendMessage(sender, "&aMax Player Count&7: &b" + server.getMaxPlayerCount());
                MessageManager.sendMessage(sender, "&aServer Icon&7: &b" + server.getServerIcon());
                MessageManager.sendMessage(sender, "&aServer Slot&7: &b" + server.getServerSlot());
                MessageManager.sendMessage(sender, "&aQueue&7: &b" + server.getServerQueue().isEnabled());

                if (server.getServerQueue().isEnabled()) {
                    MessageManager.sendMessage(sender, "&r &r &aPaused&7: &b" + server.getServerQueue().isPaused());
                    MessageManager.sendMessage(sender, "&r &r &aWhitelisted&7: &b" + server.getServerQueue().isWhitelisted());
                    MessageManager.sendMessage(sender, "&r &r &aUltimate Queue&7: &b" + server.getServerQueue().getUltimateQueue().size());
                    MessageManager.sendMessage(sender, "&r &r &aDonator Queue&7: &b" + server.getServerQueue().getDonatorQueue().size());
                    MessageManager.sendMessage(sender, "&r &r &aDefault Queue&7: &b" + server.getServerQueue().getDefaultQueue().size());
                }

                MessageManager.sendMessage(sender, "&aServer Sign Locations &7(&b" + server.getServerSignLocations().size() + "&7):");

                for (Location loc : server.getServerSignLocations()) {
                    MessageManager.sendMessage(sender, "  &7- &b" + loc.getWorld().getName() + "&7, &aX&7: &b" + loc.getBlockX() + "&7, &aY&7: &b" + loc.getBlockY() + "&7, &aZ&7: &b" + loc.getBlockZ());
                }

                MessageManager.sendMessage(sender, "&aQueue Sign Locations &7(&b" + server.getQueueSignLocations().size() + "&7):");

                for (Location loc : server.getQueueSignLocations()) {
                    MessageManager.sendMessage(sender, "  &7- &b" + loc.getWorld().getName() + "&7, &aX&7: &b" + loc.getBlockX() + "&7, &aY&7: &b" + loc.getBlockY() + "&7, &aZ&7: &b" + loc.getBlockZ());
                }
            } else if (args[0].equalsIgnoreCase("whitelist")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                if (!server.getServerQueue().isWhitelisted()) {
                    server.getServerQueue().setWhitelisted(true);
                    MessageManager.sendMessage(sender, "&aYou have turned on whitelisting for &b" + server.getName());
                } else {
                    server.getServerQueue().setWhitelisted(false);
                    MessageManager.sendMessage(sender, "&aYou have turned off whitelisting for &b" + server.getName());
                }
            } else if (args[0].equalsIgnoreCase("pause")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                if (!server.getServerQueue().isPaused()) {
                    server.getServerQueue().setPaused(true);
                    MessageManager.sendMessage(sender, "&aYou have paused the server for &b" + server.getName());
                } else {
                    server.getServerQueue().setPaused(false);
                    MessageManager.sendMessage(sender, "&aYou have resumed the server for &b" + server.getName());

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            LinkedList<UUID> queue = server.getServerQueue().getEntireQueue();

                            for (UUID id : queue) {
                                MessageManager.sendMessage(id, "&2&m-----------------------------------------------------");
                                MessageManager.sendMessage(id, "&r");
                                MessageManager.sendMessage(id, "&r &r&aThe server has been unpaused.");
                                MessageManager.sendMessage(id, "&r &r&aQueuing will now resume!");
                                MessageManager.sendMessage(id, "&r");
                                MessageManager.sendMessage(id, "&2&m-----------------------------------------------------");
                            }
                        }
                    }.runTaskAsynchronously(main);
                }
            } else if (args[0].equalsIgnoreCase("queue")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                if (server.getServerQueue().isEnabled()) {
                    server.getServerQueue().setEnabled(false);
                    MessageManager.sendMessage(sender, "&aYou have disabled the queue for &b" + server.getName());
                } else {
                    server.getServerQueue().setEnabled(true);
                    MessageManager.sendMessage(sender, "&aYou have enabled the queue for &b" + server.getName());
                }
            } else {
                MessageManager.sendMessage(sender, getUsage());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("toggle")) {
                Player target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                if (args[2].equalsIgnoreCase("off")) {
                    main.getInHubList().remove(target.getUniqueId());

                    target.teleport(serverManager.getSpawnLocation());

                    target.getInventory().clear();
                    target.getInventory().setItem(0, new ItemBuilder(Material.INK_SACK).durability(10).name("&bPlayer Visibility").build());
                    target.getInventory().setItem(4, new ItemBuilder(Material.COMPASS).name("&cServer Teleporter").build());
                    target.getInventory().setItem(8, new ItemBuilder(Material.FIREWORK).name("&6Jump Stick").build());
                    target.getInventory().setHeldItemSlot(4);
                    target.updateInventory();
                } else if (args[2].equalsIgnoreCase("on")){
                    main.getInHubList().add(target.getUniqueId());
                    target.getInventory().clear();
                    target.updateInventory();
                } else {
                    main.getInHubList().remove(target.getUniqueId());

                    target.teleport(serverManager.getSpawnLocation());

                    target.getInventory().clear();
                    target.getInventory().setItem(0, new ItemBuilder(Material.INK_SACK).durability(10).name("&bPlayer Visibility").build());
                    target.getInventory().setItem(4, new ItemBuilder(Material.COMPASS).name("&cServer Teleporter").build());
                    target.getInventory().setItem(8, new ItemBuilder(Material.FIREWORK).name("&6Jump Stick").build());
                    target.getInventory().setHeldItemSlot(4);
                    target.updateInventory();

                    System.out.println("Invalid string entered for Toggle. Setting to false.");
                }
            } else if (args[0].equalsIgnoreCase("maxslots")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                try {
                    int maxSlots = Integer.parseInt(args[2]);

                    server.setMaxPlayerCount(maxSlots);
                    MessageManager.sendMessage(sender, "&aServer &b" + server.getName() + " &amax player count has been set to &e" + server.getMaxPlayerCount() + "&a.");
                } catch (NumberFormatException e) {
                    MessageManager.sendMessage(sender, "&cYou must enter a valid number.");
                }
            } else if (args[0].equalsIgnoreCase("icon")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                if (Material.getMaterial(args[2]) == null) {
                    MessageManager.sendMessage(sender, "&cThat is not a valid material.");
                    return;
                }

                server.setServerIcon(Material.getMaterial(args[2]));

                serverManager.reloadInventories();

                MessageManager.sendMessage(sender, "&aServer &b" + server.getName() + " &aserver icon has been set to &e" + server.getServerIcon() + "&a.");
            } else if (args[0].equalsIgnoreCase("slot")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                try {
                    server.setServerSlot(Integer.parseInt(args[2]));

                    serverManager.reloadInventories();

                    MessageManager.sendMessage(sender, "&aServer &b" + server.getName() + " &aserver slot has been set to &e" + server.getServerSlot() + "&a.");
                } catch (NumberFormatException e) {
                    MessageManager.sendMessage(sender, "&cYou must enter a valid number.");
                }
            } else {
                MessageManager.sendMessage(sender, getUsage());
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("create")) {
                try {
                    if (Material.getMaterial(args[3]) == null) {
                        MessageManager.sendMessage(sender, "&cYou must enter a valid material.");
                        return;
                    }

                    serverManager.createServer(sender, args[1].replace("_", " "), Integer.parseInt(args[2]), Material.getMaterial(args[3]), Integer.parseInt(args[4]));
                } catch (NumberFormatException e) {
                    MessageManager.sendMessage(sender, "&cYou must enter a valid number.");
                }
            }
        } else {
            MessageManager.sendMessage(sender, getUsage());
        }
    }
}




