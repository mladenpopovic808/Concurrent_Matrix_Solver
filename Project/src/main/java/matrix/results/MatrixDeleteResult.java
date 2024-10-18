package matrix.results;

import matrix.M_Matrix;
import task.TaskType;

import java.io.File;

public class MatrixDeleteResult implements Result{

    //Prosledi se matrica koja treba da se obrise
    private File fileToDelete;



    public MatrixDeleteResult(File file){
        this.fileToDelete=file;

    }

    @Override
    public M_Matrix getSyncResult() {
        return null;
    }

    @Override
    public M_Matrix getAsyncResult() {
        return null;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.DELETE;
    }

    @Override
    public boolean isPoison() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    public File getFileToDelete() {
        return fileToDelete;
    }
}
