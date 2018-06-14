package com.gmail.arkobat.EssUserDataCleaner;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FileHandler {

    private final Main main;

    public FileHandler(Main main) {
        this.main = main;
    }

    private Boolean stop = false;
    private File usermap;
    private List<String> usermapList = new ArrayList<>();

    public void source(String source) {
        switch (source) {
            case "start":
                if (main.when.equalsIgnoreCase("start")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            while (!stop) {
                                checkFiles();
                                stop = true;
                            }
                        }
                    }.runTaskTimer(Main.getPlugin(Main.class), main.delay * 20, 100);
                }
                break;
            case "stop":
                if (main.when.equalsIgnoreCase("stop")) {
                    checkFiles();
                }
                break;
        }
    }

    public void checkFiles() {
        new BukkitRunnable() {
            @Override
            public void run() {
                debug("Starting a purge");
                File folder = new File(main.getDataFolder() + File.separator + ".." + File.separator + "Essentials" + File.separator + "userdata");
                debug("Userdata folder path = " + main.getDataFolder() + File.separator + ".." + File.separator + "Essentials" + File.separator + "userdata");
                File[] listOfFiles = folder.listFiles();
                usermap = new File(main.getDataFolder() + File.separator + ".." + File.separator + "Essentials" + File.separator + "usermap.csv");
                debug("Defined files");
                makeFolder();
                loadUsermap(usermap);
                int checkedFiles = 0;
                int shouldDelete = 0;
                int deletedFiles = 0;
                if (listOfFiles != null) {
                    if (listOfFiles.length >= 1) {
                        debug("Found " + listOfFiles.length + " files to handle");
                        debug("Starting to run files");
                        for (File file : listOfFiles) {
                            checkedFiles++;
                            if (checkFile(file)) {
                                shouldDelete++;
                                if (actionFile(file)) {
                                    deletedFiles++;
                                }
                            }

                        }
                    }
                } else {
                    debug("List of files == null");
                }
                Bukkit.getServer().getConsoleSender().sendMessage("Checked files: " + checkedFiles);
                Bukkit.getServer().getConsoleSender().sendMessage("Files sent to deletion: " + shouldDelete);
                Bukkit.getServer().getConsoleSender().sendMessage("Files successfully deleted: " + deletedFiles);
            }
        }.runTaskAsynchronously(this.main);
    }

    private void debug(String message) {
        if (main.debug) {
            Bukkit.getServer().getConsoleSender().sendMessage("§aEssClean DEBUG: §r" + message);
        }
    }

    private boolean checkFile(File file) {
        if (compareSize(file)) {
            if (lastModified(file)) {
                if (checkUsermap(file)) {
                    if (cantContain(file)) {
                        return true;
                    } else if (isFileEmpty(file)) {
                        return true;
                    }

                }
            }
        }
        return false;
    }

    private boolean compareSize(File file) {
        if (main.maxSize == -1) {
            return false;
        } else {
            return (file.length() <= main.maxSize);
        }
    }

    private boolean cantContain(File file) {
        if (main.cantContain.size() == 1) {
            if (main.cantContain.get(0).equalsIgnoreCase("disabled")) {
                return false;
            }
        }
        try {
            Boolean lineChecked = false;
            Scanner scanner = new Scanner(file);
            CharSequence[] cs = main.cantContain.toArray(new CharSequence[main.cantContain.size()]);
            while (scanner.hasNextLine()) {
                lineChecked = true;
                String line = scanner.nextLine();
                if (Arrays.stream(cs).anyMatch(line::contains)) {
                    debug("Files contains illegal lines");
                	scanner.close();
                    return false;
                }
            }
            scanner.close();
            if (lineChecked) {
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private boolean isFileEmpty(File file) {
        try {
            Scanner scanner = new Scanner(file);
            if (!scanner.hasNextLine()) {
                if (file.length() == 0) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void loadUsermap(File file) {
        if (main.compareUsermap) {
           usermapList.clear();
            debug("Starting to scan usermap.cvs");
            try {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    usermapList.add(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkUsermap(File file) {
        if (main.compareUsermap) {
            return !usermapList.contains(getFileName(file));
        } else {
            return true;
        }
    }

    private String getFileName(File file) {
        String[] fileName = file.getName().split("\\.");
        if (fileName.length >= 1) {
            return fileName[0];
        } else {
            return "..Unknow..";
        }

    }

    private boolean lastModified(File file) {
        if (main.lastModified == -1) {
            return true;
        }
        long daysSince = (Instant.now().getEpochSecond() * 1000 - file.lastModified()) / 24 / 60 / 1000;
        return (daysSince > main.lastModified);
    }

    private boolean actionFile(File file) {
        switch (main.todo) {
            case "move":
                return moveFile(file);
            case "delete":
                return deleteFile(file);
            default:
                Bukkit.getServer().getConsoleSender().sendMessage("§aEssClean: §cError in settings. Check 'todo'");
                return false;
        }
    }

    private boolean deleteFile(File file) {
        try {
            if (file.delete()) {
                if (main.inform) {
                    Bukkit.getServer().getConsoleSender().sendMessage("Deleted essential userdata file " + file.getName());
                    return true;
                }
            } else {
                if (main.inform) {
                    Bukkit.getServer().getConsoleSender().sendMessage("Tried to delete file " + file.getName() + " but failed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean moveFile(File file) {
        try {
            if (file.renameTo(new File(main.getDataFolder() + File.separator + "userdata" + File.separator + file.getName()))) {
                if (main.inform) {
                    Bukkit.getServer().getConsoleSender().sendMessage("Moved essential userdata file " + file.getName());
                    return true;
                }
            } else {
                if (main.inform) {
                    Bukkit.getServer().getConsoleSender().sendMessage("Tried to move file " + file.getName() + " but failed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void makeFolder() {
        new File(main.getDataFolder() + File.separator + "userdata").mkdir();
    }
}
