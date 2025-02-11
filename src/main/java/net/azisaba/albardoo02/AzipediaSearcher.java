package net.azisaba.albardoo02;

import net.azisaba.albardoo02.Command.WikiCommandExecutor;
import net.azisaba.albardoo02.Command.WikiCommandTabComplete;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class AzipediaSearcher extends JavaPlugin {

    private MessageManager messageManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getLogger().info("Plugin has been enabled");

        double currentVersion = getConfig().getDouble("configVersion",1.0);
        double saveVersion = getSavedVersion();

        if (currentVersion > saveVersion) {
            getLogger().info("新しいバージョン (" + currentVersion + ") のため、ファイルを更新します...");
            moveOldFiles();
            saveNewFiles();
            saveVersion(currentVersion);
        } else {
            getLogger().info("ファイルは最新です");
            saveIfNotExists("message_ja.yml");
            saveIfNotExists("message_en.yml");
        }

        this.saveDefaultConfig();
        messageManager = new MessageManager(this);

        this.getCommand("wiki").setExecutor(new WikiCommandExecutor(this, messageManager));
        this.getCommand("wiki").setTabCompleter(new WikiCommandTabComplete(this));

    }

    private void moveOldFiles() {
        moveFileToOld("message_ja.yml");
        moveFileToOld("message_en.yml");
    }

    private void moveFileToOld(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) return;

        // old フォルダを作成
        File oldFolder = new File(getDataFolder(), "old");
        if (!oldFolder.exists()) {
            oldFolder.mkdir();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File backupFile = new File(oldFolder, fileName.replace(".yml", "_" + timestamp + ".yml"));

        if (file.renameTo(backupFile)) {
            getLogger().info(fileName + " をoldフォルダに移動しました: " + backupFile.getName());
        } else {
            getLogger().warning(fileName + " の移動に失敗しました");
        }
    }

    private void saveNewFiles() {
        saveResource("message_ja.yml", true);
        saveResource("message_en.yml", true);
    }

    private void saveIfNotExists(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
        }
    }

    private double getSavedVersion() {
        File versionFile = new File(getDataFolder(), "version.yml");
        if (!versionFile.exists()) return 1.0;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(versionFile);
        return config.getDouble("configVersion", 1.0);
    }

    private void saveVersion(double version) {
        File versionFile = new File(getDataFolder(), "version.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("configVersion", version);

        try {
            config.save(versionFile);
        } catch (IOException e) {
            getLogger().severe("バージョン情報の保存に失敗しました: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.saveConfig();
        this.getLogger().info("plugin has been disabled");
    }
}
