package tasks;

import application.Application;
import matrices.MatrixExtractor;
import matrices.MatrixMultiplier;
import matrices.entities.Matrix;
import tasks.entities.CreateTask;
import tasks.entities.MultiplyTask;
import tasks.entities.Task;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static application.Application.matrixBrain;

public class TaskCoordinator extends Thread {
    private boolean running = true;

    @Override
    public void run() {
        while (running) {
            try {
                Task task = Application.taskQueue.take();
                switch (task.getTaskType()) {
                    case CREATE -> {
                        Future<Matrix> matrixFuture = task.initiate(new MatrixExtractor(new File(((CreateTask) task).filePath)));
                        System.out.println("APPLICATION/TaskCoordinator: \"" + ((CreateTask) task).fileName + "\" has been submitted to matrixExtractorPool.");
                        Matrix matrix;
                        try {
                            matrix = matrixFuture.get();
                            matrixBrain.getFutureMatrixMap().put(matrix.name, matrixFuture);
                            System.out.println("APPLICATION/TaskCoordinator: \"" + matrix.name + "\" has been added to the matrix map.");
                        } catch (NullPointerException e) {
                            System.err.println("APPLICATION/TaskCoordinator: An error occurred while adding \"" + ((CreateTask) task).fileName + "\" into the matrix map.");
                            continue;
                        }

                        Future<Matrix> matrixSquaredFuture = task.initiate(new MatrixMultiplier(matrix, matrix, matrix.name + matrix.name));
                        Matrix matrixSquared;
                        try {
                            matrixSquared = matrixSquaredFuture.get();
                            if (matrixSquared != null) {
                                System.out.println("APPLICATION/TaskCoordinator: \"" + matrix.name + "\" is squarable.");
                            } else {
                                System.out.println("APPLICATION/TaskCoordinator: \"" + matrix.name + "\" is not squarable.");
                            }
                        } catch (NullPointerException e) {
                            System.err.println("APPLICATION/TaskCoordinator: An error occurred.");
                        }
                    }
                    case MULTIPLY -> {
                        if (((MultiplyTask) task).matrix1.numRows != ((MultiplyTask) task).matrix2.numCols) {
                            System.err.println("APPLICATION/MatrixMultiplier: Matrix \"" + ((MultiplyTask) task).matrix1.name + "\" cannot be multiplied with matrix \"" + ((MultiplyTask) task).matrix2.name + "\" (m1.numRows [" + ((MultiplyTask) task).matrix1.numRows + "] != m2.numCols [" + ((MultiplyTask) task).matrix2.numCols + "]).");
                            break;
                        }
                        if ((matrixBrain.getFutureMatrixMap().containsKey(((MultiplyTask) task).name))) {
                            System.err.println("APPLICATION/TaskCoordinator: The inputted multiplication has previously already been completed and logged.");
                        } else {
                            Matrix matrix;
                            if (((MultiplyTask) task).isAsync) {
                                Future<Matrix> matrixFuture = task.initiate(new MatrixMultiplier(((MultiplyTask) task).matrix1, ((MultiplyTask) task).matrix2, ((MultiplyTask) task).name));
                                matrixBrain.getFutureMatrixMap().put(((MultiplyTask) task).name, matrixFuture);
                            } else {
                                if ((matrixBrain.getFutureMatrixMap().containsKey(((MultiplyTask) task).name))) {
                                    System.err.println("APPLICATION/TaskCoordinator: The inputted multiplication is already being calculated.");
                                }
                                Future<Matrix> matrixFuture = task.initiate(new MatrixMultiplier(((MultiplyTask) task).matrix1, ((MultiplyTask) task).matrix2, ((MultiplyTask) task).name));
                                matrixBrain.getFutureMatrixMap().put(((MultiplyTask) task).name, matrixFuture);

                                try {
                                    // Wait for the matrix calculation to complete and retrieve the result
                                    // This line blocks the current thread until the calculation is finished to ensure synchronous execution
                                    matrix = matrixFuture.get();
                                } catch (ExecutionException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            System.out.println("APPLICATION/TaskCoordinator: The following multiplication has been calculated: " + ((MultiplyTask) task).matrix1.name + " x " + ((MultiplyTask) task).matrix2.name + " = " + ((MultiplyTask) task).name + ".");
                        }
                    }
                    case POISON -> terminate();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void terminate() {
        System.err.println("APPLICATION: Terminating TaskCoordinator...");

        Application.matrixExtractorPool.shutdown();

        if (!Application.matrixMultiplierPool.isQuiescent()) {
            Application.matrixMultiplierPool.shutdown();
        }

        matrixBrain.executorService.shutdown();

        running = false;
    }
}

