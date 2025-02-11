package net.azisaba.albardoo02.Command;

import net.azisaba.albardoo02.AzipediaSearcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WikiCommandTabComplete implements TabCompleter {

    AzipediaSearcher plugin;
    public WikiCommandTabComplete(AzipediaSearcher plugin){
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("search", "help", "reload", "version"));
        } else if (args.length == 2) {
            completions.add("<検索する単語>");
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equalsIgnoreCase("search") || subCommand.equalsIgnoreCase("s")) {
                completions.add("<検索する単語>");
                completions.add("limit=text");
                completions.add("limit=title");
            }
        } else if (args.length >= 4) {
            String lastArg = args[args.length -1].toLowerCase();

            completions.add("<検索する単語>");

            if (lastArg.startsWith("type=") || lastArg.equals("type")) {
                completions.add("type=and");
                completions.add("type=or");
            } else if (lastArg.startsWith("limit=") || lastArg.equals("limit")) {
                completions.add("limit=text");
                completions.add("limit=title");
            } else {
                if (!Arrays.asList(args).contains("type=and") && !Arrays.asList(args).contains("type=or")) {
                    completions.add("type=and");
                    completions.add("type=or");
                }
                if (!Arrays.asList(args).contains("limit=text") && !Arrays.asList(args).contains("limit=title")) {
                    completions.add("limit=text");
                    completions.add("limit=title");
                }
            }
        }
        return completions;
    }
}
