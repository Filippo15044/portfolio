package org.example.antylogaut.rCTpaAnarchia;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public class RCTpaAnarchia extends JavaPlugin {
    private final Map<UUID, UUID> tpaRequests = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getCommand("tpa").setExecutor(this);
        getCommand("tpaccept").setExecutor(this);
        getCommand("tpdeny").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        FileConfiguration cfg = getConfig();

        switch (command.getName().toLowerCase()) {
            case "tpa":
                if (args.length != 1) {
                    player.sendMessage("§cPoprawne użycie: /tpa <gracz>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage("§cGracz nie jest online.");
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage("§cNie możesz teleportować się sam  do siebie.");
                    return true;
                }

                tpaRequests.put(target.getUniqueId(), player.getUniqueId());
                sendMessage(player, cfg.getString("tpa.messages.request-sent")
                        .replace("%target%", target.getName()));

                sendMessage(target, cfg.getString("tpa.messages.request-received")
                        .replace("%sender%", player.getName()));

                int timeout = cfg.getInt("tpa.request-timeout-seconds");
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (tpaRequests.get(target.getUniqueId()) != null &&
                            tpaRequests.get(target.getUniqueId()).equals(player.getUniqueId())) {
                        tpaRequests.remove(target.getUniqueId());
                        sendMessage(player, cfg.getString("tpa.messages.request-expired"));
                    }
                }, timeout * 20L);
                return true;

            case "tpaccept":
                UUID requesterId = tpaRequests.get(player.getUniqueId());
                if (requesterId == null) {
                    sendMessage(player, cfg.getString("tpa.messages.no-request"));
                    return true;
                }
                Player requester = Bukkit.getPlayer(requesterId);
                if (requester == null) {
                    sendMessage(player, cfg.getString("tpa.messages.no-request"));
                    return true;
                }

                int delay = cfg.getInt("tpa.teleport-delay-seconds");
                sendMessage(player, cfg.getString("tpa.messages.accepted").replace("%delay%", String.valueOf(delay)));
                sendMessage(requester, cfg.getString("tpa.messages.accepted").replace("%delay%", String.valueOf(delay)));

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    requester.teleport(player.getLocation());
                    sendMessage(requester, cfg.getString("tpa.messages.teleported").replace("%target%", player.getName()));
                }, delay * 20L);

                tpaRequests.remove(player.getUniqueId());
                return true;

            case "tpdeny":
                UUID denyRequesterId = tpaRequests.get(player.getUniqueId());
                if (denyRequesterId == null) {
                    sendMessage(player, cfg.getString("tpa.messages.no-request"));
                    return true;
                }
                Player denyRequester = Bukkit.getPlayer(denyRequesterId);
                if (denyRequester != null)
                    sendMessage(denyRequester, cfg.getString("tpa.messages.denied"));
                tpaRequests.remove(player.getUniqueId());
                sendMessage(player, cfg.getString("tpa.messages.denied"));
                return true;
        }

        return false;
    }

    private void sendMessage(Player p, String msg) {
        if (msg != null)
            p.sendMessage(msg.replace("&", "§"));
    }
}
