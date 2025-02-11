package net.azisaba.albardoo02;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class MessageManager {

    private final AzipediaSearcher plugin;
    private final Map<String, Object> messages = new HashMap<>();

    public MessageManager(AzipediaSearcher plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    public void loadMessages() {
        messages.clear();

        String lang = plugin.getConfig().getString("language", "ja");
        File langFile = new File(plugin.getDataFolder(), "message_" + lang + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("言語ファイル " + langFile.getName() + "が見つかりません！");
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(langFile);

        for (String key : yaml.getKeys(true)) {
            Object value = yaml.get(key);
            if (value instanceof String || value instanceof List) {
                messages.put(key, value);
            }
        }
    }

    public String getMessage(String key) {
        Object message = messages.getOrDefault(key, "メッセージが見つかりませんでした: " + key);
        if (message instanceof  String) {
            return (String) message;
        } else if (message instanceof List<?>) {
            List<?> messageList = (List<?>) message;
            StringBuilder builder = new StringBuilder();
            for (Object line : messageList) {
                if (line instanceof String) {
                    builder.append((String) line).append("\n");
                }
            }
            return builder.toString();
        }
        return message.toString();
    }

    public List<String> getMessageList(String key) {
        Object message =messages.getOrDefault(key, "メッセージが見つかりませんでした: " + key);
        if (message instanceof List<?>) {
            List<?> messageList = (List<?>) message;
            return messageList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of(message.toString());
    }
}
