package matrices;

import application.Application;
import application.PropertiesConfigurator;
import lombok.Getter;
import matrices.entities.Matrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MatrixBrain {
    public static ConcurrentHashMap<String, Matrix> matrixMap = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, Future<Matrix>> futureMatrixMap = new ConcurrentHashMap<>();
    public ExecutorService executorService = Executors.newCachedThreadPool();
    private boolean running = true;

    public void observe() {
        executorService.submit(() -> {
            while (running) {
                for (var futureMatrix : futureMatrixMap.entrySet()) {
                    try {
                        if (futureMatrix.getValue().isDone() && futureMatrix.getValue().get() != null) {
                            matrixMap.put(futureMatrix.getValue().get().name, futureMatrix.getValue().get());
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public void info(String matrixName, boolean all, char sortType, int sNum, int eNum) {
        if (matrixName != null) {
            Matrix matrix = matrixMap.get(matrixName);
            System.out.println("matrix_name=" + matrix.name + ", rows=" + matrix.numRows + ", cols=" + matrix.numCols);
            System.out.println("file_path=" + matrix.filePath);
        } else {
            List<Matrix> matrixList = new ArrayList<>(matrixMap.values());
            List<Matrix> matrixToPrintList = new ArrayList<>();
            Comparator<Matrix> matrixComparator = Comparator.comparing(Matrix::getNumRows)
                    .thenComparing(Matrix::getNumCols);
            if (all) {
                matrixToPrintList = new ArrayList<>(matrixMap.values());
            }
            if (sNum > 0) {
                matrixToPrintList = new ArrayList<>();
                for (int i = 0; i <= sNum; i++) {
                    matrixToPrintList.add(matrixList.get(i));
                }
            }
            if (eNum > 0) {
                if (eNum > matrixList.size()) {
                    eNum = matrixList.size();
                }
                matrixToPrintList = new ArrayList<>();
                for (int i = matrixList.size() - 1; i >= (matrixList.size() - eNum); i--) {
                    matrixToPrintList.add(matrixList.get(i));
                }
            }
            if (!matrixToPrintList.isEmpty()) {
                switch (sortType) {
                    case 'a': {
                        matrixToPrintList.sort(matrixComparator);
                        break;
                    }
                    case 'd': {
                        matrixToPrintList.sort(matrixComparator.reversed());
                        break;
                    }
                }
            } else {
                System.err.println("APPLICATION: The output with the given arguments is blank.");
                return;
            }
            for (Matrix matrix : matrixToPrintList) {
                System.out.println("matrix_name=" + matrix.name + ", rows=" + matrix.numRows + ", cols=" + matrix.numCols);
                System.out.println("file_path=" + matrix.filePath);
            }
        }
    }

    public void save(String matrixName, String fileName) {
        executorService.submit(() -> {
            Matrix matrix = matrixMap.get(matrixName);
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(PropertiesConfigurator.getInstance().getSaveDir() + fileName))) {
                writer.write(matrix.toString());
                matrix.filePath = PropertiesConfigurator.getInstance().getSaveDir() + "resources/data/" + fileName;
                System.out.println("APPLICATION/MatrixBrain: " + fileName + " (" + matrixName + ") has been successfully saved into the specified directory.");
            } catch (IOException e) {
                System.err.println("APPLICATION/MatrixBrain: An error has occurred while writing the matrix into the file.");
            }
        });
    }

    public void clear(String name, boolean isMatrixName) {
        if (isMatrixName) {
            try {
                for (var file : Application.systemExplorer.getLastModifiedMap().entrySet()) {
                    if (matrixMap.get(name).filePath.endsWith(file.getKey())) {
                        Application.systemExplorer.getLastModifiedMap().remove(file.getKey());
                        break;
                    }
                }
            } catch (NullPointerException e) {
                System.err.println("APPLICATION: The specified matrix does not have a saved \".rix\" file.");
            }

            for (var futureMatrix : futureMatrixMap.entrySet()) {
                if (futureMatrix.getKey().equals(name) || futureMatrix.getKey().endsWith(name)) {
                    futureMatrixMap.remove(futureMatrix.getKey());
                    break;
                }
            }

            for (var matrix : matrixMap.entrySet()) {
                if (matrix.getKey().equals(name) || matrix.getKey().endsWith(name)) {
                    matrixMap.remove(matrix.getKey());
                    break;
                }
            }
        } else {
            for (var file : Application.systemExplorer.getLastModifiedMap().entrySet()) {
                if (file.getKey().equals(name) || file.getKey().equals(name + ".rix")) { // Covers both types of inputs
                    Application.systemExplorer.getLastModifiedMap().remove(file.getKey());
                    break;
                }
            }

            for (var futureMatrix : futureMatrixMap.entrySet()) {
                try {
                    if (futureMatrix.getValue().get().filePath.endsWith(name)) { // Checks if the future matrix filepath ends with the given name
                        futureMatrixMap.remove(futureMatrix.getKey());
                        break;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

            for (var matrix : matrixMap.entrySet()) {
                if (matrix.getValue().filePath.endsWith(name)) {
                    matrixMap.remove(matrix.getKey());
                    break;
                }
            }
        }

        System.out.println("APPLICATION/MatrixBrain: Matrix " + name + " has been cleared.");
    }

    public void terminate() {
        System.err.println("APPLICATION: Terminating MatrixBrain...");
        running = false;
    }
}
