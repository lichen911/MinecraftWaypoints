package io.github.lichen911.waypoints.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.lichen911.waypoints.Waypoints;
import io.github.lichen911.waypoints.enums.WaypointType;
import io.github.lichen911.waypoints.managers.ClickableChatManager;
import io.github.lichen911.waypoints.managers.PermissionManager;
import io.github.lichen911.waypoints.managers.WaypointManager;
import io.github.lichen911.waypoints.objects.Waypoint;
import io.github.lichen911.waypoints.utils.CommandLiteral;
import io.github.lichen911.waypoints.utils.ConfigPath;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class WpCommand implements CommandExecutor {
    private final Waypoints plugin;
    private final WaypointManager wpManager;
    private final PermissionManager permManager;
    private final ClickableChatManager ccManager;

    private final String lastWpName = "last";

    public WpCommand(Waypoints plugin, WaypointManager wpManager, PermissionManager permManager,
            ClickableChatManager ccManager) {
        this.plugin = plugin;
        this.wpManager = wpManager;
        this.permManager = permManager;
        this.ccManager = ccManager;
    }

    private void printHelp(Player player) {
        player.sendMessage("Waypoints usage:");
        player.sendMessage(ChatColor.BLUE + "wp add <name> [pub] " + ChatColor.WHITE
                + "- Add waypoint with name to either the public or private list");
        player.sendMessage(ChatColor.BLUE + "wp rm <name> [pub] " + ChatColor.WHITE + "- Delete a named waypoint");
        player.sendMessage(ChatColor.BLUE + "wp set <name> [pub] " + ChatColor.WHITE
                + "- Set compass heading to a named waypoint");
        player.sendMessage(ChatColor.BLUE + "wp tp <name> [pub] " + ChatColor.WHITE + "- Teleport to a named waypoint");
        player.sendMessage(ChatColor.BLUE + "wp list " + ChatColor.WHITE
                + "- Print a list of both public and the player's own private waypoints");

        if (!this.ccManager.isGeyserUser(player)) {
            player.sendMessage("You can also " + ChatColor.YELLOW + "click" + ChatColor.WHITE + " on the ("
                    + ChatColor.GOLD + "set tp rm" + ChatColor.WHITE + ") commands in " + ChatColor.BLUE + "wp list");
        }
    }

    private void addWaypoint(Player player, String wpName, WaypointType wpType) {
        String playerUuid = player.getUniqueId().toString();
        String playerName = player.getPlayerListName();

        Location location = player.getLocation();
        Waypoint waypoint = new Waypoint(playerName, playerUuid, wpName, wpType, location);

        player.sendMessage("Creating " + wpType.text + " waypoint named '" + ChatColor.YELLOW + wpName + ChatColor.WHITE
                + "' at " + ChatColor.YELLOW + waypoint.getCoordString());

        this.wpManager.addWaypoint(waypoint);
    }

    private void rmWaypoint(Player player, String wpName, WaypointType wpType) {
        String playerUuid = player.getUniqueId().toString();
        Waypoint waypoint = wpManager.getWaypoint(playerUuid, wpName, wpType);

        player.sendMessage(
                "Deleting " + wpType.text + " waypoint named '" + ChatColor.YELLOW + wpName + ChatColor.WHITE + "'");

        this.wpManager.rmWaypoint(waypoint);
    }

    private void sendWaypointDetailMessage(Waypoint wp, Player player) {
        String wpName = wp.getName();
        Location location = wp.getLocation();
        String biome = wp.getBiome();
        WaypointType wpType = wp.getWaypointType();

        ComponentBuilder component = new ComponentBuilder(" ");

        if (this.ccManager.getClickableChatConfig(ConfigPath.useClickableChat)) {
            if (!this.ccManager.isGeyserUser(player)) {

                TextComponent ccMenuOpts = this.ccManager.getClickableCommands(player, wpName, wpType);
                if (ccMenuOpts != null) {
                    component = component.append(ccMenuOpts).append(" ");
                }
            }
        }

        component = component.append(wpName).color(ChatColor.BLUE).append(" -> ").color(ChatColor.WHITE)
                .append(Integer.toString(location.getBlockX())).color(ChatColor.YELLOW).append(", ")
                .color(ChatColor.WHITE).append(Integer.toString(location.getBlockY())).color(ChatColor.YELLOW)
                .append(", ").color(ChatColor.WHITE).append(Integer.toString(location.getBlockZ()))
                .color(ChatColor.YELLOW).append(" [").color(ChatColor.WHITE).append(biome).color(ChatColor.YELLOW)
                .append(", ").color(ChatColor.WHITE).append(location.getWorld().getName()).color(ChatColor.YELLOW)
                .append("]").color(ChatColor.WHITE);

        BaseComponent[] msg = component.create();
        player.spigot().sendMessage(msg);
    }

    private void listWaypoint(Player player) {
        String playerUuid = player.getUniqueId().toString();

        List<Waypoint> pubWaypoints = this.wpManager.getWaypoints(WaypointType.PUBLIC);
        player.sendMessage(ChatColor.RED + WaypointType.PUBLIC.text + " waypoints" + ChatColor.WHITE + ":");
        for (Waypoint wp : pubWaypoints) {
            this.sendWaypointDetailMessage(wp, player);
        }

        List<Waypoint> privWaypoints = this.wpManager.getWaypoints(playerUuid, WaypointType.PRIVATE);
        player.sendMessage(ChatColor.RED + WaypointType.PRIVATE.text + " waypoints" + ChatColor.WHITE + ":");
        for (Waypoint wp : privWaypoints) {
            this.sendWaypointDetailMessage(wp, player);
        }
    }

    private void setWaypoint(Player player, String wpName, WaypointType wpType) {
        Waypoint waypoint = this.wpManager.getWaypoint(player.getUniqueId().toString(), wpName, wpType);
        Location location = waypoint.getLocation();

        if (location != null) {
            player.setCompassTarget(location);
            player.sendMessage("Set compass to " + wpType.text + " waypoint '" + ChatColor.YELLOW + wpName
                    + ChatColor.WHITE + "'");
        } else {
            player.sendMessage(this.plugin.getResponseMessage(ConfigPath.waypointNotExist));
        }

    }

    private void tpWaypoint(Player player, String wpName, WaypointType wpType) {
        Waypoint waypoint = this.wpManager.getWaypoint(player.getUniqueId().toString(), wpName, wpType);
        Location location = waypoint.getLocation();

        // Whenever a player teleports store their current location in
        // a waypoint named "last".
        Location prevLocation = player.getLocation().clone();
        Waypoint prevWaypoint = new Waypoint(player.getDisplayName(), player.getUniqueId().toString(), lastWpName,
                WaypointType.PRIVATE, prevLocation);

        if (location != null) {
            player.sendMessage("Teleporting to " + wpType.text + " waypoint '" + ChatColor.YELLOW + wpName
                    + ChatColor.WHITE + "'");
            player.teleport(location);

            this.wpManager.addWaypoint(prevWaypoint);
        } else {
            player.sendMessage(this.plugin.getResponseMessage(ConfigPath.waypointNotExist));
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.plugin.getResponseMessage(ConfigPath.playerCmdOnly));
            return true;
        }
        Player player = (Player) sender;

        if (split.length == 1) {
            String cmd = split[0];
            if (cmd.equals(CommandLiteral.LIST)) {
                if (this.permManager.checkHasPermission(player, cmd, null)) {
                    this.listWaypoint(player);
                } else {
                    player.sendMessage(ChatColor.YELLOW + this.plugin.getResponseMessage(ConfigPath.noPermission));
                }
            } else {
                this.printHelp(player);
            }
        } else if (split.length >= 2 && split.length <= 3) {
            String cmd = split[0];
            String wpName = split[1];
            WaypointType wpType = WaypointType.PRIVATE;

            if (split.length == 3) {
                if (!split[2].equals(CommandLiteral.PUB)) {
                    this.printHelp(player);
                    return true;
                } else {
                    wpType = WaypointType.PUBLIC;
                }
            }

            if (!this.permManager.checkHasPermission(player, cmd, wpType)) {
                player.sendMessage(ChatColor.YELLOW + this.plugin.getResponseMessage(ConfigPath.noPermission));
                return true;
            }

            if (cmd.equals(CommandLiteral.ADD)) {
                this.addWaypoint(player, wpName, wpType);
            } else if (cmd.equals(CommandLiteral.RM)) {
                this.rmWaypoint(player, wpName, wpType);
            } else if (cmd.equals(CommandLiteral.SET)) {
                this.setWaypoint(player, wpName, wpType);
            } else if (cmd.equals(CommandLiteral.TP)) {
                this.tpWaypoint(player, wpName, wpType);
            } else {
                this.printHelp(player);
            }
        } else {
            this.printHelp(player);
        }

        return true;
    }
}
