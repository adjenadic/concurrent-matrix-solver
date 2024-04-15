# Concurrent Matrix Solver
## Summary
A Java console application which concurrently explores set directories for ".rix" files, extracts them into internal data structures, and can multiply and save the results back into new ".rix" files.

## Use
| Command          | Description                                                          |
|------------------|----------------------------------------------------------------------|
| `help` | Lists the available commands.                                                  |
| `dir <dir_name>` | Marks the specified directory for exploration.                       |
| `info <matrix_name>` | Provides information about the specified matrix.                 |
|                   | - `-all`: Displays available information about all matrices.        |
|                   | - `-asc`: Sorts information in ascending order.                     |
|                   | - `-desc`: Sorts information in descending order.                   |
|                   | - `-s <N>`: Displays starting N elements.                           |
|                   | - `-e <N>`: Displays ending N elements.                             |
| `multiply <mat1>,<mat2>` | Multiplies two matrices.                                     |
|                   | - `-async`: Performs multiplication asynchronously.                 |
|                   | - `-name <matrix_name>`: Sets the name of the resulting matrix.     |
| `save -name <mat_name> -file <file_name>` | Saves a matrix to a file.                   |
| `clear <mat_name>`  | Clears the specified matrix.                                      |
| `clear <file_name>` | Clears the specified file.                                        |
| `stop`              | Stops the application gracefully.                                 |
