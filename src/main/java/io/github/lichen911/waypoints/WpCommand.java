package io.github.lichen911.waypoints;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class WpCommand implements CommandExecutor {
    private final Waypoints plugin;
    private final String configPublicPrefix = "waypoints.public";
    private final String configPlayersPrefix = "waypoints.players";

    public WpCommand(Waypoints plugin) {
        this.plugin = plugin;
    }

    private void printHelp(Player player) {
        player.sendMessage("Waypoints usage:");
        player.sendMessage("wp add <name> [pub] - Add waypoint with name to either the public or private list");
        player.sendMessage("wp rm <name> [pub] - Delete a named waypoint");
        player.sendMessage("wp set <name> [pub] - Set compass heading to a named waypoint");
        player.sendMessage("wp tp <name> [pub] - Teleport to a named waypoint");
        player.sendMessage("wp list - Print a list of both public and the player's own private waypoints");
    }

    private String getConfigPath(String playerName, String wpName, String wpType) {
        String configPath;
        if (wpType.equals(CommandLiteral.PUB)) {
            configPath = configPublicPrefix + "." + wpName;
        } else {
            configPath = configPlayersPrefix + "." + playerName + "." + wpName;
        }

        return configPath;
    }

    private Location getLocationFromConfig(Player player, String wpName, String wpType) {
        String playerName = player.getPlayerListName();
        String configPath = this.getConfigPath(playerName, wpName, wpType);
        String coord = this.plugin.getConfig().getString(configPath);

        Location location = null;
        if (coord != null) {
            String[] coordArray = coord.split("\\s+");

            double x = (double) Integer.parseInt(coordArray[0]);
            double y = (double) Integer.parseInt(coordArray[1]);
            double z = (double) Integer.parseInt(coordArray[2]);

            String worldName = coordArray[3];
            World world = Bukkit.getWorld(worldName);

            location = new Location(world, x, y, z);
        }
        return location;
    }

    // Overloaded addWaypoint method to allow passing location that is different
    // than player's current location
    private void addWaypoint(Player player, String worldName, String wpName, String wpType, Location location) {
        String playerName = player.getPlayerListName();
        String coord = location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
        String configPath = this.getConfigPath(playerName, wpName, wpType);

        player.sendMessage("Creating waypoint named '" + wpName + "' at " + coord);
        this.plugin.getConfig().set(configPath, coord + " " + worldName);
        this.plugin.saveConfig();
    }

    private void addWaypoint(Player player, String wpName, String wpType) {
        Location location = player.getLocation();
        String worldName = player.getWorld().getName();
        this.addWaypoint(player, worldName, wpName, wpType, location);
    }

    private void delWaypoint(Player player, String wpName, String wpType) {
        String playerName = player.getPlayerListName();
        String configPath = this.getConfigPath(playerName, wpName, wpType);

        player.sendMessage("Deleting waypoint named '" + wpName + "'");
        this.plugin.getConfig().set(configPath, null);
        this.plugin.saveConfig();

    }

    private void printWaypointConfigMap(ConfigurationSection waypointMapConfigSection, Player player,
            String wpTypeDesc) {
        if (waypointMapConfigSection != null) {
            Map<String, Object> publicWaypointMap = waypointMapConfigSection.getValues(false);

            player.sendMessage(wpTypeDesc + " waypoints:");
            for (Map.Entry<String, Object> entry : publicWaypointMap.entrySet()) {
                String coordStr = (String) entry.getValue();
                String[] coordArray = coordStr.split("\\s+");
                String x = coordArray[0];
                String y = coordArray[1];
                String z = coordArray[2];
                String world = coordArray[3];

                player.sendMessage("  " + entry.getKey() + " -> " + x + ", " + y + ", " + z + " [" + world + "]");
            }
        } else {
            player.sendMessage("No " + wpTypeDesc + " waypoints");
        }
    }

    private void listWaypoint(Player player) {
        String playerName = player.getPlayerListName();
        String configPlayerPath = configPlayersPrefix + "." + playerName;
        ConfigurationSection publicWaypointMapSection = this.plugin.getConfig()
                .getConfigurationSection(configPublicPrefix);
        ConfigurationSection privateWaypointMapSection = this.plugin.getConfig()
                .getConfigurationSection(configPlayerPath);

        this.printWaypointConfigMap(publicWaypointMapSection, player, "public");
        this.printWaypointConfigMap(privateWaypointMapSection, player, "private");

    }

    private void setWaypoint(Player player, String wpName, String wpType) {
        Location location = this.getLocationFromConfig(player, wpName, wpType);
        if (location != null) {
            player.setCompassTarget(location);
            player.sendMessage("Set compass to waypoint '" + wpName + "'");
        } else {
            player.sendMessage("Waypoint does not exist");
        }

    }

    private void tpWaypoint(Player player, String wpName, String wpType) {
        Location prevLocation = player.getLocation().clone();
        String prevWorldName = player.getWorld().getName();

        Location location = this.getLocationFromConfig(player, wpName, wpType);
        if (location != null) {
            player.sendMessage("Teleporting to waypoint '" + wpName + "'");
            player.teleport(location);

            // Whenever a player teleports first store their current location in
            // a waypoint named "last".
            this.addWaypoint(player, prevWorldName, "last", CommandLiteral.PRIV, prevLocation);
        } else {
            player.sendMessage("Waypoint does not exist");
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;

        if (split.length == 1) {
            String cmd = split[0];
            if (cmd.equals(CommandLiteral.LIST)) {
                this.listWaypoint(player);
            } else {
                this.printHelp(player);
            }
        } else if (split.length >= 2 && split.length <= 3) {
            String cmd = split[0];
            String wpName = split[1];
            String wpType = CommandLiteral.PRIV;

            if (split.length == 3) {
                wpType = split[2];

                if (!wpType.equals(CommandLiteral.PUB)) {
                    this.printHelp(player);
                    return true;
                }
            }

            if (cmd.equals(CommandLiteral.ADD)) {
                this.addWaypoint(player, wpName, wpType);
            } else if (cmd.equals(CommandLiteral.RM)) {
                this.delWaypoint(player, wpName, wpType);
            } else if (cmd.equals(CommandLiteral.SET)) {
                this.setWaypoint(player, wpName, wpType);
            } else if (cmd.equals(CommandLiteral.TP)) {
                this.tpWaypoint(player, wpName, wpType);
            }
        } else {
            this.printHelp(player);
        }

        return true;
    }
}
