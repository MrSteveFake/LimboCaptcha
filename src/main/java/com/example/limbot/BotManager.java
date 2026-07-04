package com.example.limbot;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BotManager {
    
    private final Limbobots plugin;
    private final List<EntityPlayer> bots;
    
    public BotManager(Limbobots plugin) {
        this.plugin = plugin;
        this.bots = new ArrayList<>();
    }
    
    public void addBots(int count, Location location) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        
        for (int i = 0; i < count; i++) {
            try {
                int botNumber = bots.size() + 1;
                String botName = "§7[Bot] §fBot_" + botNumber;
                UUID uuid = UUID.randomUUID();
                
                GameProfile gameProfile = new GameProfile(uuid, botName);
                
                EntityPlayer entityPlayer = new EntityPlayer(
                    server, 
                    world, 
                    gameProfile,
                    new PlayerInteractManager(world)
                );
                
                // Установка позиции и поворота
                entityPlayer.setPosition(location.getX(), location.getY(), location.getZ());
                entityPlayer.yaw = location.getYaw();
                entityPlayer.pitch = location.getPitch();
                
                // Настройка сетевого соединения
                NetworkManager networkManager = new NetworkManager(EnumProtocolDirection.CLIENTBOUND);
                PlayerConnection connection = new PlayerConnection(server, networkManager, entityPlayer);
                entityPlayer.playerConnection = connection;
                
                // Добавление в мир
                world.addEntity(entityPlayer);
                
                // Отображение для всех онлайн игроков
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    CraftPlayer craftPlayer = (CraftPlayer) onlinePlayer;
                    EntityPlayer nmsPlayer = craftPlayer.getHandle();
                    
                    // Добавляем бота в таб
                    PacketPlayOutPlayerInfo packetInfo = new PacketPlayOutPlayerInfo(
                        PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                        entityPlayer
                    );
                    nmsPlayer.playerConnection.sendPacket(packetInfo);
                    
                    // Спавним бота как игрока
                    PacketPlayOutNamedEntitySpawn packetSpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);
                    nmsPlayer.playerConnection.sendPacket(packetSpawn);
                    
                    // Добавляем игрока в таб бота
                    PacketPlayOutPlayerInfo addPlayerPacket = new PacketPlayOutPlayerInfo(
                        PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                        nmsPlayer
                    );
                    connection.sendPacket(addPlayerPacket);
                }
                
                bots.add(entityPlayer);
                
                if (plugin != null) {
                    plugin.getLogger().info("Бот " + botName + " создан на координатах X:" + 
                        String.format("%.1f", location.getX()) + 
                        " Y:" + String.format("%.1f", location.getY()) + 
                        " Z:" + String.format("%.1f", location.getZ()));
                }
            } catch (Exception e) {
                if (plugin != null) {
                    plugin.getLogger().severe("Ошибка при создании бота: " + e.getMessage());
                }
                e.printStackTrace();
            }
        }
    }
    
    public void teleportBot(int index, Location location) {
        if (index < 0 || index >= bots.size()) {
            return;
        }
        
        try {
            EntityPlayer bot = bots.get(index);
            WorldServer newWorld = ((CraftWorld) location.getWorld()).getHandle();
            
            // Телепортация бота
            bot.world = newWorld;
            bot.setPosition(location.getX(), location.getY(), location.getZ());
            bot.yaw = location.getYaw();
            bot.pitch = location.getPitch();
            
            // Отправка пакета телепортации всем игрокам
            PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(bot);
            for (Player player : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(teleportPacket);
            }
            
            if (plugin != null) {
                plugin.getLogger().info("Бот " + (index + 1) + " телепортирован на X:" + 
                    String.format("%.1f", location.getX()) + 
                    " Y:" + String.format("%.1f", location.getY()) + 
                    " Z:" + String.format("%.1f", location.getZ()));
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().severe("Ошибка при телепортации бота: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
    
    public void teleportAllBots(Location location) {
        for (int i = 0; i < bots.size(); i++) {
            teleportBot(i, location);
        }
    }
    
    public void removeAllBots() {
        try {
            for (EntityPlayer bot : new ArrayList<>(bots)) {
                // Удаление из таба
                PacketPlayOutPlayerInfo removePacket = new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                    bot
                );
                
                // Удаление сущности
                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(bot.getId());
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    CraftPlayer craftPlayer = (CraftPlayer) player;
                    EntityPlayer nmsPlayer = craftPlayer.getHandle();
                    nmsPlayer.playerConnection.sendPacket(removePacket);
                    nmsPlayer.playerConnection.sendPacket(destroyPacket);
                }
                
                // Удаление из мира
                bot.world.removeEntity(bot);
            }
            bots.clear();
            
            if (plugin != null) {
                plugin.getLogger().info("Все боты успешно удалены!");
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().severe("Ошибка при удалении ботов: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
    
    public List<EntityPlayer> getBots() {
        return new ArrayList<>(bots);
    }
    
    public int getBotCount() {
        return bots.size();
    }
}
