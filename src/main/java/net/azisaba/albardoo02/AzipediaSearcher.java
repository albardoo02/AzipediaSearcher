package net.azisaba.albardoo02;

import net.azisaba.albardoo02.Command.WikiCommandExecutor;
import net.azisaba.albardoo02.Command.WikiCommandTabComplete;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class AzipediaSearcher extends JavaPlugin {

    private MessageManager messageManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getLogger().info("Plugin has been enabled");

        this.saveDefaultConfig();
        messageManager = new MessageManager(this);

        saveIfNotExists("message_ja.yml");
        saveIfNotExists("message_en.yml");

        this.getCommand("wiki").setExecutor(new WikiCommandExecutor(this, messageManager));
        this.getCommand("wiki").setTabCompleter(new WikiCommandTabComplete(this));

    }

    private void saveIfNotExists(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.saveConfig();
        this.getLogger().info("plugin has been disabled");
    }
}
