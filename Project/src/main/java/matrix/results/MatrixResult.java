package matrix.results;

import matrix.M_Matrix;
import task.TaskType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

//Rezultat koji predstavlja skeniranu matricu.
public class MatrixResult implements Result{



    private final Future<List<M_Matrix>> future;//Lista future-a: svaku Future ima u sebi listu matrica koje su skenirane
    private Map<String, Integer> cache = new HashMap<>();
    private boolean poison=false;


    //Svaki rezultat ce da vrati listu,zato sto za Modifikovani fajl vraca jednu matricu ali kada skeniramo folder onda vraca vise
    //pa ce brain da uzme tu listu i da raspakuje u pojedinacne matrice.

    public MatrixResult(Future<List<M_Matrix>> futureResult){
        this.future = futureResult;
    }
    public MatrixResult(boolean poison){
        this.poison=poison;
        future=null;
    }

    //S obzirom da imas odvojene metode za skeniranje vise fajlove i jednog fajla,ovde treba da prosledis listu matrica
    //pa iako jedan fajl vraca jednu matricu,ubacu je u listu
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
        return TaskType.CREATE;
    }

    @Override
    public boolean isPoison() {
        return poison;
    }

    @Override
    public String getName() {
        return null;
    }

    public Future<List<M_Matrix>> getFuture() {
        return future;
    }
}
