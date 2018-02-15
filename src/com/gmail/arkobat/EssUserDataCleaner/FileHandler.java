package com.gmail.arkobat.EssUserDataCleaner;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;

public class FileHandler {


    private final Main main;

    public FileHandler(Main main) {
        this.main = main;
    }

    private Boolean stop = false;
    private File usermap;

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
        File folder = new File("plugins\\Essentials\\userdata");
        File[] listOfFiles = folder.listFiles();
        usermap = new File("plugins\\Essentials\\usermap.csv");
        makeFolder();
                int checkedFiles = 0;
                int shouldDelete = 0;
                int deletedFiles = 0;
                if (listOfFiles != null) {
                    if (listOfFiles.length >= 1) {
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
                }
                Bukkit.getServer().getConsoleSender().sendMessage("Checked files: " + checkedFiles);
                Bukkit.getServer().getConsoleSender().sendMessage("Files sent to deletion: " + shouldDelete);
                Bukkit.getServer().getConsoleSender().sendMessage("Files successfully deleted: " + deletedFiles);
            }
        }.runTaskAsynchronously(this.main);
    }

    private boolean checkFile(File file) {
        if (compareSize(file)) {
            if (lastModified(file)) {
                if (cantContain(file)) {
                    if (checkUsermap(file)) {
                        return true;
                    }
                } else if (isFileEmpty (file)) {
                    if (checkUsermap(file)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean compareSize(File file) {
        return (file.length() <= main.maxSize);
    }

    private boolean cantContain(File file) {
        try {
            Boolean lineChecked = false;
            Scanner scanner = new Scanner(file);
            List<String> cantContain = main.cantContain;
            while (scanner.hasNextLine()) {
                lineChecked = true;
                String line = scanner.nextLine();
                if (cantContain.contains(line)) {
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

    private boolean checkUsermap(File file) {
       if (main.compareUsermap) {
           try {
               Boolean lineChecked = false;
               Scanner scanner = new Scanner(usermap);
               String uuid = getFileName(file);
               while (scanner.hasNextLine()) {
                   lineChecked = true;
                   String line = scanner.nextLine();
                   if (line.contains(uuid)) {
                       return false;
                   }
               }
               scanner.close();
               if (lineChecked) {
                   return true;
               }
           } catch (FileNotFoundException ignored) {
               return false;
           }
           return false;
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
                Bukkit.getServer().getConsoleSender().sendMessage("Error in settings. Check 'todo'");
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
            if (file.renameTo(new File(main.getDataFolder() + File.separator + "userdata\\" + file.getName()))) {
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
