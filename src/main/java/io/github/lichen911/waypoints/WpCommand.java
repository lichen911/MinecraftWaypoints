package io.github.lichen911.waypoints;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class WpCommand implements CommandExecutor {
    private final Waypoints plugin;
    private final String configPublicPrefix = "waypoints.public";
    private final String configPlayersPrefix = "waypoints.players";
    private final String configLocPath = "loc";
    private final String configBiomePath = "biome";
    private final String configUserPath = "userName";
    private final String lastWpName = "last";
    private final String wpTypeNamePub = "public";
    private final String wpTypeNamePriv = "private";

    public WpCommand(Waypoints plugin) {
        this.plugin = plugin;
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

    private String getConfigPath(String playerUuid, String wpName, String wpType) {
        String configPath;
        if (wpType.equals(wpTypeNamePub)) {
            configPath = configPublicPrefix + "." + wpName;
        } else {
            configPath = configPlayersPrefix + "." + playerUuid + "." + wpName;
        }

        return configPath;
    }

    private Location getLocationFromConfig(Player player, String wpName, String wpType) {
        String playerUuid = player.getUniqueId().toString();
        String configPath = this.getConfigPath(playerUuid, wpName, wpType) + "." + configLocPath;
        Location location = Waypoints.WpConfig.getConfig().getLocation(configPath);
        return location;
    }

    // Overloaded addWaypoint method to allow passing location that is different
    // than player's current location
    private void addWaypoint(Player player, String worldName, String wpName, String wpType, Location location) {
        String playerUuid = player.getUniqueId().toString();
        String playerName = player.getPlayerListName();
        String coord = location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
        String configPath = this.getConfigPath(playerUuid, wpName, wpType);
        String playerBiome = location.getBlock().getBiome().toString();

        // Do not display the message if the waypoint is named "last" since this is an
        // automatically created waypoint.
        if (!wpName.equals(lastWpName)) {
            player.sendMessage("Creating " + wpType + " waypoint named '" + ChatColor.YELLOW + wpName + ChatColor.WHITE
                    + "' at " + ChatColor.YELLOW + coord);
        }

        Waypoints.WpConfig.getConfig().set(configPath + "." + configLocPath, location);
        Waypoints.WpConfig.getConfig().set(configPath + "." + configBiomePath, playerBiome);
        Waypoints.WpConfig.getConfig().set(configPath + "." + configUserPath, playerName);
        Waypoints.WpConfig.saveConfig();
    }

    private void addWaypoint(Player player, String wpName, String wpType) {
        Location location = player.getLocation();
        String worldName = player.getWorld().getName();
        this.addWaypoint(player, worldName, wpName, wpType, location);
    }

    private void delWaypoint(Player player, String wpName, String wpType) {
        String playerUuid = player.getUniqueId().toString();
        String configPath = this.getConfigPath(playerUuid, wpName, wpType);

        player.sendMessage(
                "Deleting " + wpType + " waypoint named '" + ChatColor.YELLOW + wpName + ChatColor.WHITE + "'");
        Waypoints.WpConfig.getConfig().set(configPath, null);
        Waypoints.WpConfig.saveConfig();

    }

    private void printWaypointConfigMap(ConfigurationSection waypointMapConfigSection, Player player, String wpTypeDesc,
            String configPrefix) {
        if (waypointMapConfigSection != null) {
            Map<String, Object> waypointMap = waypointMapConfigSection.getValues(false);

            player.sendMessage(ChatColor.RED + wpTypeDesc + " waypoints" + ChatColor.WHITE + ":");
            for (Map.Entry<String, Object> entry : waypointMap.entrySet()) {
                String wpName = entry.getKey();

                String wpLocPath = configPrefix + "." + wpName + "." + configLocPath;
                Location wpLoc = Waypoints.WpConfig.getConfig().getLocation(wpLocPath);

                String wpBiomePath = configPrefix + "." + wpName + "." + configBiomePath;
                String wpBiome = Waypoints.WpConfig.getConfig().getString(wpBiomePath);

                this.printWaypointDetail(player, wpName, wpLoc, wpBiome);
            }
        } else {
            player.sendMessage("No " + wpTypeDesc + " waypoints");
        }
    }

    private void printWaypointDetail(Player player, String wpName, Location location, String biome) {
        String msg = "  " + ChatColor.BLUE + wpName + ChatColor.WHITE;
        msg += " -> " + ChatColor.YELLOW + location.getBlockX() + ChatColor.WHITE;
        msg += ", " + ChatColor.YELLOW + location.getBlockY() + ChatColor.WHITE;
        msg += ", " + ChatColor.YELLOW + location.getBlockZ() + ChatColor.WHITE;
        msg += " [" + ChatColor.YELLOW + biome + ChatColor.WHITE;
        msg += ", " + ChatColor.YELLOW + location.getWorld().getName() + ChatColor.WHITE + "]";

        player.sendMessage(msg);
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

    private void setWaypoint(Player player, String wpName, String wpType) {
        Location location = this.getLocationFromConfig(player, wpName, wpType);
        if (location != null) {
            player.setCompassTarget(location);
            player.sendMessage(
                    "Set compass to " + wpType + " waypoint '" + ChatColor.YELLOW + wpName + ChatColor.WHITE + "'");
        } else {
            player.sendMessage("Waypoint does not exist");
        }

    }

    private void tpWaypoint(Player player, String wpName, String wpType) {
        Location prevLocation = player.getLocation().clone();
        String prevWorldName = player.getWorld().getName();

        Location location = this.getLocationFromConfig(player, wpName, wpType);
        if (location != null) {
            player.sendMessage(
                    "Teleporting to " + wpType + " waypoint '" + ChatColor.YELLOW + wpName + ChatColor.WHITE + "'");
            player.teleport(location);

            // Whenever a player teleports first store their current location in
            // a waypoint named "last".
            this.addWaypoint(player, prevWorldName, lastWpName, wpTypeNamePriv, prevLocation);
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
            String wpType = wpTypeNamePriv;

            if (split.length == 3) {
                if (!split[2].equals(CommandLiteral.PUB)) {
                    this.printHelp(player);
                    return true;
                } else {
                    wpType = wpTypeNamePub;
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
            } else {
                this.printHelp(player);
            }
        } else {
            this.printHelp(player);
        }

        return true;
    }
}
