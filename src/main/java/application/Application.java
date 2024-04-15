package application;

import matrices.MatrixBrain;
import matrices.entities.Matrix;
import tasks.TaskCoordinator;
import tasks.entities.MultiplyTask;
import tasks.entities.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;

import static matrices.MatrixBrain.matrixMap;

public class Application {
    // QUEUES, LISTS
    public static final CopyOnWriteArrayList<String> dirsToExplore = new CopyOnWriteArrayList<>();
    // SINGLE THREADS
    public static final SystemExplorer systemExplorer = new SystemExplorer(dirsToExplore);
    public static final TaskCoordinator taskCoordinator = new TaskCoordinator();
    public static BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
    // THREAD POOLS
    public static ForkJoinPool matrixExtractorPool = new ForkJoinPool();
    public static ForkJoinPool matrixMultiplierPool = new ForkJoinPool();
    public static MatrixBrain matrixBrain = new MatrixBrain();

    public void start() {
        PropertiesConfigurator.getInstance().loadProperties();
        dirsToExplore.add(PropertiesConfigurator.getInstance().getStartDir());

        systemExplorer.start();
        taskCoordinator.start();
        matrixBrain.observe();

        startCli();
    }

    private void startCli() {
        Scanner scanner = new Scanner(System.in);
        String ln;
        String[] lnSplit;

        while (true) {
            ln = scanner.nextLine().trim();
            if (ln.isEmpty()) {
                continue;
            }

            lnSplit = ln.split(" ");
            switch (lnSplit[0]) {
                case "dir": {
                    if (lnSplit.length != 2) {
                        System.err.println("APPLICATION: Invalid number of arguments.");
                        System.err.println("APPLICATION: Proper use: `dir <dir_name>`.");
                        continue;
                    }
                    System.out.println("APPLICATION: Adding directory to scan: " + lnSplit[1]);
                    dirsToExplore.add(lnSplit[1]);
                    break;
                }
                case "info": {
                    boolean all = false;
                    char sortType = 'n';
                    int sNum = 0;
                    int eNum = 0;

                    if (lnSplit.length < 2 || lnSplit.length > 7) {
                        System.err.println("APPLICATION: Invalid number of arguments.");
                        System.err.println("APPLICATION: Proper use: `info <matrix_name>` (`-all`, `-asc`, `-desc`, `-s <N>`, `-e <N>`).");
                        continue;
                    }
                    if (Arrays.asList(lnSplit).contains("-all")) {
                        all = true;
                    }
                    if (Arrays.asList(lnSplit).contains("-asc")) {
                        sortType = 'a';
                    }
                    if (Arrays.asList(lnSplit).contains("-desc")) {
                        sortType = 'd';
                    }
                    if (Arrays.asList(lnSplit).contains("-s")) {
                        int index = Arrays.asList(lnSplit).indexOf("-s") + 1;
                        sNum = Integer.parseInt(Arrays.asList(lnSplit).get(index));
                    }
                    if (Arrays.asList(lnSplit).contains("-e")) {
                        int index = Arrays.asList(lnSplit).indexOf("-e") + 1;
                        eNum = Integer.parseInt(Arrays.asList(lnSplit).get(index));
                    }
                    if (matrixMap.containsKey(lnSplit[1].trim()) && lnSplit.length == 2) {
                        matrixBrain.info(lnSplit[1].trim(), false, 'n', 0, 0);
                        break;
                    }
                    matrixBrain.info(null, all, sortType, sNum, eNum);
                    break;
                }
                case "multiply": {
                    boolean async = false;
                    String name;

                    if (lnSplit.length < 2 || lnSplit.length > 5) {
                        System.err.println("APPLICATION: Invalid number of arguments.");
                        System.err.println("APPLICATION: Proper use: `multiply <mat1>,<mat2>` (`-async`, `-name <matrix_name>`).");
                        continue;
                    }
                    String[] matNames = lnSplit[1].split(",");
                    if (!matrixMap.containsKey(matNames[0]) || !matrixMap.containsKey(matNames[1])) {
                        System.err.println("APPLICATION: Invalid input.");
                        System.err.println("APPLICATION: Proper use: `multiply <mat1>,<mat2>` (`-async`, `-name <matrix_name>`).");
                        continue;
                    }
                    if (Arrays.asList(lnSplit).contains("-async")) {
                        async = true;
                    }
                    name = matNames[0] + matNames[1];
                    if (Arrays.asList(lnSplit).contains("-name")) {
                        int index = Arrays.asList(lnSplit).indexOf("-name") + 1;
                        name = Arrays.asList(lnSplit).get(index);
                    }
                    try {
                        taskQueue.put(new MultiplyTask(matrixMap.get(matNames[0]), matrixMap.get(matNames[1]), async, name));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case "save": {
                    if (lnSplit.length != 5) {
                        System.err.println("APPLICATION: Invalid number of arguments.");
                        System.err.println("APPLICATION: Proper use: `save -name <mat_name> -file <file_name>`.");
                        continue;
                    }
                    if (!matrixMap.containsKey(lnSplit[2].trim()) || !lnSplit[4].endsWith(".rix")) {
                        System.err.println("APPLICATION: Invalid input.");
                        System.err.println("APPLICATION: Proper use: `save -name <mat_name> -file <file_name>`.");
                        continue;
                    }
                    matrixBrain.save(lnSplit[2], lnSplit[4]);
                    break;
                }
                case "clear": {
                    if (lnSplit.length != 2) {
                        System.err.println("APPLICATION: Invalid number of arguments.");
                        System.err.println("APPLICATION: Proper use: `clear <mat_name>`, `clear <file_name>`.");
                        continue;
                    }
                    List<Matrix> matrixList = new ArrayList<>(matrixMap.values());
                    boolean flag = false;
                    for (Matrix matrix : matrixList) {
                        try {
                            if (matrix.name.equals(lnSplit[1].trim())) {
                                matrixBrain.clear(lnSplit[1].trim(), true);
                                flag = true;
                                break;
                            } else if (matrix.filePath.endsWith(lnSplit[1].trim())) {
                                matrixBrain.clear(lnSplit[1].trim(), false);
                                flag = true;
                                break;
                            }
                        } catch (NullPointerException e) {
                            System.err.println("APPLICATION: Invalid name or file path.");
                        }
                    }
                    if (!flag) {
                        System.err.println("APPLICATION: Invalid input.");
                        System.err.println("APPLICATION: Proper use: `clear <mat_name>`, `clear <file_name>`.");
                    }
                    break;
                }
                case "stop": {
                    scanner.close();
                    terminate();
                    System.err.println("APPLICATION: Terminated.");
                    return;
                }
                case "help": {
                    System.out.println("APPLICATION: `dir <dir_name>`");
                    System.out.println("APPLICATION: `info <matrix_name>` (`-all`, `-asc`, `-desc`, `-s <N>`, `-e <N>`)");
                    System.out.println("APPLICATION: `multiply <mat1>,<mat2>` (`-async`, `-name <matrix_name>`)");
                    System.out.println("APPLICATION: `save -name <mat_name> -file <file_name>`");
                    System.out.println("APPLICATION: `clear <mat_name>`, `clear <file_name>`");
                    System.out.println("APPLICATION: `stop`");
                    break;
                }
                default: {
                    System.err.println("APPLICATION: Unknown command.");
                    System.err.println("APPLICATION: Type `help` for the command listing.");
                    break;
                }
            }
        }
    }

    private void terminate() {
        systemExplorer.terminate();
        matrixBrain.terminate();
    }
}
