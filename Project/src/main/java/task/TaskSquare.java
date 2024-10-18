package task;

import matrix.M_Matrix;

public class TaskSquare implements Task {


    private M_Matrix matrix;

    public TaskSquare(M_Matrix matrix) {
        this.matrix = matrix;
    }

    @Override
    public TaskType getType() {
        return TaskType.SQUARE;
    }

    @Override
    public boolean isPoison() {
        return false;
    }

    public M_Matrix getMatrix() {
        return matrix;
    }
}
