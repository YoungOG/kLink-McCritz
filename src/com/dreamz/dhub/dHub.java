package com.dreamz.dhub;

import com.dreamz.dhub.commands.dHubCommand;
import com.dreamz.dhub.listeners.GadgetListeners;
import com.dreamz.dhub.listeners.GuiListeners;
import com.dreamz.dhub.listeners.PlayerListeners;
import com.dreamz.dhub.listeners.ServerListeners;
import com.dreamz.dhub.server.Server;
import com.dreamz.dhub.server.ServerManager;
import com.dreamz.dhub.utils.command.Register;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.MongoClient;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class dHub extends JavaPlugin implements PluginMessageListener {

    private static dHub instance;
    private DB database;
    private ServerManager serverManager;
    private Permission permissions;
    private List<UUID> inHubList = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveConfig();

        setupDatabase();
        setupPermissions();

        serverManager = new ServerManager();

        setupCommands();
        setupListeners();
    }

    @Override
    public void onDisable() {
        serverManager.saveServers();
        serverManager.saveSpawn();
    }

    public void setupDatabase() {
        try {
            database = MongoClient.connect(new DBAddress(getConfig().getString("database.host"), getConfig().getString("database.database-name")));
            this.getLogger().log(Level.INFO, "Sucessfully connected to MongoDB.");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            this.getLogger().log(Level.INFO, "Failed to connect to MongoDB.");
        }
    }

    public void setupCommands() {
        try {
            Register register = new Register();
            register.registerCommand("dhub", new dHubCommand());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new GadgetListeners(), this);
        getServer().getPluginManager().registerEvents(new GuiListeners(), this);
        getServer().getPluginManager().registerEvents(new ServerListeners(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permissions = rsp.getProvider();
        return permissions != null;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            String subchannel = in.readUTF();

            if (subchannel.equals("PlayerCount")) {
                String server = in.readUTF();
                int playerCount = in.readInt();

                for (Server s : serverManager.getServers()) {
                    if (s.getName().equalsIgnoreCase(server)) {
                        s.setPlayerCount(playerCount);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static dHub getInstance() {
        return instance;
    }

    public DB getDB() {
        return database;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public List<UUID> getInHubList() {
        return inHubList;
    }
}
