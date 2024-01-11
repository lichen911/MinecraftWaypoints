package io.github.lichen911.waypoints;

import org.bukkit.plugin.java.JavaPlugin;

public final class Waypoints extends JavaPlugin {
	@Override
	public void onEnable() {
		 getCommand(CommandLiteral.WP).setExecutor(new WpCommand(this));
	}

	@Override
	public void onDisable() {
		getLogger().info("onDisable has been invoked!");
	}
}