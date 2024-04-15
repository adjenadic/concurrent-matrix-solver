package matrices.entities;

import lombok.Getter;

@Getter
public class Matrix {
    public String name;
    public int numRows;
    public int numCols;
    public String filePath;
    public Long[][] arr;

    public Matrix(String name, int numRows, int numCols) {
        this.name = name;
        this.numRows = numRows;
        this.numCols = numCols;
        this.arr = new Long[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                this.arr[i][j] = 0L;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("matrix_name=").append(name).append(", rows=").append(numRows).append(", cols=").append(numCols).append("\n");
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                sb.append(i).append(",").append(j).append(" = ").append(arr[i][j]);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
