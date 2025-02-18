package net.azisaba.albardoo02.Command;

import net.azisaba.albardoo02.AzipediaSearcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WikiCommandTabComplete implements TabCompleter {

    AzipediaSearcher plugin;
    public WikiCommandTabComplete(AzipediaSearcher plugin){
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        List<String> completions = new ArrayList<>();
        List<String> categoryList = plugin.getConfig().getStringList("Category");
        categoryList = categoryList.stream().map(s -> "Category=" + s).collect(Collectors.toList());

        if (args.length == 1) {
            completions.add("search");
            completions.add("help");
            completions.add("version");
            completions.add("config");
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("config")) {
                completions.add("reload");
            } else if (args[0].equalsIgnoreCase("search")) {
                completions.add("<検索ワード>");
            }
            return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("search")) {
                completions.add("<検索ワード>");
                completions.add("SearchRange=Content");
                completions.add("SearchRange=Title");
                completions.add("Category=");
                completions.addAll(categoryList);
            }
        } else if (args.length >= 4) {
            if (args[0].equalsIgnoreCase("search")) {
                String lastArg = args[args.length - 1].toLowerCase();

                if (lastArg.startsWith("SearchType=") || lastArg.equalsIgnoreCase("SearchType=")) {
                    completions.add("SearchType=AND");
                    completions.add("SearchType=OR");
                }
                if (lastArg.startsWith("SearchRange=") || lastArg.equalsIgnoreCase("SearchRange=")) {
                    completions.add("SearchRange=Content");
                    completions.add("SearchRange=Title");
                }
                if (lastArg.startsWith("Category=") || lastArg.equalsIgnoreCase("Category=")) {
                    completions.addAll(categoryList);
                }

                if (Arrays.stream(args).noneMatch(arg -> arg.startsWith("SearchType="))) {
                    completions.add("SearchType=AND");
                    completions.add("SearchType=OR");
                }
                if (Arrays.stream(args).noneMatch(arg -> arg.startsWith("SearchRange="))) {
                    completions.add("SearchRange=Content");
                    completions.add("SearchRange=Title");
                }
                if (Arrays.stream(args).noneMatch(arg -> arg.startsWith("Category="))) {
                    completions.add("Category=");
                    completions.addAll(categoryList);
                }
            }
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
    }
}
