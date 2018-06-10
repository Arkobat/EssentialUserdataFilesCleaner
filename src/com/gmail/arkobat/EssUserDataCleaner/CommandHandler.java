package com.gmail.arkobat.EssUserDataCleaner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor {

    private final Main main;
    private final FileHandler fileHandler;
    private List<CommandSender> confirming = new ArrayList<>();

    public CommandHandler(Main main, FileHandler fileHandler) {
        this.main = main;
        this.fileHandler = fileHandler;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("essclean")) {
            if (preChecks(sender)) {
                if (args.length == 1) {
                    switch (args[0].toLowerCase()) {
                        case "purge":
                            return purge(sender);
                        case "confirm":
                            return confirm(sender);
                        case "cancel":
                            return cancel(sender);
                        case "debug":
                            return debug(sender);
                        case "reload":
                            return reload(sender);
                        default:
                            return false;
                    }
                } else return false;
            }
        }
        return true;
    }

    private boolean preChecks (CommandSender sender) {
        Player p = sender instanceof Player ? (Player) sender: null;
        boolean enterSwitch = false;
        if (p != null) {
            if (p.hasPermission("EssUserDataCleaner.use")) {
                if (!main.consoleOnly) {
                    enterSwitch = true;
                } else {
                    p.sendMessage("§cCommands can only be executed from console");
                }
            } else {
                p.sendMessage(main.noPermMsg);
            }
        } else {
            enterSwitch = true;
        }
        return enterSwitch;
    }

    private boolean purge(CommandSender p) {
            if (!confirming.contains(p)) {
                p.sendMessage("§cAre you sure you want to move/delete invalid userdata files.");
                p.sendMessage("§cBe sure you know what to are doing.");
                p.sendMessage("§cTo continue, type '§b/essclean confirm§c' within 30 seconds");
                p.sendMessage("§cTo cancel please type '§b/essclean cancel§c'");
                addList(p);
            } else {
                p.sendMessage("§cYou are already awaiting confirmation'");
            }
        return true;
    }

    private boolean confirm(CommandSender p) {
            if (confirming.contains(p)) {
                confirming.remove(p);
                p.sendMessage("§cStarting to purge");
                fileHandler.checkFiles();
            } else {
                p.sendMessage("§cYou must first use §b/essclean purge");
            }
        return true;
    }

    private boolean cancel(CommandSender p) {
            if (confirming.contains(p)) {
                confirming.remove(p);
                p.sendMessage("§cEssClean purge canceled");
            } else {
                p.sendMessage("§cCouldn't find anything to cancel");
            }
        return true;
    }

    private boolean debug(CommandSender p) {
            if (!main.debug) {
                p.sendMessage("§aEnabling debug (to console)");
                main.debug = true;
            } else if (main.debug) {
                main.debug = false;
                p.sendMessage("§cDisabling debug");
            }
        return true;
    }

    private boolean reload(CommandSender p) {
        main.loadDefaultConfig();
        p.sendMessage("§cReloaded the config");
        return true;
    }

    private void addList(CommandSender player) {
            confirming.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                    if (confirming.contains(player)) {
                        confirming.remove(player);
                }
                return;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 600, 0);
    }
}
