package matrix.results;

import matrix.M_Matrix;
import task.TaskType;

public interface Result {
    M_Matrix getSyncResult(); //blokirajuci
    M_Matrix getAsyncResult();
    TaskType getTaskType();
    boolean isPoison();
    String getName();







}
