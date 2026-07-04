package com.example.limbot;

import org.bukkit.plugin.java.JavaPlugin;

public class Limbobots extends JavaPlugin {
    
    private static Limbobots instance;
    private BotManager botManager;
    
    @Override
    public void onEnable() {
        instance = this;
        botManager = new BotManager(this);
        
        getCommand("limbobots").setExecutor(new BotCommand(this));
        getServer().getPluginManager().registerEvents(new BotListener(this), this);
        
        getLogger().info("Limbobots v" + getDescription().getVersion() + " успешно запущен!");
        getLogger().info("Автор: " + getDescription().getAuthors());
    }
    
    @Override
    public void onDisable() {
        if (botManager != null) {
            botManager.removeAllBots();
        }
        getLogger().info("Limbobots плагин выключен. Все боты удалены.");
    }
    
    public static Limbobots getInstance() {
        return instance;
    }
    
    public BotManager getBotManager() {
        return botManager;
    }
}
