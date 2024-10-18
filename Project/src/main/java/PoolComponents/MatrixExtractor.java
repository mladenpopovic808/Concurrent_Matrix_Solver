package PoolComponents;

import Main.MainCLI;
import config.Config;
import matrix.M_Matrix;
import matrix.results.MatrixDeleteResult;
import matrix.results.MatrixResult;
import task.Task;
import task.TaskCreate;
import task.TaskDeleteMatrix;
import task.TaskType;
import task.workers.FileExtractorWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MatrixExtractor implements Runnable{

    //TODO izmeniti kada pogledas sta pool treba da ima
    private final ExecutorCompletionService<List<M_Matrix>> completionService;//koristimo ga jer ima queue u sebi i daje nam rezulate cim se zavrsi task
    private ExecutorService threadPool= Executors.newCachedThreadPool();
    private volatile boolean working=true;

    public MatrixExtractor(){
        this.completionService=new ExecutorCompletionService<>(threadPool);
    }


    @Override
    public void run() {
        while(true){
            try {
                Task task= MainCLI.taskCreateDeleteQueue.take();
                if(task.isPoison()){
                    System.out.println("MatrixExtractor je otrovan...");
                    MainCLI.brainResultQueue.add(new MatrixResult(true));
                    threadPool.shutdown();


                    break;

                }
                if(task.getType().equals(TaskType.CREATE)){
                    TaskCreate taskCreate=(TaskCreate)task;
                    if(taskCreate.getFile().isDirectory()){//Ako mu prosledimo direktorijum,treba da prodje kroz fajlove i izvuce matrice (to znaci da prvi put skeniramo direktorijum)
                        getMatricesFromDirectory(taskCreate.getFile());
                    }else{
                        getMatrixFromFile(taskCreate.getFile()); //Ako mu prosledimo fajl,to znaci da je file promenjen,tj da se lastModified izmenio.
                    }
                }else if(task.getType().equals(TaskType.DELETE)){
                    TaskDeleteMatrix taskDelete=(TaskDeleteMatrix)task;
                    deleteMatrix(taskDelete.getFile());
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //Obrisace se matrica ciji je timeModified promenjen(tj brise se stara verzija)
    private void deleteMatrix(File file){
        MainCLI.brainResultQueue.add(new MatrixDeleteResult(file));//brain treba da prodje kroz listu matrica i da obrise onu koja ima ovaj fajl
    }
    private void getMatricesFromDirectory(File directory){
        if(!directory.canRead()){
            System.err.println("Direktorijum nije citljiv -"+directory.getName());
            return;
        }
        List<File> filesToExtract = new ArrayList<>();
        //List<Future<List<M_Matrix>>>doneFutures=new ArrayList<>();
        long limit = Config.getInstance().getMaximum_file_chink_size();
        long fileLengthSum = 0;
        File[] childFiles = directory.listFiles();

        if (childFiles.length==0) {
            System.err.println("Nema decu direktorijum:"+directory.getName());
        }

        //Prolazimo kroz fajlove i sabiramo velicine, Kada se predje limit - pokrecemo thread da racuna prikupljene fajlove.
        int obradjeno=0;
        for (File childFile : childFiles) {

            if(childFile.isDirectory()) continue;//prolazimo samo kroz fajlove. Svakako ce se iz SystemExplorera pozvati za svaki direktorijum po dubini


            if (childFile.getName().endsWith(Config.getInstance().getFileExtension())) {

                if (fileLengthSum + childFile.length() > limit) {


                    if(!filesToExtract.isEmpty()){
                       // System.out.println("Pocinjem job sa "+filesToExtract.size());
                        List<File> copiedFilesToExtract = new ArrayList<>(filesToExtract);//da ne bi dolazilo do ConcurrentModification
                        Future<List<M_Matrix>> future=this.completionService.submit(new FileExtractorWorker(copiedFilesToExtract));
                        MatrixResult result=new MatrixResult(future);
                        MainCLI.brainResultQueue.add(result);
                        obradjeno+=filesToExtract.size();
                        filesToExtract.clear();
                    }
                    //Suma nam je trenutni fajl koji nije mogao da udje u obradu
                    fileLengthSum = childFile.length();
                    filesToExtract.add(childFile);
                }else{
                    fileLengthSum += childFile.length();
                    filesToExtract.add(childFile);
                }
            }
        }


        if (!filesToExtract.isEmpty()) {

            List<File> copiedFilesToExtract = new ArrayList<>(filesToExtract);//da ne bi dolazilo do ConcurrentModification
            Future<List<M_Matrix>> future=this.completionService.submit(new FileExtractorWorker(copiedFilesToExtract));
            MatrixResult result=new MatrixResult(future);


            MainCLI.brainResultQueue.add(result);

            obradjeno+=filesToExtract.size();
        }else{
            System.out.println("Nema fajlova za obradu u folderu "+directory.getName());
        }



    }
    //Ne treba da se proverava limit fajla,zato sto ce jedan thread sigurno moci jedan fajl da obradi.
    private void getMatrixFromFile(File file){

        if(!file.canRead()){
            System.err.println("Fajl nije citljiv -"+file.getName());
            return;
        }

        List<File> filesToExtract=new ArrayList<>();


        filesToExtract.add(file);

        //List<Future<M_Matrix>>doneFuture=this.completionService.submit(new FileExtractorWorker(filesToExtract));
        Future<List<M_Matrix>>future=this.completionService.submit(new FileExtractorWorker(filesToExtract));



        //Posao treba da vrati future matricu i da ti to posle prosledi brainu, kao i da pokrenes posao za kvadriranje.
        //App.logger.fileScanner(corpusDirName + " added to result queue");


        MatrixResult result=new MatrixResult(future);

        MainCLI.brainResultQueue.add(result);
        //Task za kvadriranje se pokrece u brainu kada pristigne rezultat
    }

//    public void stop() {
//        System.out.println("Gasim MatrixExtractor...");
//        working = false;
//    }
}
