package com.example.limbot;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class BotListener implements Listener {
    
    private final Limbobots plugin;
    
    public BotListener(Limbobots plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer nmsPlayer = craftPlayer.getHandle();
        
        // Показываем всех ботов новому игроку
        for (EntityPlayer bot : plugin.getBotManager().getBots()) {
            try {
                // Добавляем бота в таб
                PacketPlayOutPlayerInfo packetInfo = new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                    bot
                );
                nmsPlayer.playerConnection.sendPacket(packetInfo);
                
                // Спавним бота
                PacketPlayOutNamedEntitySpawn packetSpawn = new PacketPlayOutNamedEntitySpawn(bot);
                nmsPlayer.playerConnection.sendPacket(packetSpawn);
                
                // Синхронизируем экипировку
                PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment(
                    bot.getId(), 
                    bot.getEquipment()
                );
                nmsPlayer.playerConnection.sendPacket(equipmentPacket);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при отображении бота для игрока " + 
                    player.getName() + ": " + e.getMessage());
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Боты продолжают существовать после выхода игроков
        // Никаких дополнительных действий не требуется
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // Проверяем, не кликнул ли игрок по боту
        if (event.getRightClicked() instanceof CraftPlayer) {
            CraftPlayer clicked = (CraftPlayer) event.getRightClicked();
            
            // Проверяем, является ли сущность ботом
            for (EntityPlayer bot : plugin.getBotManager().getBots()) {
                if (bot.getId() == clicked.getEntityId()) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§7Это бот Limbobots: §f" + bot.getName());
                    break;
                }
            }
        }
    }
}
