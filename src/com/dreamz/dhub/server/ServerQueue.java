package com.dreamz.dhub.server;

import com.dreamz.dhub.dHub;
import com.dreamz.dhub.utils.Cooldowns;
import com.dreamz.dhub.utils.MessageManager;
import com.dreamz.dhub.utils.PlayerUtility;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

@Getter
@Setter
public class ServerQueue {

    private Server server;
    private LinkedList<UUID> ultimateQueue = new LinkedList<>();
    private LinkedList<UUID> donatorQueue = new LinkedList<>();
    private LinkedList<UUID> defaultQueue = new LinkedList<>();
    private boolean enabled;
    private boolean paused;
    private boolean whitelisted;

    public ServerQueue(Server server) {
        this.server = server;
        this.enabled = true;
        this.paused = false;
        this.whitelisted = false;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (isPaused()) {
                    getEntireQueue().stream().filter(id -> Cooldowns.tryCooldown(id, "paused-cooldown", 10000)).forEach(id -> {
                        MessageManager.sendMessage(id, "&4&m-----------------------------------------------------");
                        MessageManager.sendMessage(id, "&r");
                        MessageManager.sendMessage(id, "&r &r&cThe server is currently paused.");
                        MessageManager.sendMessage(id, "&r &r&cQueuing will resume shortly!");
                        MessageManager.sendMessage(id, "&r");
                        MessageManager.sendMessage(id, "&4&m-----------------------------------------------------");
                    });
                } else if (!whitelisted && enabled) {
                    ArrayList<UUID> joinList = new ArrayList<>();
                    LinkedList<UUID> queue = getEntireQueue();

                    if (queue.size() > 0) {
                        for (int count = 0; count < 3; count++) {
                            if (peek() != null) {
                                if (defaultQueue.contains(peek()) && server.getPlayerCount() >= server.getMaxPlayerCount()) {
                                    continue;
                                }

                                System.out.println("Polling: " + peek() + ", Count: " + count);

                                joinList.add(poll());
                            }
                        }

                        if (joinList.size() > 0) {
                            System.out.println(joinList);

                            for (UUID id : joinList) {
                                dHub.getInstance().getServerManager().removeFromServerQueue(Bukkit.getPlayer(id), server);
                                dHub.getInstance().getServerManager().getPlayerCount(Bukkit.getPlayer(id), server.getName());
                                dHub.getInstance().getServerManager().connectToServer(Bukkit.getPlayer(id), server.getName());
                                MessageManager.sendMessage(id, "&6Connecting you to " + server.getName() + "...");

                                for (Player all : PlayerUtility.getOnlinePlayers()) {
                                    PlayerUtility.updateSigns(all, server.getServerSignLocations());
                                    PlayerUtility.updateSigns(all, server.getQueueSignLocations());
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(dHub.getInstance(), 0L, 2 * 20L);
    }

    public LinkedList<UUID> getEntireQueue() {
        LinkedList<UUID> queue = new LinkedList<>();
        queue.addAll(ultimateQueue);
        queue.addAll(donatorQueue);
        queue.addAll(defaultQueue);

        return queue;
    }

    public UUID poll() {
        if (ultimateQueue.size() > 0) {
            return ultimateQueue.poll();
        } else if (donatorQueue.size() > 0) {
            return donatorQueue.poll();
        } else {
            return defaultQueue.poll();
        }
    }

    public UUID peek() {
        if (ultimateQueue.size() > 0) {
            return ultimateQueue.peek();
        } else if (donatorQueue.size() > 0) {
            return donatorQueue.peek();
        } else {
            return defaultQueue.peek();
        }
    }
}
