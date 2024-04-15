package tasks.entities;

import application.Application;
import matrices.entities.Matrix;

import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class CreateTask extends Task {
    public final String filePath;
    public final String fileName;

    public CreateTask(String filePath, String fileName) {
        super(TaskType.CREATE);
        this.filePath = filePath;
        this.fileName = fileName;
    }

    @Override
    public Future<Matrix> initiate(RecursiveTask task) {
        return Application.matrixExtractorPool.submit(task);
    }
}
