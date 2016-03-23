package com.dreamz.dhub.utils;

import com.dreamz.dhub.dHub;
import com.dreamz.dhub.server.ServerQueue;
import de.blablubbabc.insigns.InSigns;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class PlayerUtility {

    public static Player[] getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    public static String getGroupColor(Player p) {
        String group = dHub.getInstance().getPermissions().getPrimaryGroup(p);

        if (group.equalsIgnoreCase("Ultimate")) {
            return "&2" + group;
        }

        if (group.equalsIgnoreCase("Master")) {
            return "&1" + group;
        }

        if (group.equalsIgnoreCase("PRO")) {
            return "&6" + group;
        }

        if (group.equalsIgnoreCase("MVP")) {
            return "&9" + group;
        }

        if (group.equalsIgnoreCase("VIP")) {
            return "&a" + group;
        }

        if (group.equalsIgnoreCase("YouTube+")) {
            return "&d" + group;
        }

        if (group.equalsIgnoreCase("YouTube")) {
            return "&d" + group;
        }

        return "&7" + group;
    }

    public static int findPosition(UUID id, ServerQueue queue) {
        int pos = 0;

        Queue<UUID> totalQueue = queue.getEntireQueue();

        for (UUID i : totalQueue) {
            if (i != id) {
                pos++;
            } else {
                return pos;
            }
        }

        return pos;
    }

    public static void updateSign(Player p, Location loc) {
        if (loc.getWorld().getBlockAt(loc) != null) {
            if (loc.getWorld().getBlockAt(loc).getState() instanceof Sign) {
                Sign s = (Sign) loc.getWorld().getBlockAt(loc).getState();

                if (s != null) {
                    if (p == null || !p.isOnline()) {
                        return;
                    }

                    if (!loc.getWorld().equals(p.getWorld())) {
                        return;
                    }

                    if (!loc.getChunk().isLoaded()) {
                        return;
                    }

                    if (loc.distanceSquared(p.getLocation()) > 1024.0) {
                        return;
                    }

                    if (s.getLines() == null || s.getLines().length == 0) {
                        return;
                    }

                    InSigns.sendSignChange(p, s);
                }
            }
        }
    }

    public static void updateSigns(Player p, List<Location> locs) {
        for (Location loc : locs) {
            if (loc.getWorld().getBlockAt(loc) != null) {
                if (loc.getWorld().getBlockAt(loc).getState() instanceof Sign) {
                    Sign s = (Sign) loc.getWorld().getBlockAt(loc).getState();

                    if (s != null) {
                        if (p == null || !p.isOnline()) {
                            return;
                        }

                        if (!loc.getWorld().equals(p.getWorld())) {
                            return;
                        }

                        if (!loc.getChunk().isLoaded()) {
                            return;
                        }

                        if (loc.distanceSquared(p.getLocation()) > 1024.0) {
                            return;
                        }

                        if (s.getLines() == null || s.getLines().length == 0) {
                            return;
                        }

                        InSigns.sendSignChange(p, s);
                    }
                }
            }
        }
    }
}
