package tasks.entities;

import lombok.Getter;
import matrices.entities.Matrix;

import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

@Getter
public abstract class Task {
    private final TaskType taskType;

    public Task(TaskType taskType) {
        this.taskType = taskType;
    }

    public abstract Future<Matrix> initiate(RecursiveTask<?> task);
}

