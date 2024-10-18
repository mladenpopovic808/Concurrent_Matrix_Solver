# Matrix Management System

## Description

The Matrix Management System is an application that allows users to manage matrices through a simple command-line interface. The application supports various matrix operations, including adding, multiplying, saving, and deleting matrices. The system utilizes multi-threading to optimize performance, enabling users to execute matrix operations without blocking the main thread of the application.

## Key Features

- **Add Directories**: Allows users to add directories for scanning and searching for matrices within the project.
- **Matrix Information**: Displays basic information about matrices, including the number of rows, columns, and file paths.
- **Matrix Multiplication**: Supports synchronous and asynchronous multiplication of matrices, with the option to name the resultant matrix.
- **Save Matrices**: Users can save matrices to disk in a specified format.
- **Clear Matrices**: Provides the ability to delete matrices or data from files.
- **Stop Application**: The application can be gracefully shut down, ensuring all processes are completed.

## Commands

The system supports the following commands:

- **`dir dir_name`**: Adds a new directory for scanning.
- **`info matrix_name`**: Displays information about a specific matrix or a group of matrices.
- **`multiply mat1,mat2`**: Multiplies two matrices.
- **`save -name mat_name -file file_name`**: Saves a matrix to disk.
- **`clear mat_name` / `clear file_name`**: Clears a specified matrix or file.
- **`stop`**: Shuts down the application.

## Examples

Here are some usage examples demonstrating how to interact with the system:

### Example 1: Adding a Directory and Retrieving Results

