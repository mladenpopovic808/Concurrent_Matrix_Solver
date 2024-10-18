package task;

import java.io.File;

public class TaskDeleteMatrix implements Task{

    private final boolean poison;
    private File file;

    public TaskDeleteMatrix(boolean poison) {
        this.poison = poison;
    }
    public TaskDeleteMatrix(File file) {
        this.file=file;
        this.poison = false;
    }

//    @Override
//    public Future<M_Matrix> initiate(File file) {
//        //Brisanje matrice
//        //Obrati paznju na to da brisanje bude sinhronizovano! Verovatno hoce ako u brainu imas konkurektnu listu
//        //Potrebno je da prodje kroz sve matrice,i ako se poklapaju fajlovi - brisemo ga.
//        return null;
//    }
    @Override
    public TaskType getType() {
        return TaskType.DELETE;
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean isPoison() {
        return poison;
    }
}
