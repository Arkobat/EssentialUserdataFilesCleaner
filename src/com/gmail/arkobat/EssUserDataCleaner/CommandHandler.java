package com.gmail.arkobat.EssUserDataCleaner;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor {

    private final Main main;
    private final FileHandler fileHandler;
    private List<Player> confirmingP = new ArrayList<>();
    private List<ConsoleCommandSender> confirmingCmd = new ArrayList<>();

    public CommandHandler(Main main, FileHandler fileHandler) {
        this.main = main;
        this.fileHandler = fileHandler;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("essclean")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (p != null) {
                    if (p.hasPermission("EssUserDataCleaner.use")) {
                        if (!main.consoleOnly) {
                            if (args.length == 1) {
                                if (args[0].equalsIgnoreCase("purge")) {
                                    if (!confirmingP.contains(p)) {
                                        p.sendMessage("Are you sure you want to move/delete invalid userdata files.");
                                        p.sendMessage("Be sure you know what to are doing.");
                                        p.sendMessage("To contuine, type '/esscealn confirm' within 30 seconds");
                                        p.sendMessage("To cancel please type '/essclean cancel'");
                                        addList(p, null);
                                    } else {
                                        p.sendMessage("You are already awaiting confirmation'");
                                    }
                                    return true;
                                } else if (args[0].equalsIgnoreCase("confirm")) {
                                    if (confirmingP.contains(p)) {
                                        confirmingP.remove(p);
                                        fileHandler.checkFiles();
                                        return true;
                                    } else {
                                        p.sendMessage("You must first use /essclean purge");
                                        return true;
                                    }
                                } else if (args[0].equalsIgnoreCase("cancel")) {
                                    if (confirmingP.contains(p)) {
                                        confirmingP.remove(p);
                                        return true;
                                    } else {
                                        p.sendMessage("EssClean purge canceled");
                                        p.sendMessage("Couldn't find anything to cancel");
                                        return true;
                                    }
                                }
                            }
                        } else {
                            p.sendMessage("Commands can only be executed from Console");
                            return true;
                        }
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.noPermMsg));
                        return true;
                    }
                }
            } else if (sender instanceof ConsoleCommandSender) {
                ConsoleCommandSender p = (ConsoleCommandSender) sender;
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("purge")) {
                        if (!confirmingCmd.contains(p)) {
                            p.sendMessage("Are you sure you want to move/delete invalid userdata files.");
                            p.sendMessage("Be sure you know what to are doing.");
                            p.sendMessage("To contuine, type '/esscealn confirm' within 30 seconds");
                            p.sendMessage("To cancel please type '/essclean cancel'");
                            addList(null, p);
                        } else {
                                p.sendMessage("You are already awaiting confirmation'");
                            }
                        return true;
                    } else if (args[0].equalsIgnoreCase("confirm")) {
                        if (confirmingCmd.contains(p)) {
                            confirmingCmd.remove(p);
                            fileHandler.checkFiles();
                            return true;
                        } else {
                            p.sendMessage("You must first use /essclean purge");
                            return true;
                        }
                    } else if (args[0].equalsIgnoreCase("cancel")) {
                        if (confirmingCmd.contains(p)) {
                            confirmingCmd.remove(p);
                            p.sendMessage("EssClean purge canceled");
                            return true;
                        } else {
                            p.sendMessage("Couldn't find anything to cancel");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void addList(Player player, ConsoleCommandSender console) {
        if (player != null) {
            confirmingP.add(player);
        } else if (console != null) {
            confirmingCmd.add(console);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player != null) {
                    if (confirmingP.contains(player)) {
                        confirmingP.remove(player);
                    }
                } else if (console != null) {
                    if (confirmingCmd.contains(console)) {
                        confirmingCmd.remove(console);
                    }
                }
                return;
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 600, 0);
    }
}