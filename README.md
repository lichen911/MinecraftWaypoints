### Waypoints Plugin
---
#### What is it?
This project is a plugin for Bukkit based Minecraft servers to add the use of Waypoints.

This plugin allows players to set named Waypoints from their current location and store them both publicly or privately. The waypoints are stored in the plugin's config.yml, and then players may either set their compass heading to one of the stored waypoints or teleport directly to it.

#### Download and install
Download the [latest release](https://github.com/lichen911/MinecraftWaypoints/releases) and drop it in the `plugins` dir of your Bukkit based Minecraft server.

#### Usage
The following commands allow you to manage the waypoints, set them as a compass heading, or teleport directly to them. The `pub` arg is optional and is used to indicate the player is referring to public waypoints (instead of their private waypoints)
```
wp add <name> [pub] - Add waypoint with name to either the public or private list
wp del <name> [pub] - Delete a named waypoint
wp set <name> [pub] - Set compass heading to a named waypoint
wp tp <name> [pub] - Teleport to a named waypoint
wp list - Print a list of both public and the player's own private waypoints
```
