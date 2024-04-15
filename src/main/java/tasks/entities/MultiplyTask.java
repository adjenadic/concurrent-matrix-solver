package tasks.entities;

import application.Application;
import matrices.entities.Matrix;

import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class MultiplyTask extends Task {
    public Matrix matrix1, matrix2;
    public boolean isAsync;
    public String name;

    public MultiplyTask(Matrix matrix1, Matrix matrix2, boolean isAsync, String name) {
        super(TaskType.MULTIPLY);
        this.matrix1 = matrix1;
        this.matrix2 = matrix2;
        this.isAsync = isAsync;
        this.name = name;
    }

    @Override
    public Future<Matrix> initiate(RecursiveTask task) {
        return Application.matrixMultiplierPool.submit(task);
    }
}
