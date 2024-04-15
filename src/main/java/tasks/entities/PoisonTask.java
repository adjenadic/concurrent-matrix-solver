package tasks.entities;

import matrices.entities.Matrix;

import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class PoisonTask extends Task {
    public PoisonTask() {
        super(TaskType.POISON);
    }

    @Override
    public Future<Matrix> initiate(RecursiveTask<?> task) {
        return null;
    }
}
