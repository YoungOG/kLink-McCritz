package com.dreamz.dhub.server;

import com.dreamz.dhub.dHub;
import com.dreamz.dhub.utils.ItemBuilder;
import com.dreamz.dhub.utils.LocationSerialization;
import com.dreamz.dhub.utils.MessageManager;
import com.dreamz.dhub.utils.PlayerUtility;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerManager {

    private dHub main = dHub.getInstance();
    private DBCollection mCollection = main.getDB().getCollection("main");
    private DBCollection sCollection = main.getDB().getCollection("servers");
    private ArrayList<Server> servers = new ArrayList<>();
    private Location spawnLocation;

    public ServerManager() {
        loadSpawn();
        loadServers();

        new BukkitRunnable() {
            @Override
            public void run() {
                saveSpawn();
                saveServers();
            }
        }.runTaskTimer(main, 0L, 300*20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player all : PlayerUtility.getOnlinePlayers()) {
                    if (all.getOpenInventory().getTopInventory() != null) {
                        Inventory inv = all.getOpenInventory().getTopInventory();

                        if (inv.getTitle().contains("Server Selector")) {
                            for (ItemStack item : inv.getContents()) {
                                if (item != null) {
                                    Server s = getServerFromItem(item);

                                    if (s != null) {
                                        ItemMeta im = item.getItemMeta();
                                        List<String> lore = im.getLore();
                                        lore.clear();
                                        lore.add(ChatColor.translateAlternateColorCodes('&', "&9" + s.getPlayerCount() + "/" + s.getMaxPlayerCount()));

                                        if (s.isInQueue(all.getUniqueId())) {
                                            lore.add(" ");
                                            lore.add(ChatColor.translateAlternateColorCodes('&', "&aYour Position&7: &e" + (PlayerUtility.findPosition(all.getUniqueId(), s.getServerQueue()) + 1) + " &aof &e" + (s.getServerQueue().getEntireQueue().size())));
                                        }

                                        im.setLore(lore);
                                        item.setItemMeta(im);
                                    } else {
                                        inv.remove(item);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(main, 0L, 20L);
    }

    public void loadServers() {
        System.out.println("Loading " + sCollection.count() + " servers");

        DBCursor dbc = sCollection.find();

        while (dbc.hasNext()) {
            BasicDBObject dbo = (BasicDBObject) dbc.next();

            String name = dbo.getString("name");
            int maxPlayerCount = dbo.getInt("maxPlayerCount");
            ArrayList<Location> serverSignLocations = new ArrayList<>();
            ArrayList<Location> queueSignLocations = new ArrayList<>();
            Material serverIcon = Material.valueOf(dbo.getString("serverIcon"));
            int serverSlot = dbo.getInt("serverSlot");
            boolean queueEnabled = dbo.getBoolean("queueEnabled");

            BasicDBList obj1 = (BasicDBList) dbo.get("serverSignLocations");
            if (obj1 != null) {
                for (Object str : obj1) {
                    serverSignLocations.add(LocationSerialization.deserializeLocation((String) str));
                }
            }

            BasicDBList obj2 = (BasicDBList) dbo.get("queueSignLocations");
            if (obj2 != null) {
                for (Object str : obj2) {
                    queueSignLocations.add(LocationSerialization.deserializeLocation((String) str));
                }
            }

            Server server = new Server(name, maxPlayerCount, serverIcon, serverSlot, queueEnabled, serverSignLocations, queueSignLocations);
            servers.add(server);

            System.out.println("Server: " + server.getName());
            System.out.println("  Max Player Count: " + server.getMaxPlayerCount());
            System.out.println("  Server Icon: " + server.getServerIcon());
            System.out.println("  Server Slot: " + server.getServerSlot());
            System.out.println("  Queue Enabled: " + server.getServerQueue().isEnabled());
            System.out.println("  Server Sign Locations (" + server.getServerSignLocations().size() + "):");
            for (Location loc : server.getServerSignLocations()) {
                System.out.println("    - " + loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
            }
            System.out.println("  Queue Sign Locations (" + server.getQueueSignLocations().size() + "):");
            for (Location loc : server.getQueueSignLocations()) {
                System.out.println("    - " + loc.getWorld() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
            }
        }

        System.out.println("Loaded " + servers.size() + " servers");
    }

    public void saveServers() {
        System.out.println("Saving " + servers.size() + " servers");

        for (Server s : servers) {
            BasicDBObject query = new BasicDBObject("name", s.getName());
            BasicDBObject dbo = new BasicDBObject("name", s.getName());
            dbo.append("maxPlayerCount", s.getMaxPlayerCount());
            dbo.append("serverIcon", s.getServerIcon().toString());
            dbo.append("serverSlot", s.getServerSlot());
            dbo.append("queueEnabled", s.getServerQueue().isEnabled());

            List<String> serverSignList = new ArrayList<>();
            for (Location loc : s.getServerSignLocations()) {
                serverSignList.add(LocationSerialization.serializeLocation(loc));
            }
            List<String> queueSignList = new ArrayList<>();
            for (Location loc : s.getQueueSignLocations()) {
                queueSignList.add(LocationSerialization.serializeLocation(loc));
            }

            dbo.append("serverSignLocations", serverSignList);
            dbo.append("queueSignLocations", queueSignList);

            if (sCollection.find(query).hasNext()) {
                sCollection.update(query, dbo);
            } else {
                sCollection.insert(dbo);
            }
        }

        System.out.println("Saved " + servers.size() + " servers");
    }

    public void loadSpawn() {
        DBCursor dbc = mCollection.find(new BasicDBObject("spawn", "searchBy"));

        if (dbc.hasNext()) {
            BasicDBObject dbo = (BasicDBObject) dbc.next();

            if (dbo.getString("spawnLocation") != null) {
                spawnLocation = LocationSerialization.deserializeLocation(dbo.getString("spawnLocation"));
                System.out.println("Spawn Loaded: " + spawnLocation);
            } else {
                System.out.println("Failed to load spawn. (MongoDB document found)");
            }
        } else {
            System.out.println("Failed to load spawn. (Not Saved)");
        }
    }

    public void saveSpawn() {
        if (spawnLocation != null) {
            DBCursor query = mCollection.find(new BasicDBObject("spawn", "searchBy"));
            BasicDBObject dbo = new BasicDBObject("spawn", "searchBy");
            dbo.append("spawnLocation", LocationSerialization.serializeLocation(spawnLocation));

            if (query.hasNext()) {
                mCollection.update(query.next(), dbo);
            } else {
                mCollection.insert(dbo);
            }

            System.out.println("Spawn Location (X: " + spawnLocation.getBlockX() + " Y: " + spawnLocation.getBlockY() + " Z: " + spawnLocation.getBlockZ() + ") saved!");
        }
    }

    public void showServerSelectionInventory(Player p) {
        Inventory serverSelectionInv = Bukkit.createInventory(p, 9, ChatColor.RED + "Server Selector");

        for (Server s : servers) {
            if (s.getServerIcon() != null && (s.getServerSlot() > -1)) {
                serverSelectionInv.setItem(s.getServerSlot(), new ItemBuilder(s.getServerIcon()).name("&b" + s.getName()).addLore("&9" + s.getPlayerCount() + "/" + s.getMaxPlayerCount()).build());
            }
        }

        for (ItemStack item : serverSelectionInv.getContents()) {
            if (item != null) {
                Server s = getServerFromItem(item);

                if (s != null) {
                    ItemMeta im = item.getItemMeta();
                    List<String> lore = im.getLore();
                    lore.clear();
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&9" + s.getPlayerCount() + "/" + s.getMaxPlayerCount()));
                    im.setLore(lore);
                    item.setItemMeta(im);
                } else {
                    serverSelectionInv.remove(item);
                }
            }
        }

        p.openInventory(serverSelectionInv);
    }

    public void reloadInventories() {
        for (Player all : PlayerUtility.getOnlinePlayers()) {
            if (all.getOpenInventory().getTopInventory() != null) {
                Inventory inv = all.getOpenInventory().getTopInventory();

                if (inv.getTitle().contains("Server Selector")) {
                    inv.clear();

                    for (Server s : servers) {
                        if (s.getServerIcon() != null && (s.getServerSlot() > -1)) {
                            inv.setItem(s.getServerSlot(), new ItemBuilder(s.getServerIcon()).name("&b" + s.getName()).addLore("&9" + s.getPlayerCount() + "/" + s.getMaxPlayerCount()).build());
                        }
                    }
                }
            }
        }
    }

    public void createServer(CommandSender sender, String name, int maxPlayerCount, Material serverIcon, int serverSlot) {
        Server server = new Server(name, maxPlayerCount, serverIcon, serverSlot, true, new ArrayList<>(), new ArrayList<>());
        servers.add(server);

        reloadInventories();

        MessageManager.sendMessage(sender, "&aServer &b" + name + " &ahas been created.");
    }

    public void removeServer(CommandSender sender, Server s) {
        BasicDBObject dbo = new BasicDBObject("name", s.getName());

        DBCursor dbc = sCollection.find(dbo);

        while (dbc.hasNext()) {
            sCollection.remove(dbc.next());
        }

        servers.remove(s);

        for (Location loc : s.getServerSignLocations()) {
            loc.getBlock().breakNaturally(new ItemStack(Material.AIR));
        }

        for (Location loc : s.getQueueSignLocations()) {
            loc.getBlock().breakNaturally(new ItemStack(Material.AIR));
        }

        MessageManager.sendMessage(sender, "&aServer &b" + s.getName() + " &aand its signs have been removed.");
    }

    public void addToServerQueue(Player p, Server server) {
        if (p.hasPermission("hub.staff")) {
            connectToServer(p, server.getName());
            MessageManager.sendMessage(p.getUniqueId(), "&6Connecting you to " + server.getName() + "...");
            return;
        }

        if (!server.getServerQueue().isEnabled()) {
            connectToServer(p, server.getName());
            MessageManager.sendMessage(p.getUniqueId(), "&6Connecting you to " + server.getName() + "...");
            return;
        }

        String groupName = main.getPermissions().getPrimaryGroup(p);

        if (groupName.equalsIgnoreCase("ultimate") || groupName.equalsIgnoreCase("master")) {
            if (!server.getServerQueue().getUltimateQueue().contains(p.getUniqueId())) {
                server.getServerQueue().getUltimateQueue().offer(p.getUniqueId());
                server.getServerQueue().getDefaultQueue().remove(p.getUniqueId());
                server.getServerQueue().getDonatorQueue().remove(p.getUniqueId());

                MessageManager.sendMessage(p, "&3You are currently position &b#" + (PlayerUtility.findPosition(p.getUniqueId(), server.getServerQueue()) + 1) + " &3in the &b" + server.getName() + " &3queue.");
                MessageManager.sendMessage(p, "&bYour " + PlayerUtility.getGroupColor(p) + " &brank has queued you in front of other players!");
                p.playNote(p.getLocation(), Instrument.PIANO, Note.sharp(1, Note.Tone.A));
            } else {
                MessageManager.sendMessage(p, "&cYou are already queued for this server.");
            }
        } else if (groupName.equalsIgnoreCase("vip") || groupName.equalsIgnoreCase("mvp") || groupName.equalsIgnoreCase("pro") || groupName.equalsIgnoreCase("youtube") || groupName.equalsIgnoreCase("youtube+")) {
            if (!server.getServerQueue().getDonatorQueue().contains(p.getUniqueId())) {
                server.getServerQueue().getDonatorQueue().offer(p.getUniqueId());
                server.getServerQueue().getUltimateQueue().remove(p.getUniqueId());
                server.getServerQueue().getDefaultQueue().remove(p.getUniqueId());

                MessageManager.sendMessage(p, "&3You are currently position &b#" + (PlayerUtility.findPosition(p.getUniqueId(), server.getServerQueue()) + 1) + " &3in the &b" + server.getName() + " &3queue.");
                MessageManager.sendMessage(p, "&bYour " + PlayerUtility.getGroupColor(p) + " &brank has queued you in front of other players!");
                p.playNote(p.getLocation(), Instrument.PIANO, Note.sharp(1, Note.Tone.A));
            } else {
                MessageManager.sendMessage(p, "&cYou are already queued for this server.");
            }
        } else {
            if (!server.getServerQueue().getDefaultQueue().contains(p.getUniqueId())) {
                server.getServerQueue().getDefaultQueue().offer(p.getUniqueId());
                server.getServerQueue().getDonatorQueue().remove(p.getUniqueId());
                server.getServerQueue().getDonatorQueue().remove(p.getUniqueId());

                MessageManager.sendMessage(p, "&3You are currently position &b#" + (PlayerUtility.findPosition(p.getUniqueId(), server.getServerQueue()) + 1) + " &3in the &b" + server.getName() + " &3queue.");
            } else {
                MessageManager.sendMessage(p, "&cYou are already queued for this server.");
            }
        }

        PlayerUtility.updateSigns(p, server.getServerSignLocations());
        PlayerUtility.updateSigns(p, server.getQueueSignLocations());
    }

    public void removeFromServerQueue(Player p, Server server) {
        if (server.getServerQueue().getUltimateQueue().contains(p.getUniqueId())) {
            server.getServerQueue().getUltimateQueue().remove(p.getUniqueId());
        }
        if (server.getServerQueue().getDonatorQueue().contains(p.getUniqueId())) {
            server.getServerQueue().getDonatorQueue().remove(p.getUniqueId());
        }
        if (server.getServerQueue().getDefaultQueue().contains(p.getUniqueId())) {
            server.getServerQueue().getDefaultQueue().remove(p.getUniqueId());
        }

        PlayerUtility.updateSigns(p, server.getServerSignLocations());
        PlayerUtility.updateSigns(p, server.getQueueSignLocations());
    }

    public void connectToServer(Player p, String channel) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("Connect");
            out.writeUTF(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        p.sendPluginMessage(dHub.getInstance(), "BungeeCord", b.toByteArray());
    }

    public void getPlayerCount(Player player, String server) {
        if (server == null) {
            server = "ALL";
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF(server);

        player.sendPluginMessage(main, "BungeeCord", out.toByteArray());
    }

    public Server getServer(String name) {
        for (Server s : servers) {
            if (s.getName().equalsIgnoreCase(name.replace("_", " "))) {
                return s;
            }
        }

        return null;
    }

    public Server getServerFromServerSignLocation(Location location) {
        for (Server servers : getServers()) {
            if (servers.getServerSignLocations().contains(location)) {
                return servers;
            }
        }

        return null;
    }

    public Server getServerFromQueueSignLocation(Location location) {
        for (Server servers : getServers()) {
            if (servers.getQueueSignLocations().contains(location)) {
                return servers;
            }
        }

        return null;
    }

    public Server getServerFromItem(ItemStack item) {
        String name = item.getItemMeta().getDisplayName();

        for (Server s : getServers()) {
            if (s.getName().equalsIgnoreCase(ChatColor.stripColor(name))) {
                return s;
            }
        }

        return null;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public ArrayList<Server> getServers() {
        return servers;
    }
}
