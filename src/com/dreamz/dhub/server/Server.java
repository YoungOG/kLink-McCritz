package com.dreamz.dhub.server;

import com.dreamz.dhub.dHub;
import com.dreamz.dhub.utils.PlayerUtility;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
public class Server {

    private String name;
    private int playerCount;
    private int maxPlayerCount;
    private Material serverIcon;
    private int serverSlot;
    private ArrayList<Location> serverSignLocations;
    private ArrayList<Location> queueSignLocations;
    private ServerQueue serverQueue;

    public Server(String name, int maxPlayerCount, Material serverIcon, int serverSlot, boolean queueEnabled, ArrayList<Location> serverSignLocations, ArrayList<Location> queueSignLocations) {
        this.name = name;
        this.playerCount = 0;
        this.maxPlayerCount = maxPlayerCount;
        this.serverIcon = serverIcon;
        this.serverSlot = serverSlot;
        this.serverSignLocations = serverSignLocations;
        this.queueSignLocations = queueSignLocations;
        this.serverQueue = new ServerQueue(this);
        this.serverQueue.setEnabled(queueEnabled);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (PlayerUtility.getOnlinePlayers().length > 0) {
                    dHub.getInstance().getServerManager().getPlayerCount(PlayerUtility.getOnlinePlayers()[0], name);
                }

                for (Player all : PlayerUtility.getOnlinePlayers()) {
                    PlayerUtility.updateSigns(all, serverSignLocations);
                    PlayerUtility.updateSigns(all, queueSignLocations);
                }
            }
        }.runTaskTimerAsynchronously(dHub.getInstance(), 0L, 2*20L);
    }

    public boolean isInQueue(UUID id) {
        return serverQueue.getEntireQueue().contains(id);
    }
}
