package application;

import lombok.Getter;
import tasks.entities.CreateTask;
import tasks.entities.PoisonTask;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SystemExplorer extends Thread {
    private final CopyOnWriteArrayList<String> dirsToExplore;
    private final HashMap<String, Boolean> exploredFiles;
    @Getter
    private final HashMap<String, Long> lastModifiedMap;
    private boolean running;

    public SystemExplorer(CopyOnWriteArrayList<String> dirsToExplore) {
        this.dirsToExplore = dirsToExplore;
        exploredFiles = new HashMap<>();
        lastModifiedMap = new HashMap<>();
        running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                for (String dir : dirsToExplore) {
                    explore(new File(dir), dir);
                }
                Thread.sleep(PropertiesConfigurator.getInstance().getSysExplorerSleepTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void explore(File inputFile, String path) throws InterruptedException {
        File[] listFiles = inputFile.listFiles();

        if (listFiles == null) {
            System.err.println("APPLICATION/SystemExplorer: The given directory cannot be found or opened.");
            dirsToExplore.remove(path);
            return;
        }

        for (File file : listFiles) {
            if (file.isDirectory()) {
                addTaskToQueue(file);
                explore(file, path);
            } else if (file.isFile() && file.getName().endsWith(PropertiesConfigurator.getInstance().getFileExtension())) {
                addTaskToQueue(file);
            }
        }
    }

    private void addTaskToQueue(File file) throws InterruptedException {
        exploredFiles.clear();
        if (lastModifiedMap.getOrDefault(file.getName(), 0L) != file.lastModified()) {
            lastModifiedMap.put(file.getName(), file.lastModified());
            if (!exploredFiles.containsKey(file.getName())) {
                exploredFiles.put(file.getName(), true);
                Application.taskQueue.put(new CreateTask(file.getAbsolutePath(), file.getName()));
                System.out.println("APPLICATION/SystemExplorer: \"" + file.getName() + "\" sent to TaskScheduler.");
            }
        }
    }

    public void terminate() {
        try {
            Application.taskQueue.put(new PoisonTask());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.err.println("APPLICATION: Terminating SystemExplorer...");
        running = false;
    }
}
