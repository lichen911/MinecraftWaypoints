package io.github.lichen911.waypoints;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.lichen911.waypoints.commands.WpCommand;
import io.github.lichen911.waypoints.commands.WpTabCompleter;
import io.github.lichen911.waypoints.managers.ClickableChatManager;
import io.github.lichen911.waypoints.managers.PermissionManager;
import io.github.lichen911.waypoints.managers.WaypointManager;
import io.github.lichen911.waypoints.utils.CommandLiteral;
import io.github.lichen911.waypoints.utils.ConfigPath;
import io.github.lichen911.waypoints.utils.ConfigReader;
import io.github.lichen911.waypoints.utils.UpdateChecker;

public final class Waypoints extends JavaPlugin {
    private static ConfigReader wpConfig;
    private static WaypointManager wpManager;
    private static PermissionManager permManager;
    private ClickableChatManager ccManager;

    private static final int spigotResourceId = 114447;
    private static final int bStatsPluginId = 20727;

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, bStatsPluginId);

        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.getConfig().options().parseComments(true);
        this.saveConfig();

        this.checkForUpdates();

        wpConfig = new ConfigReader(this, "", "waypoints.yml");
        wpConfig.saveDefaultConfig();

        wpManager = new WaypointManager(wpConfig);
        permManager = new PermissionManager();
        ccManager = new ClickableChatManager(this, permManager);

        getCommand(CommandLiteral.WP).setExecutor(new WpCommand(this, wpManager, permManager, ccManager));
        getCommand(CommandLiteral.WP).setTabCompleter(new WpTabCompleter(wpManager));
    }

    public String getResponseMessage(String msgName) {
        return this.getConfig().getString(msgName);
    }

    private void checkForUpdates() {
        if (this.getConfig().getBoolean(ConfigPath.checkForUpdates)) {
            UpdateChecker updater = new UpdateChecker(this, spigotResourceId);
            try {
                int updateResult = updater.checkForUpdates();
                if (updateResult < 0) {
                    getLogger().warning("An update was found! New version: " + updater.getLatestVersion()
                            + ", download: " + updater.getResourceURL());
                } else if (updateResult > 0) {
                    getLogger().info("Current version appears to be newer than the latest official release version ("
                            + updater.getLatestVersion() + ")");
                }
            } catch (Exception e) {
                getLogger().warning("Error while checking for updates: " + e.getMessage());
            }
        }
    }
}