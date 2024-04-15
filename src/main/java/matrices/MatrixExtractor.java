package matrices;

import application.PropertiesConfigurator;
import matrices.entities.Matrix;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class MatrixExtractor extends RecursiveTask<Matrix> {
    private final File file;
    private Matrix matrixResult;
    private int from;
    private int to;

    // Constructor for initial parsing of the header
    public MatrixExtractor(File file) {
        this.file = file;
        parseHeader(); // Call method to parse the header
    }

    // Constructor for splitting the task recursively
    public MatrixExtractor(File file, Matrix matrixResult, int from, int to) {
        this.file = file;
        this.matrixResult = matrixResult;
        this.from = from;
        this.to = to;
    }

    // Method to parse the header of the file
    private void parseHeader() {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            int pointer = 0;
            randomAccessFile.seek(pointer);
            while (randomAccessFile.readByte() != '\n') {
                pointer++;
            }
            from = ++pointer;

            byte[] firstLineBytes = new byte[pointer];
            randomAccessFile.seek(0);
            randomAccessFile.read(firstLineBytes, 0, pointer);
            String header = new String(firstLineBytes);
            Map<String, String> headerMap = new HashMap<>();
            for (String headerValues : header.split(",")) {
                headerMap.put(headerValues.split("=")[0].trim(), headerValues.split("=")[1].trim());
            }

            matrixResult = new Matrix(headerMap.get("matrix_name"), Integer.parseInt(headerMap.get("rows")), Integer.parseInt(headerMap.get("cols")));
            matrixResult.filePath = file.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("APPLICATION/MatrixExtractor: Error while reading header from file; \"from\" and \"to\" bytes: \"" + from + ", " + to + "\".");
        }
        to = (int) file.length();
    }

    @Override
    protected Matrix compute() {
        if (from == to) {
            return null;
        }
        if (to - from > PropertiesConfigurator.getInstance().getMaximumFileChunkSize()) {
            int mid = from + PropertiesConfigurator.getInstance().getMaximumFileChunkSize() - 1;
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                randomAccessFile.seek(mid);
                while (randomAccessFile.readByte() != '\n') {
                    mid--;
                    if (mid <= from) {
                        break;
                    }
                    randomAccessFile.seek(mid);
                }
            } catch (IOException e) {
                System.err.println("APPLICATION/MatrixExtractor: Error while reading values from file; \"mid\" byte: \"" + mid + "\".");
            }
            MatrixExtractor right = new MatrixExtractor(file, matrixResult, ++mid, to); // Forking right task
            right.fork();
            parseFileRow(from, mid); // Processing left part
            right.join(); // Waiting for right task to complete
        } else {
            parseFileRow(from, to); // Processing the chunk if it's small enough
        }
        return matrixResult; // Return the result matrix
    }

    // Method which parses lines within a specified range (from -> to)
    private void parseFileRow(int from, int to) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            byte ch;
            StringBuilder fileRow = new StringBuilder();
            randomAccessFile.seek(from);
            for (int i = from; i < to; i++) {
                if ((ch = randomAccessFile.readByte()) == '\n') {
                    if (!fileRow.toString().trim().isEmpty()) {
                        String[] matrixIndices = fileRow.toString().split("=")[0].split(",");
                        matrixResult.arr[Integer.parseInt(matrixIndices[0].trim())][Integer.parseInt(matrixIndices[1].trim())] = Long.parseLong(fileRow.toString().split("=")[1].trim());
                    }
                    fileRow = new StringBuilder();
                } else {
                    fileRow.append((char) ch);
                }
            }
        } catch (Exception e) {
            System.err.println("APPLICATION/MatrixExtractor: Error while parsing given lines; \"from\" and \"to\" bytes: \"" + from + ", " + to + "\".");
        }
    }
}
