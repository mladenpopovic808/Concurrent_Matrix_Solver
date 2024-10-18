package matrix.results;

import matrix.M_Matrix;
import task.TaskType;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SquareResult implements Result{

    private final Future<M_Matrix> future;
    private final String name;

    public SquareResult(Future<M_Matrix> future, String name) {
        this.future = future;
        this.name = name;
    }
    @Override
    public M_Matrix getSyncResult() {
        try {
            M_Matrix matrix=future.get();
            return matrix;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public M_Matrix getAsyncResult() {

        if(future.isDone()){
            try {
                return future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }else{

        }
        return null;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SQUARE;
    }

    @Override
    public boolean isPoison() {
        return false;
    }

    public String getName() {
        return name;
    }

    public Future<M_Matrix> getFuture() {
        return future;
    }
}
