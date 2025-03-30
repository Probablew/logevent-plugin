package hu.thepingu.logEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatColor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class LogEvent extends JavaPlugin implements CommandExecutor {

    private BukkitRunnable eventTask;
    private boolean eventRunning = false;
    private int totalTime;
    private int timeRemaining;
    private int endDistance;
    private int startDistance;
    private int broadcastInterval;
    private boolean borderShrink = true;

    @Override
    public void onEnable() {
        getCommand("logevent").setExecutor(this);
    }

    @Override
    public void onDisable() {
        stopEvent();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase();

        if ("start".equals(action)) {
            if (eventRunning) {
                sender.sendMessage(ChatColor.RED + "An event is already running!");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /logevent start <minutes> [end distance] [start distance]");
                return true;
            }

            try {
                int minutes = Integer.parseInt(args[1]);
                borderShrink = true;

                if (args.length >= 4) {
                    endDistance = Integer.parseInt(args[2]);
                    startDistance = Integer.parseInt(args[3]);
                } else {
                    borderShrink = false;
                }

                World world = Bukkit.getWorlds().get(0);
                if (sender instanceof Player) {
                    world = ((Player) sender).getWorld();
                }
                startEvent(world, minutes * 60, endDistance, startDistance);

                String startMessage = "Log event started for " + minutes + " minutes";
                if (borderShrink) {
                    startMessage += " with border going from " + startDistance + " to " + endDistance + "!";
                } else {
                    startMessage += " with a static border!";
                }

                Bukkit.broadcastMessage(ChatColor.GREEN + startMessage);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number format.");
            }
        } else if ("stop".equals(action)) {
            if (!eventRunning) {
                sender.sendMessage(ChatColor.RED + "No event is currently running.");
                return true;
            }
            stopEvent();
            sender.sendMessage(ChatColor.GREEN + "Log event stopped!");
        } else if ("help".equals(action)) {
            sendHelp(sender);
        } else {
            sendHelp(sender);
            return true;
        }
        return true;
    }

    private void startEvent(World world, int time, int endDistance, int startDistance) {
        eventRunning = true;
        totalTime = time;
        timeRemaining = time;
        this.endDistance = endDistance;
        this.startDistance = startDistance;

        if (borderShrink) {
            world.getWorldBorder().setSize(startDistance);
            world.getWorldBorder().setSize(endDistance, (long) (time * 0.9));
        }

        killAllPlayers();
        broadcastInterval = totalTime / 5;

        eventTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timeRemaining <= 0) {
                    stopEvent();
                    return;
                }

                updateActionBar();
                if ((totalTime - timeRemaining) != 0 && (totalTime - timeRemaining) % broadcastInterval == 0) {
                    broadcastRemainingTime();
                }
                timeRemaining--;
            }
        };
        eventTask.runTaskTimer(this, 0L, 20L);
    }

    private void killAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setHealth(0);
        }
    }

    private void stopEvent() {
        if (eventTask != null) {
            eventTask.cancel();
        }
        if (eventRunning) {
            World world = Bukkit.getWorlds().get(0);
            if (Bukkit.getOnlinePlayers().size() > 0) { // Corrected line
                world = Bukkit.getOnlinePlayers().iterator().next().getWorld(); // Get first player world
            }
            world.getWorldBorder().setSize(world.getWorldBorder().getSize());
            countPlayerLogs();
        }
        eventRunning = false;
    }

    private void updateActionBar() {
        String timeLeft = formatTime(timeRemaining);
        String message = ChatColor.RED + "Time Remaining: " + timeLeft;

        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, message);
        }
    }

    private void sendActionBar(Player player, String message) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent");
            Class<?> chatComponentTextClass = Class.forName("net.minecraft.server.v1_8_R3.ChatComponentText");

            Constructor<?> textConstructor = chatComponentTextClass.getConstructor(String.class);
            Object textComponent = textConstructor.newInstance(message);

            Constructor<?> packetConstructor = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class);
            Object chatPacket = packetConstructor.newInstance(textComponent, (byte) 2);

            Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server.v1_8_R3.Packet")).invoke(playerConnection, chatPacket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastRemainingTime() {
        String timeLeft = formatTime(timeRemaining);
        Bukkit.broadcastMessage(ChatColor.RED + "Time remaining: " + timeLeft);
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private void countPlayerLogs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int logCount = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && isLog(item.getType())) {
                    logCount += item.getAmount();
                }
            }
            Bukkit.broadcastMessage(ChatColor.AQUA + player.getName() + " collected " + logCount + " log(s) during the event!");
        }
    }

    private boolean isLog(Material material) {
        return material == Material.LOG || material == Material.LOG_2;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "LogEvent Plugin Help:");
        sender.sendMessage(ChatColor.YELLOW + "Author: ThePingu");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "/logevent start <minutes> [end distance] [start distance] - Starts a log event.");
        sender.sendMessage(ChatColor.YELLOW + "/logevent stop - Stops a log event.");
        sender.sendMessage(ChatColor.YELLOW + "/logevent help - Displays this help message.");
    }
}