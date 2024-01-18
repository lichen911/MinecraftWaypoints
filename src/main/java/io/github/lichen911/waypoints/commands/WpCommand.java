package io.github.lichen911.waypoints.commands;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import io.github.lichen911.waypoints.CommandLiteral;
import io.github.lichen911.waypoints.NoPermissionException;
import io.github.lichen911.waypoints.Waypoint;
import io.github.lichen911.waypoints.Waypoints;
import io.github.lichen911.waypoints.enums.WaypointType;
import io.github.lichen911.waypoints.managers.WaypointManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class WpCommand implements CommandExecutor {
    private final Waypoints plugin;
    private final WaypointManager wpManager;

    private final String configPublicPrefix = "waypoints.public";
    private final String configPlayersPrefix = "waypoints.players";
    private final String configLocPath = "loc";
    private final String configBiomePath = "biome";
    private final String configUserPath = "userName";
    private final String lastWpName = "last";
    private final String wpTypeNamePub = "public";
    private final String wpTypeNamePriv = "private";
    private final String chatConfigPrefix = "clickableChat";

    public WpCommand(Waypoints plugin, WaypointManager wpManager) {
        this.plugin = plugin;
        this.wpManager = wpManager;
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
    }

    private boolean getClickableChatConfig(String setting) {
        return this.plugin.getConfig().getBoolean(chatConfigPrefix + "." + setting);
    }

    private void addWaypoint(Player player, String wpName, WaypointType wpType) {
        String playerUuid = player.getUniqueId().toString();
        String playerName = player.getPlayerListName();

        Location location = player.getLocation();
        Waypoint waypoint = new Waypoint(playerName, playerUuid, wpName, wpType, location);

        player.sendMessage("Creating " + wpType + " waypoint named '" + ChatColor.YELLOW + wpName + ChatColor.WHITE
                + "' at " + ChatColor.YELLOW + waypoint.getCoordString());

        this.wpManager.addWaypoint(waypoint);
    }

    private void rmWaypoint(Player player, String wpName, WaypointType wpType) {
        String playerUuid = player.getUniqueId().toString();
        Waypoint waypoint = wpManager.getWaypoint(playerUuid, wpName, wpType);

        player.sendMessage(
                "Deleting " + wpType + " waypoint named '" + ChatColor.YELLOW + wpName + ChatColor.WHITE + "'");

        this.wpManager.rmWaypoint(waypoint);
    }

    private void printWaypointConfigMap(ConfigurationSection waypointMapConfigSection, Player player, String wpType,
            String configPrefix) {
        if (waypointMapConfigSection != null) {
            Map<String, Object> waypointMap = waypointMapConfigSection.getValues(false);

            player.sendMessage(ChatColor.RED + wpType + " waypoints" + ChatColor.WHITE + ":");
            for (Map.Entry<String, Object> entry : waypointMap.entrySet()) {
                String wpName = entry.getKey();

                String wpLocPath = configPrefix + "." + wpName + "." + configLocPath;
                Location wpLoc = Waypoints.WpConfig.getConfig().getLocation(wpLocPath);

                String wpBiomePath = configPrefix + "." + wpName + "." + configBiomePath;
                String wpBiome = Waypoints.WpConfig.getConfig().getString(wpBiomePath);

//                this.printWaypointDetail(player, wpName, wpLoc, wpBiome);
                this.sendWaypointDetailMessage(player, wpName, wpLoc, wpBiome, wpType);
            }
        } else {
            player.sendMessage("No " + wpType + " waypoints");
        }
    }

//    private void printWaypointDetail(Player player, String wpName, Location location, String biome) {
//        String msg = "  " + ChatColor.BLUE + wpName + ChatColor.WHITE;
//        msg += " -> " + ChatColor.YELLOW + location.getBlockX() + ChatColor.WHITE;
//        msg += ", " + ChatColor.YELLOW + location.getBlockY() + ChatColor.WHITE;
//        msg += ", " + ChatColor.YELLOW + location.getBlockZ() + ChatColor.WHITE;
//        msg += " [" + ChatColor.YELLOW + biome + ChatColor.WHITE;
//        msg += ", " + ChatColor.YELLOW + location.getWorld().getName() + ChatColor.WHITE + "]";
//        msg += ChatColor.WHITE + " (" + ChatColor.GOLD + "set tp rm" + ChatColor.WHITE + ")";
//
//        player.sendMessage(msg);
//    }

    private TextComponent addClickableCommands(Player player, String wpName, String wpType) {
        boolean useRunCmd = this.getClickableChatConfig("clicksRunCommands");
        boolean hasSetPerm = this.checkHasPermission(player, wpName, wpType);

        TextComponent msg = new TextComponent("(");

    }

    private void sendWaypointDetailMessage(Player player, String wpName, Location location, String biome) {
        ComponentBuilder component = new ComponentBuilder("  ").append(wpName).color(ChatColor.BLUE).append(" -> ")
                .color(ChatColor.WHITE).append(Integer.toString(location.getBlockX())).color(ChatColor.YELLOW)
                .append(", ").color(ChatColor.WHITE).append(Integer.toString(location.getBlockY()))
                .color(ChatColor.YELLOW).append(", ").color(ChatColor.WHITE)
                .append(Integer.toString(location.getBlockZ())).color(ChatColor.YELLOW).append(" [")
                .color(ChatColor.WHITE).append(biome).color(ChatColor.YELLOW).append(", ").color(ChatColor.WHITE)
                .append(location.getWorld().getName()).color(ChatColor.YELLOW).append("]").color(ChatColor.WHITE);

        if (this.getClickableChatConfig("useClickableChat")) {
            // TODO: Add logic to test for Geyser users

        }

        BaseComponent[] msg = component.create();
        player.spigot().sendMessage(msg);
    }

    private void listWaypoint(Player player) {
        String playerUuid = player.getUniqueId().toString();
        String configPlayerPath = configPlayersPrefix + "." + playerUuid;

        ConfigurationSection publicWaypointMapSection = Waypoints.WpConfig.getConfig()
                .getConfigurationSection(configPublicPrefix);
        this.printWaypointConfigMap(publicWaypointMapSection, player, wpTypeNamePub, configPublicPrefix);

        ConfigurationSection privateWaypointMapSection = Waypoints.WpConfig.getConfig()
                .getConfigurationSection(configPlayerPath);
        this.printWaypointConfigMap(privateWaypointMapSection, player, wpTypeNamePriv, configPlayerPath);
    }

    private void setWaypoint(Player player, String wpName, WaypointType wpType) {
        Waypoint waypoint = this.wpManager.getWaypoint(player.getUniqueId().toString(), wpName, wpType);
        Location location = waypoint.getLocation();

        if (location != null) {
            player.setCompassTarget(location);
            player.sendMessage(
                    "Set compass to " + wpType + " waypoint '" + ChatColor.YELLOW + wpName + ChatColor.WHITE + "'");
        } else {
            player.sendMessage("Waypoint does not exist");
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
            player.sendMessage(
                    "Teleporting to " + wpType + " waypoint '" + ChatColor.YELLOW + wpName + ChatColor.WHITE + "'");
            player.teleport(location);

            this.wpManager.addWaypoint(prevWaypoint);
        } else {
            player.sendMessage("Waypoint does not exist");
        }
    }

    public String getPermissionPath(String cmd, WaypointType wpType) {
        String subCmd = cmd.toString().toLowerCase();

        String permPath;
        if (wpType != null) {
            permPath = "waypoints." + wpType.text + "." + subCmd;
        } else {
            permPath = "waypoints." + subCmd;
        }
        return permPath;
    }

    public boolean checkHasPermission(Player player, String cmd, WaypointType wpType) throws NoPermissionException {
        if (player.hasPermission(this.getPermissionPath(cmd, wpType))) {
            return true;
        }

        throw new NoPermissionException();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;

        try {
            if (split.length == 1) {
                String cmd = split[0];
                if (cmd.equals(CommandLiteral.LIST)) {
                    this.checkHasPermission(player, cmd, null);
                    this.listWaypoint(player);
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

                this.checkHasPermission(player, cmd, wpType);
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
        } catch (

        NoPermissionException ex) {
            player.sendMessage(ChatColor.YELLOW + "Player does not have permission");
        }

        return true;
    }
}
