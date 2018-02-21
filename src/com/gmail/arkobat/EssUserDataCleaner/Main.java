package com.gmail.arkobat.EssUserDataCleaner;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public class Main extends JavaPlugin{

    public String noPermMsg;
    public List<String> cantContain;
    public List<String> mustContain;
    public int maxSize;
    public String when;
    public int delay;
    public boolean compareUsermap;
    public String todo;
    public boolean inform;
    public long lastModified;
    public boolean consoleOnly;
    public boolean debug = false;

    FileHandler fileHandler = new FileHandler(this);

    @Override
    public void onEnable() {
        getCommand("essclean").setExecutor(new CommandHandler(this, fileHandler));
        saveDefaultConfig();
        if (loadDefaultConfig()) {
            Bukkit.getServer().getLogger().info("EssUserDataCleaner Enabled");
        } else {
            getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin("EssUserDataCleaner"));
        }
        fileHandler.source("start");
    }

    @Override
    public void onDisable() {
        fileHandler.source("stop");
        Bukkit.getServer().getLogger().info("EssUserDataCleaner Disabled");
    }

    public boolean loadDefaultConfig() {
        noPermMsg = getConfig().getString("noPermMsg");
        maxSize = getConfig().getInt("maxSize");
        cantContain = getConfig().getStringList("cantContain");
        mustContain = getConfig().getStringList("mustContain");
        when = getConfig().getString("when");
        delay = getConfig().getInt("delay");
        compareUsermap = getConfig().getBoolean("compareUsermap");
        todo = getConfig().getString("todo");
        inform = getConfig().getBoolean("inform");
        lastModified = getConfig().getInt("lastModified");
        consoleOnly = getConfig().getBoolean("consoleOnly");
        debug = false;
        return true;
    }

}
