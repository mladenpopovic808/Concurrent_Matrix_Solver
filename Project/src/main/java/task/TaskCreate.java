package task;

import java.io.File;

//Kreiranje matrice iz tekstualnog fajla
public class TaskCreate implements Task{

    private final boolean poison;
    private  File file;


    public TaskCreate(File file) {
        this.file=file;
        this.poison=false;
    }

    public TaskCreate(boolean poison) {
        this.poison = poison;
    }



    @Override
    public boolean isPoison() {
        return poison;
    }


    @Override
    public TaskType getType() {
        return TaskType.CREATE;
    }

    public File getFile() {
        return file;
    }
}
