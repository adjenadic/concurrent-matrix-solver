package matrices;

import application.PropertiesConfigurator;
import matrices.entities.Matrix;

import java.util.concurrent.RecursiveTask;

public class MatrixMultiplier extends RecursiveTask<Matrix> {
    private final Matrix matrix1;
    private final Matrix matrix2;
    private final Matrix matrixResult;
    private final int from;
    private final int to;

    // Constructor for initial call
    public MatrixMultiplier(Matrix matrix1, Matrix matrix2, String matrixResultName) {
        this.matrix1 = matrix1;
        this.matrix2 = matrix2;
        matrixResult = new Matrix(matrixResultName, matrix1.numRows, matrix2.numCols);
        from = 0;
        to = matrix1.numRows;
    }

    // Constructor for splitting the task recursively
    public MatrixMultiplier(Matrix matrix1, Matrix matrix2, Matrix matrixResult, int from, int to) {
        this.matrix1 = matrix1;
        this.matrix2 = matrix2;
        this.matrixResult = matrixResult;
        this.from = from;
        this.to = to;
    }

    @Override
    protected Matrix compute() {
        if (matrix1.numRows != matrix2.numCols) {
            System.err.println("APPLICATION/MatrixMultiplier: Matrix \"" + matrix1.name + "\" cannot be multiplied with matrix \"" + matrix2.name + "\" (m1.numRows [" + matrix1.numRows + "] != m2.numCols [" + matrix2.numCols + "]).");
            return null;
        }
        if (from == to) {
            return null;
        }
        if (to - from > PropertiesConfigurator.getInstance().getMaximumRowsSize()) {
            int mid = from + PropertiesConfigurator.getInstance().getMaximumRowsSize();
            MatrixMultiplier right = new MatrixMultiplier(matrix1, matrix2, matrixResult, mid, to);
            right.fork();
            multiplyMatrixRows(from, mid); // Processing left part
            right.join(); // Waiting for right task to complete
        } else {
            multiplyMatrixRows(from, to); // Processing the chunk if it's small enough
        }
        return matrixResult; // Return the result matrix
    }

    // Method which multiplies matrix rows within a specified range (from -> to)
    private void multiplyMatrixRows(int from, int to) {
        for (int i = from; i < Math.min(to, matrix1.arr.length); i++) {
            for (int j = 0; j < matrix2.arr[0].length; j++) {
                long sum = 0;
                for (int k = 0; k < matrix1.arr[0].length; k++) {
                    sum += matrix1.arr[i][k] * matrix2.arr[k][j];
                }
                matrixResult.arr[i][j] = sum;
            }
        }
    }
}
