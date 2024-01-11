package io.github.lichen911.waypoints;


import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WpCommand implements CommandExecutor {
	private final Waypoints plugin;
	private final String configPublicPrefix = "waypoints.public";
	private final String configPlayersPrefix = "waypoints.players";
	
	public WpCommand(Waypoints plugin) {
		this.plugin = plugin;
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
		String coords = this.plugin.getConfig().getString(configPath);
		String[] coordArray = coords.split("\\s+");
		
		double x = (double) Integer.parseInt(coordArray[0]);
		double y = (double) Integer.parseInt(coordArray[1]);
		double z = (double) Integer.parseInt(coordArray[2]);

		// TODO: Review to see if we should ensure that World is always the Overworld?
		Location location = new Location(player.getWorld(), x, y, z);
		return location;
	}
	
	private void addWaypoint(Player player, String wpName, String wpType) {
		Location location = player.getLocation();
		String playerName = player.getPlayerListName();
    	String coords = location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    	String configPath = this.getConfigPath(playerName, wpName, wpType);
    	
    	player.sendMessage("Creating waypoint named '" + wpName + "' at: " + coords);
    	this.plugin.getConfig().set(configPath, coords);
    	this.plugin.saveConfig();
		
	}
	
	private void delWaypoint(Player player, String wpName, String wpType) {
		String playerName = player.getPlayerListName();
    	String configPath = this.getConfigPath(playerName, wpName, wpType);
    	
    	player.sendMessage("Deleting waypoint named '" + wpName + "'");
    	this.plugin.getConfig().set(configPath, null);
    	this.plugin.saveConfig();
		
	}
	
	private void listWaypoint(Player player) {
		String playerName = player.getPlayerListName();
		String configPlayerPath = configPlayersPrefix + "." + playerName;
		Map<String, Object> publicWaypointMap = this.plugin.getConfig()
				.getConfigurationSection(configPublicPrefix).getValues(false);
		Map<String, Object> privateWaypointMap = this.plugin.getConfig()
				.getConfigurationSection(configPlayerPath).getValues(false);
		
		player.sendMessage("Public waypoints:");
		for (Map.Entry<String, Object> entry : publicWaypointMap.entrySet()) {
		    player.sendMessage("  " + entry.getKey() + " - " + entry.getValue());
		}
		
		player.sendMessage();
		player.sendMessage("Private waypoints:");
		for (Map.Entry<String, Object> entry : privateWaypointMap.entrySet()) {
		    player.sendMessage("  " + entry.getKey() + " - " + entry.getValue());
		}
	}
	
	private void setWaypoint(Player player, String wpName, String wpType) {
		Location location = this.getLocationFromConfig(player, wpName, wpType);
		player.setCompassTarget(location);
		player.sendMessage("Set compass to waypoint '" + wpName + "'");
	}
	
	
	private void tpWaypoint(Player player, String wpName, String wpType) {
		player.sendMessage("Teleporting to waypoint '" + wpName + "'");
		Location location = this.getLocationFromConfig(player, wpName, wpType);
		player.teleport(location);
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
        		player.sendMessage("Invalid command");
        		// Implement logic to print help
        	}
        } else if (split.length >= 2 && split.length <= 3) {
        	String cmd = split[0];
        	String wpName = split[1];
        	String wpType = CommandLiteral.PRIV;
        	
        	if (split.length == 3) {
        		wpType = split[2];
        		
        		if (!wpType.equals(CommandLiteral.PUB)) {
            		player.sendMessage("Invalid command");
            		// Implement logic to print help
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
        	player.sendMessage("Invalid command");
        	// Print help message here
        }
        
        return true;
    }
}
