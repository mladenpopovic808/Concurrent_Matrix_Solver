package PoolComponents;

import Main.MainCLI;
import config.Config;
import matrix.M_Matrix;
import matrix.results.MultiplyResult;
import matrix.results.SquareResult;
import task.Task;
import task.TaskMultiply;
import task.TaskSquare;
import task.TaskType;
import task.workers.MultiplyWorker;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MatrixMultiplier implements Runnable{

    private volatile boolean working=true;
    //TODO izmeniti kada pogledas sta pool treba da ima
    private final ExecutorService multiplyService;//koristimo ga jer ima queue u sebi i daje nam rezulate cim se zavrsi task
    List<Future>futureList=new ArrayList<>(); //pomocna lista

    public MatrixMultiplier(){
        this.multiplyService=Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        while(true){
            try {
                Task task= MainCLI.matrixMultiplierQueue.take();

                if(task.isPoison()){
                    System.out.println("MatrixMultiplier otrovan...");
                    multiplyService.shutdown();
                    break;
                }

                if(task instanceof TaskMultiply){

                    TaskMultiply taskMultiply=(TaskMultiply)task;
                    CompletableFuture<M_Matrix> future = CompletableFuture.supplyAsync(() -> {

                        MultiplyObject o= multiplyMatrices(taskMultiply.getMatrix1(),taskMultiply.getMatrix2(),taskMultiply.getName());
                        //check if futures are done
                        for(Future f:o.getFutures()){
                            while(!f.isDone());
                        }

                        return o.getMatrix();
                    });
                    MainCLI.brainResultQueue.add(new MultiplyResult(future,taskMultiply.getName()));//DODATI REZULTAT

                }else if(task instanceof TaskSquare){

                    TaskSquare taskSquareMatrix=(TaskSquare)task;
                    System.out.println("Kvadriram matricu "+taskSquareMatrix.getMatrix().getName());
                    CompletableFuture<M_Matrix> future = CompletableFuture.supplyAsync(() -> {

                        MultiplyObject o= squareMatrix(taskSquareMatrix.getMatrix());

                        for(Future f:o.getFutures()){
                            while(!f.isDone()){

                            }

                        }


                        return o.getMatrix();
                    });
                    String matrixName=taskSquareMatrix.getMatrix().getName()+taskSquareMatrix.getMatrix().getName();

                    MainCLI.brainResultQueue.add(new MultiplyResult(future,matrixName));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //TODO dobro obrati paznju da li ti radi asinhrono, jer ako ti posaljes brainu rezultat i kazes get() tu se blokira i ceka, pazi
    //imas primer sa vezbi kako radimo sa gotovim rezultatima
    private MultiplyObject multiplyMatrices(M_Matrix matrix1, M_Matrix matrix2, String name){

        futureList.clear();

        M_Matrix newMatrix = new M_Matrix(name, matrix1.getRows(), matrix2.getCols(), null,true,matrix1,matrix2);

//        for(int i:matrix1.getValues().keySet()){
//            System.out.println("Red "+i+" "+matrix1.getRow(i));
//        }

        int rows = matrix1.getRows();
        int limit = Config.getInstance().getMaximum_rows_size();
        int tmpLimit=0;
        List<List<BigInteger>>rowList=new ArrayList<>(); //lista redova prve matrice(koje se mnoze sa svim kolonama druge matrice)

        int rowToBeginFrom=0;//od kog reda da punimo matricu
        
        for (int i = 0; i < rows; i ++) {
            //Imaces 2 liste,mape cega god
            //prva lista su redovi prve matrice
            //druga lista su kolone druge matrice

            //NASTAVI ODAVDE:
            //Razmisli o tome kako da u brain vratis future,zato sto kada se zatrazi rezultat treba da
            //vratis info o tome da li je u toku, da li je zavrseno itd.

            //Dodajemo posao racunanja reda
            tmpLimit++;
            rowList.add(matrix1.getRow(i));

            if(tmpLimit==limit){
                //Ovde zelis da punis novu matricu
                //Takodje trebas da prosledis koji red nove matrice se puni,tj od koje se krece

                //make copy of rowlist
                List<List<BigInteger>>rowListCopy=new ArrayList<>(rowList);

                futureList.add(multiplyService.submit(new MultiplyWorker(newMatrix,rowListCopy,matrix2,rowToBeginFrom)));

                tmpLimit=0;
                rowToBeginFrom=i+1;

                //Mnogo brzo izracuna,pa da bismo simulirali cekanje
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                rowList.clear();

            }
        }
        if(rowList.size()>0){
            List<List<BigInteger>>rowListCopy=new ArrayList<>(rowList);

            futureList.add(multiplyService.submit(new MultiplyWorker(newMatrix,rowListCopy,matrix2,rowToBeginFrom)));
        }
        //sada treba da vrv spakujes u rezultat koji sadrzi ime matrice pomnozene i taj future i da je cuvas u brainu kao mapa future-a

        //Kako funkcionise sve ovo:
        //Cim se pokrene mnozenje matrica, brainu se posalje future nad ovom metodom (koja vraca novu matricu)
        //u ovoj metodi pokrecemo nove threadove koji mnoze matricu
        //kada se zavrsi mnozenje, tada ce i nas future u brainu biti gotov i znacemo da je matrica izracunata




        List<Future>futureListCopy=new ArrayList<>(futureList);
        return new MultiplyObject(newMatrix,futureListCopy);

        //Treba verovatno da raspakujes dobijenu matricu u brainu,ubacis je u listu matrica i da kesiras
    }


    //Kvadriranje se radi sa transponovanom matricom (ne mozemo da pomnozimo 3x2 i 3x2 matricu vec 3x2 i 2x3)
    private MultiplyObject squareMatrix(M_Matrix matrix1){
        M_Matrix matrix2=matrix1.getTransponedMatrix();
        //TODO obrati paznju da li treba sync ili async

        MultiplyObject result=multiplyMatrices(matrix1,matrix2,matrix1.getName()+"^2");
        return result;

    }
    private class MultiplyObject{

        M_Matrix matrix;
        List<Future>futures;

        public MultiplyObject(M_Matrix matrix, List<Future> futures) {
            this.matrix = matrix;
            this.futures = futures;
        }

        public M_Matrix getMatrix() {
            return matrix;
        }

        public List<Future> getFutures() {
            return futures;
        }
    }


    public void stop() {
        System.out.println("Gasim MatrixMultiplier...");
        working = false;
    }

}
