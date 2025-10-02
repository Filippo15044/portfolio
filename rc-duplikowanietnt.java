package org.example.antylogaut.rCDuplikowanie_Tnt;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class RCDuplikowanie_Tnt extends JavaPlugin {

    private final HashMap<UUID, BukkitRunnable> activeTasks = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix", "&7[&bRemotecode&7] "));
        String noPerm = ChatColor.translateAlternateColorCodes('&', getConfig().getString("no-permission", "&cNie masz permisji!"));
        String onlyPlayer = ChatColor.translateAlternateColorCodes('&', getConfig().getString("only-player", "&cTylko gracze mogą używać tej komendy!"));
        String invalidArg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("invalid-arg", "&cPodaj poprawną ilość TNT (1-2304)!"));
        String success = ChatColor.translateAlternateColorCodes('&', getConfig().getString("success", "&aRozpoczęto wybuchanie TNT: %amount% razy!"));
        String stopped = ChatColor.translateAlternateColorCodes('&', getConfig().getString("stopped", "&cZatrzymano wybuchanie TNT."));

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + onlyPlayer);
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("tnt")) {

            if (!player.hasPermission("remotecode.tnt")) {
                player.sendMessage(prefix + noPerm);
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(prefix + invalidArg);
                return true;
            }

            int cycles;
            try {
                cycles = Integer.parseInt(args[0]);
                if (cycles <= 0 || cycles > 2304) {
                    player.sendMessage(prefix + invalidArg);
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(prefix + invalidArg);
                return true;
            }

            if (activeTasks.containsKey(player.getUniqueId())) {
                activeTasks.get(player.getUniqueId()).cancel();
            }

            BukkitRunnable task = new BukkitRunnable() {
                int remaining = cycles;

                @Override
                public void run() {
                    if (remaining <= 0) {
                        cancel();
                        activeTasks.remove(player.getUniqueId());
                        return;
                    }

                    Location loc = player.getLocation().add(0, 1, 0);
                    TNTPrimed tnt = player.getWorld().spawn(loc, TNTPrimed.class);
                    tnt.setFuseTicks(40); 
                    tnt.setSource(player);

                    remaining--;
                }
            };
            task.runTaskTimer(this, 0L, 100L); 

            activeTasks.put(player.getUniqueId(), task);
            player.sendMessage(prefix + success.replace("%amount%", String.valueOf(cycles)));
            return true;
        }

        if (command.getName().equalsIgnoreCase("stoptnt")) {
            BukkitRunnable task = activeTasks.remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
                player.sendMessage(prefix + stopped);
            } else {
                player.sendMessage(prefix + "&cNie masz aktywnego TNT.");
            }
            return true;
        }

        return false;
    }
}
