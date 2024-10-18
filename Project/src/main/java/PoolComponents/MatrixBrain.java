package PoolComponents;

import Main.MainCLI;
import matrix.M_Matrix;
import matrix.results.*;
import task.*;
import task.workers.SaveMatrixToFileWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MatrixBrain implements Runnable {



    private final Map<String, M_Matrix> matrixMap = new ConcurrentHashMap<>();//mapa svih matrica
    private final Map<String, M_Matrix> multiplyMap = new ConcurrentHashMap<>();//matrice koje su dobijene mnozenjem
    private final Map<String,MultiplyResult> multiplyFeatureMap =new ConcurrentHashMap<>(); //mapa future-a
    private final ExecutorService voidExecutorService;//Koristimo ga za save,i za cekanje future-a od mnozenja matrica


    public MatrixBrain(){
        this.voidExecutorService = Executors.newCachedThreadPool();

    }

    @Override
    public void run() {
        while(true){
            try {
                Result result= MainCLI.brainResultQueue.take();
                if(result.isPoison()){
                    System.out.println("MatrixBrain je otrovan...");
                    voidExecutorService.shutdown();
                    break;
                }
                if(result.getTaskType().equals(TaskType.CREATE)){

                    MatrixResult matrixResult=(MatrixResult)result;
                    addMatricesToList(matrixResult);

                }else if(result.getTaskType().equals(TaskType.DELETE)){
                    MatrixDeleteResult deleteResult=(MatrixDeleteResult)result;
                    deleteMatrixWithFile(deleteResult.getFileToDelete().getName());

                }else if(result.getTaskType().equals(TaskType.MULTIPLY)){

                    MultiplyResult multiplyResult = (MultiplyResult) result;
                    addToMatrixListWhenFutureIsDone(multiplyResult);
                    multiplyFeatureMap.put(multiplyResult.getName(), multiplyResult);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void addToMatrixListWhenFutureIsDone(Result result) {
        voidExecutorService.submit(() -> {
            Future<M_Matrix> futureResults;
            if (result instanceof MultiplyResult) {
                futureResults = ((MultiplyResult) result).getFuture();
            } else
                futureResults = ((SquareResult) result).getFuture();

            M_Matrix matrix = null;
            try {
                matrix = futureResults.get();



                matrixMap.put(matrix.getName(), matrix);
                multiplyMap.put(matrix.getName(), matrix);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    //Metoda brise pojave matrice i vraca njen fajl main-u ako matrica postoji
    //vraca null ako ne postoji matrica
    private synchronized File deleteMatrixWithName(String matrixName) {

        //Prvo brisemu samu tu matricu
        M_Matrix matrix;
        if (matrixMap.containsKey(matrixName)) {
            matrix = matrixMap.get(matrixName);
            matrixMap.remove(matrixName);

            Iterator<String> iterator = matrixMap.keySet().iterator();
            while (iterator.hasNext()) {
                String nameKey = iterator.next();
                M_Matrix m = matrixMap.get(nameKey);
                if (m.isMultiplied()) {
                    //Ako je matrica rezultat mnozenja,proveravamo da li je nasa matrica ucestvovala u mnozenju
                    if (m.getMatrix1().getName().equals(matrixName) || m.getMatrix2().getName().equals(matrixName)) {
                        System.out.println("Brisemo pojavu " + m.getName());
                        matrixMap.remove(nameKey);
                        multiplyMap.remove(nameKey);
                        multiplyFeatureMap.remove(nameKey);
                        isMultiplyJobSent.remove(nameKey);

                    }
                }
            }
            System.out.println("Obrisane sve pojave matrice " + matrixName);
            return matrix.getFile();
        }
        System.out.println("Matrica ne postoji");
        return null;
    }
    private synchronized File deleteMatrixWithFile(String fileName){

        //Pronalazimo ime matrice koja sadrzi fajl
        Iterator<M_Matrix> iterator=matrixMap.values().iterator();
        while(iterator.hasNext()){
            M_Matrix matrix=iterator.next();

            if(matrix.getFile()==null) continue;//ovo su pomnozene matrice,koje nemaju fajl! (imace fajl ako ih korisnik sacuva

            if(matrix.getFile().getName().equals(fileName)){
                deleteMatrixWithName(matrix.getName());
                return matrix.getFile();
            }
        }
        System.out.println("Ne postoji takva matrica");
        return null;
    }

    public synchronized File deleteAndGetMatrix(String name) {

        if(name.endsWith(".rix")){
            return deleteMatrixWithFile(name);
        }else{
            return deleteMatrixWithName(name);
        }
    }


    public synchronized void multiplySync(String matrix1Name,String matrix2Name,String newMatrixName){
        M_Matrix matrix1= matrixMap.get(matrix1Name);
        M_Matrix matrix2= matrixMap.get(matrix2Name);

        if(matrix1==null || matrix2==null){
            System.out.println("Ne postoji matrica sa tim imenom");
            return;
        }
        //proveri da li matrice mogu da se pomnoze
        if(matrix1.getCols()!=matrix2.getRows()){
            System.out.println("Matrice ne mogu da se pomnoze (broj kolona prve matrice nije jednak broju redova druge matrice)");
            return;
        }
        if(newMatrixName==null){
            newMatrixName=matrix1Name+matrix2Name;
        }


        if(multiplyMap.containsKey(newMatrixName)){
            M_Matrix newMatrix=multiplyMap.get(newMatrixName);
            System.out.println("Kesirano : "+newMatrix.toString());
            return;
        }

        System.out.println("Zapocinjem SYNC mnozenje...");
        //MatrixBrain moze da upisuje u queue,vidi se sa prve slike u specifikaciji
        MainCLI.jobQueue.add(new TaskMultiply(matrix1,matrix2,newMatrixName));


        //Dok MatrixMultiplier nam ne posalje rezultat
        while(!multiplyFeatureMap.containsKey(newMatrixName)) {


        }

        MultiplyResult result= multiplyFeatureMap.get(newMatrixName);
        M_Matrix newMatrix=result.getSyncResult();
        multiplyMap.put(newMatrixName,newMatrix);
        System.out.println("Gotovo! "+newMatrix.toString());
        multiplyFeatureMap.remove(newMatrixName);
        //U mapu se upisuje kada brain dobije rezultat kada Brain primi rezultat

    }
    private final Map<String,Boolean>isMultiplyJobSent=new ConcurrentHashMap<>();

    public void multiplyAsync(String matrix1Name,String matrix2Name,String newMatrixName){
        M_Matrix matrix1= matrixMap.get(matrix1Name);
        M_Matrix matrix2= matrixMap.get(matrix2Name);

        if(matrix1==null || matrix2==null){
            System.out.println("Ne postoji takva matrica");
            return;
        }
        //proveri da li matrice mogu da se pomnoze
        if(matrix1.getCols()!=matrix2.getRows()){
            System.out.println("Matrice ne mogu da se pomnoze");
            return;
        }
        if(newMatrixName==null){
            newMatrixName=matrix1Name+matrix2Name;
        }

        if(multiplyMap.containsKey(newMatrixName)){
            M_Matrix newMatrix=multiplyMap.get(newMatrixName);
            System.out.println("Kesirano : "+newMatrix.toString());
            return;
        }


        if(multiplyFeatureMap.keySet().isEmpty()){
            System.out.println("PRAZNO JE");
        }

        if(multiplyFeatureMap.containsKey(newMatrixName)){
            MultiplyResult result= multiplyFeatureMap.get(newMatrixName);
            M_Matrix newMatrix=result.getAsyncResult();//vraca null ako nije gotovo
            if(newMatrix==null) {
                System.out.println("U toku...");

            }

        }else{
            //Ako smo vec poslali na obradu,ali nije nam stigao feature
            if(isMultiplyJobSent.containsKey(newMatrixName)){
                System.out.println("Pocinje uskoro...");

            //Ako nije do sad poslato na obradu
            }else{
                //MatrixBrain moze da upisuje u queue,vidi se sa prve slike u specifikaciji
                MainCLI.jobQueue.add(new TaskMultiply(matrix1,matrix2,newMatrixName));
//              MainCLI.matrixMultiplierQueue.add(new TaskMultiply(matrix1,matrix2,newMatrixName));
                isMultiplyJobSent.put(newMatrixName,true);
                System.out.println("Zapoceto!");
            }
        }
    }
   private void addMatricesToList(MatrixResult result){
        Future<List<M_Matrix>> futureResults=result.getFuture();
        try {
            List<M_Matrix> matrices=futureResults.get();//get je blokirajuci
            Iterator<M_Matrix> iterator=matrices.iterator();

            while(iterator.hasNext()){
                M_Matrix matrix=iterator.next();
                matrixMap.put(matrix.getName(),matrix);
                System.out.println("Kreirana matrica "+matrix.getName());
                MainCLI.jobQueue.put(new TaskSquare(matrix));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    //mora sync dok se ne napravi thread u poolu jer matrica moze da se obrise izmedju
    public synchronized void saveMatrix(String matName,String fileName){

        if(!matrixMap.containsKey(matName)) {
            System.out.println("Ne postoji takva matrica");
            return;
        }
        System.out.println("Zapocinjem asinhrono cuvanje...");
        this.voidExecutorService.submit(new SaveMatrixToFileWorker(matName, fileName, matrixMap.get(matName)));
    }
    public String getMatrixInfoWithName(String matrixName){

        if(matrixMap.containsKey(matrixName)){
            return matrixMap.get(matrixName).toString();
        }
        return "Ne postoji takva matrica";
    }

    public List<M_Matrix>getMatrixList(){
        return new ArrayList<>(matrixMap.values());
    }
    public List<M_Matrix>getAllMatricesSorted(int asc){

        List<M_Matrix>sortedList=getMatrixList();

        //Ukoliko je asc==-1 to znaci da se sortira desc,i mnozimo rezultat sa -1 kako bismo obrnuli

        sortedList.sort((o1, o2) -> {
            if(o1.getRows()==o2.getRows()){
                return asc*Integer.compare(o1.getCols(),o2.getCols());
            }
            return asc*Integer.compare(o1.getRows(),o2.getRows());
        });

        return sortedList;
    }
}
