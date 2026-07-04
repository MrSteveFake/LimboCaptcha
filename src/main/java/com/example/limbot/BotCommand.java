package com.example.limbot;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BotCommand implements CommandExecutor, TabCompleter {
    
    private final Limbobots plugin;
    
    public BotCommand(Limbobots plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "❌ Эта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("limbobots.admin")) {
            player.sendMessage(ChatColor.RED + "❌ У вас нет прав для использования этой команды!");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "add":
                return handleAdd(player, args);
            case "tp":
                return handleTeleport(player, args);
            case "tpall":
                return handleTeleportAll(player);
            case "remove":
                return handleRemove(player);
            case "list":
                return handleList(player);
            case "help":
                showHelp(player);
                return true;
            default:
                player.sendMessage(ChatColor.RED + "❌ Неизвестная команда. Используйте /limbobots help");
                return true;
        }
    }
    
    private boolean handleAdd(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "❌ Использование: /limbobots add <количество>");
            return true;
        }
        
        try {
            int count = Integer.parseInt(args[1]);
            if (count <= 0) {
                player.sendMessage(ChatColor.RED + "❌ Количество должно быть больше 0!");
                return true;
            }
            if (count > 50) {
                player.sendMessage(ChatColor.RED + "❌ Максимальное количество ботов: 50!");
                return true;
            }
            
            int currentBots = plugin.getBotManager().getBotCount();
            if (currentBots + count > 50) {
                player.sendMessage(ChatColor.RED + "❌ Общее количество ботов не может превышать 50!");
                return true;
            }
            
            Location loc = player.getLocation();
            plugin.getBotManager().addBots(count, loc);
            
            player.sendMessage(ChatColor.GREEN + "✅ Добавлено " + count + " ботов!");
            player.sendMessage(ChatColor.GRAY + "📍 Координаты: X:" + 
                String.format("%.1f", loc.getX()) + 
                " Y:" + String.format("%.1f", loc.getY()) + 
                " Z:" + String.format("%.1f", loc.getZ()));
            
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "❌ Введите корректное число!");
        }
        return true;
    }
    
    private boolean handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "❌ Использование: /limbobots tp <номер_бота>");
            return true;
        }
        
        try {
            int index = Integer.parseInt(args[1]) - 1;
            int botCount = plugin.getBotManager().getBotCount();
            
            if (index < 0 || index >= botCount) {
                player.sendMessage(ChatColor.RED + "❌ Бот с номером " + args[1] + " не найден!");
                player.sendMessage(ChatColor.GRAY + "Доступные номера: 1 - " + botCount);
                return true;
            }
            
            plugin.getBotManager().teleportBot(index, player.getLocation());
            player.sendMessage(ChatColor.GREEN + "✅ Бот #" + (index + 1) + " телепортирован к вам!");
            
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "❌ Введите корректный номер бота!");
        }
        return true;
    }
    
    private boolean handleTeleportAll(Player player) {
        int botCount = plugin.getBotManager().getBotCount();
        if (botCount == 0) {
            player.sendMessage(ChatColor.YELLOW + "⚠ Нет ботов для телепортации.");
            return true;
        }
        
        plugin.getBotManager().teleportAllBots(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "✅ Все боты (" + botCount + ") телепортированы к вам!");
        return true;
    }
    
    private boolean handleRemove(Player player) {
        int botCount = plugin.getBotManager().getBotCount();
        if (botCount == 0) {
            player.sendMessage(ChatColor.YELLOW + "⚠ Нет ботов для удаления.");
            return true;
        }
        
        plugin.getBotManager().removeAllBots();
        player.sendMessage(ChatColor.GREEN + "✅ Удалено ботов: " + botCount);
        return true;
    }
    
    private boolean handleList(Player player) {
        int botCount = plugin.getBotManager().getBotCount();
        
        player.sendMessage(ChatColor.GOLD + "═══ Limbobots - Список ботов ═══");
        
        if (botCount == 0) {
            player.sendMessage(ChatColor.YELLOW + "⚠ Нет активных ботов.");
        } else {
            player.sendMessage(ChatColor.GREEN + "✓ Активных ботов: " + botCount);
            player.sendMessage("");
            
            for (int i = 0; i < botCount; i++) {
                net.minecraft.server.v1_16_R3.EntityPlayer bot = 
                    plugin.getBotManager().getBots().get(i);
                player.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + 
                    ChatColor.WHITE + bot.getName() + 
                    ChatColor.GRAY + " | HP: " + ChatColor.RED + 
                    String.format("%.0f", bot.getHealth()) + "/" + 
                    String.format("%.0f", bot.getMaxHealth()));
            }
        }
        
        player.sendMessage(ChatColor.GOLD + "══════════════════════════");
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "═══ Limbobots - Помощь ═══");
        player.sendMessage(ChatColor.YELLOW + "/limbobots add <count> " + 
            ChatColor.GRAY + "- Добавить ботов");
        player.sendMessage(ChatColor.YELLOW + "/limbobots tp <number> " + 
            ChatColor.GRAY + "- Телепортировать бота");
        player.sendMessage(ChatColor.YELLOW + "/limbobots tpall " + 
            ChatColor.GRAY + "- Телепортировать всех ботов");
        player.sendMessage(ChatColor.YELLOW + "/limbobots remove " + 
            ChatColor.GRAY + "- Удалить всех ботов");
        player.sendMessage(ChatColor.YELLOW + "/limbobots list " + 
            ChatColor.GRAY + "- Список ботов");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "tp", "tpall", "remove", "list", "help")
                .stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            return Arrays.asList("1", "5", "10", "20");
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
            List<String> numbers = new ArrayList<>();
            for (int i = 1; i <= plugin.getBotManager().getBotCount(); i++) {
                numbers.add(String.valueOf(i));
            }
            return numbers;
        }
        
        return new ArrayList<>();
    }
}
