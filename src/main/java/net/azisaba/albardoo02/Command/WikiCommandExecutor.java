package net.azisaba.albardoo02.Command;

import net.azisaba.albardoo02.AzipediaSearcher;
import net.azisaba.albardoo02.MessageManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WikiCommandExecutor implements CommandExecutor {

    private final Map<UUID, Integer> pageMap = new HashMap<>();
    private final Map<UUID, JSONArray> searchResultsMap = new HashMap<>();
    private final Map<UUID, Long> cooldownMap = new HashMap<>();
    private final Map<UUID, String> queryMap = new HashMap<>();
    private final AzipediaSearcher plugin;
    private final MessageManager messageManager;

    public WikiCommandExecutor(AzipediaSearcher plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    private boolean isCooldownActive(Player player) {
        UUID playerId = player.getUniqueId();
        int cooldownTime = plugin.getConfig().getInt("cooldown", 10);
        long currentTime = System.currentTimeMillis();

        if (cooldownMap.containsKey(playerId)) {
            long lastUsed = cooldownMap.get(playerId);
            if ((currentTime - lastUsed) < cooldownTime * 1000L) {
                long remainingTime = (cooldownTime * 1000L - (currentTime - lastUsed)) / 1000;
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("CooldownMessage").replaceFirst("%time", String.valueOf(remainingTime))));
                return true;
            }
        }

        cooldownMap.put(playerId, currentTime);
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("OnlyPlayers")));
            return true;

        }
        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (args.length < 1) {
            List<String> WikiUrl = messageManager.getMessageList("WikiUrl");
            for (String msg : WikiUrl) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
            }
            return true;
        }

        String subCommand = args[0];

        if (subCommand.equalsIgnoreCase("search") || subCommand.equalsIgnoreCase("s")) {
            if (args.length < 2) {
                List<String> searchMessages = messageManager.getMessageList("SearchUsage");
                for (String msg : searchMessages) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
                }
                return true;
            }
            if (isCooldownActive(player)) return true;

            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("Searching")));

            String type = "and";
            String limit = "text";

            List<String> queryWords = new ArrayList<>();
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("type=")) {
                    type = arg.replace("type=","").toLowerCase();
                } else if (arg.startsWith("limit=")) {
                    limit = arg.replace("limit=","").toLowerCase();
                } else {
                    queryWords.add(arg);
                }
            }

            String query = String.join(" ",queryWords);

            JSONArray items = searchWiki(query, type, limit);
            if (items == null || items.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("SearchNotFound").replace("%search",query)));
                return true;
            }
            searchResultsMap.put(playerId, items);
            pageMap.put(playerId, 1);
            queryMap.put(playerId, query);

            showSearchResults(player, items, 1, query);
            return true;
        }

        if (subCommand.equalsIgnoreCase("next")) {
            handlePagination(player, playerId, true);
            return true;
        }

        if (subCommand.equalsIgnoreCase("prev")) {
            handlePagination(player, playerId, false);
            return true;
        }

        if (subCommand.equalsIgnoreCase("help") || subCommand.equalsIgnoreCase("h")) {
            List<String> helpMessages = messageManager.getMessageList("Help");
            for (String msg : helpMessages) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
            }
            return true;
        }

        if (subCommand.equalsIgnoreCase("version") || subCommand.equalsIgnoreCase("v")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&aAzipediaSearcher &fVersion &a" + plugin.getConfig().getString("version")));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&fWebsite: &ahttps://github.com/albardoo02/"));
            return true;
        }

        if (subCommand.equalsIgnoreCase("reload") || subCommand.equalsIgnoreCase("r")) {
            if (!sender.hasPermission("azipediasearcher.command.reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getMessage("NoPermission")));
                return true;
            }
            plugin.reloadConfig();
            messageManager.loadMessages();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("ReloadConfig")));
            return true;
        }

        List<String> unknownMessages = messageManager.getMessageList("UnknownCommand");
        for (String msg : unknownMessages) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',msg).replace("%command",subCommand));
        }
        return true;
    }

    private JSONArray searchWiki(String query, String type, String limit) {
        try {
            String baseUrl = plugin.getConfig().getString("BaseUrl");
            String encodedQuery;
            if (type.equalsIgnoreCase("or")) {
                encodedQuery = URLEncoder.encode(query.replace(" ","|"), StandardCharsets.UTF_8);
            } else {
                encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            }

            String srwhat = limit.equalsIgnoreCase("title") ? "&srwhat=title" : "&srwhat=text";

            String  urlString = baseUrl + "api.php?action=query&list=search&srsearch=" + encodedQuery + srwhat + "&format=json";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            return json.getJSONObject("query").getJSONArray("search");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handlePagination(Player player, UUID playerId, boolean isNext) {
        if (!searchResultsMap.containsKey(playerId)) {
            List<String> NotFoundMessage = messageManager.getMessageList("NoSearch");
            for (String msg : NotFoundMessage) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
            }
            return;
        }

        int currentPage = pageMap.getOrDefault(playerId, 1);
        JSONArray items = searchResultsMap.get(playerId);
        String query = queryMap.getOrDefault(playerId, "");

        int totalPages = (int) Math.ceil(items.length() / 5.0);

        if (isNext && currentPage < totalPages) {
            pageMap.put(playerId, currentPage + 1);
            showSearchResults(player, items, currentPage + 1, query);
        } else if (!isNext && currentPage> 1) {
            pageMap.put(playerId, currentPage - 1);
            showSearchResults(player, items, currentPage - 1, query);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getMessage("PageEnd")));
        }
    }

    private void showSearchResults(Player player, JSONArray items, int page, String query) {
        int totalResults = items.length();
        int totalPages = (int) Math.ceil(totalResults / 5.0);
        int start = (page - 1) * 5;
        int end = Math.min(start + 5, totalResults);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("SearchResult").replace("%search",query)).replace("%totalResults", String.valueOf(totalResults)));


        for (int i = start; i < end; i++) {
            JSONObject item = items.getJSONObject(i);
            String title = item.getString("title");
            String link = "https://wiki.azisaba.net/wiki/" + title.replace(" ","_");

            TextComponent message = new TextComponent(ChatColor.GRAY + "[" + (i + 1) + "] " + ChatColor.WHITE + title);
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("OpenUrl"))).create()
            ));
            player.spigot().sendMessage(message);
        }

        TextComponent navigation = new TextComponent();
        if (page > 1) {
            TextComponent prevButton = new TextComponent(ChatColor.BLUE + "◀ " + ChatColor.RESET);
            prevButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wiki prev"));
            prevButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("PrevPage"))).create()
            ));
            navigation.addExtra(prevButton);
        }

        TextComponent pageInfo = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getMessage("Page") + " &e" + page + " / " + totalPages + " "));

        navigation.addExtra(pageInfo);

        if (page < totalPages) {
            TextComponent nextButton = new TextComponent(ChatColor.BLUE + "▶" + ChatColor.RESET);
            nextButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wiki next"));
            nextButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
                            messageManager.getMessage("NextPage"))).create()
            ));
            navigation.addExtra(nextButton);
        }
        player.spigot().sendMessage(navigation);
    }

}