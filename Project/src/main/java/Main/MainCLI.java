package Main;

import PoolComponents.MatrixBrain;
import PoolComponents.MatrixExtractor;
import PoolComponents.MatrixMultiplier;
import ThreadComponents.SystemExplorer;
import ThreadComponents.TaskCoordinator;
import config.Config;
import matrix.M_Matrix;

import matrix.results.Result;
import task.Task;
import task.TaskCreate;

import java.io.File;
import java.util.List;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class MainCLI {

    private volatile boolean working=true;


    private static final CopyOnWriteArrayList<String> filesToScan = new CopyOnWriteArrayList<>(); //komandom dir dodajes


    public static BlockingQueue<Task> jobQueue = new LinkedBlockingQueue<>(100);


    public static BlockingQueue<Task> taskCreateDeleteQueue = new LinkedBlockingQueue<>();


    public static BlockingQueue<Task> matrixMultiplierQueue = new LinkedBlockingQueue<>();
    public static BlockingQueue<Result> brainResultQueue = new LinkedBlockingQueue<>();



    private static final SystemExplorer systemExplorer=new SystemExplorer(filesToScan);
    private static final TaskCoordinator taskCoordinator=new TaskCoordinator();

    private static final MatrixBrain matrixBrain=new MatrixBrain();
    private static final MatrixExtractor matrixExtractor=new MatrixExtractor();
    private static final MatrixMultiplier matrixMultiplier=new MatrixMultiplier();







    public void start(){
        Config.getInstance().loadProperties();
        File file=new File(Config.getInstance().getStart_dir());
        if(!file.exists()){
            System.err.println("Pocetni direktorijum ne postoji!");
            return;
        }
        filesToScan.add(file.getAbsolutePath());//dodajemo direktorijum iz config fajla
        runAllThreads();
        begginCommandLine();
    }

    private void runAllThreads(){
        Thread systemExplorerThread = new Thread(systemExplorer);
        Thread taskCoordinatorThread = new Thread(taskCoordinator);
        Thread matrixExtractorThread = new Thread(matrixExtractor);
        Thread matrixBrainThread = new Thread(matrixBrain);
        Thread matrixMultiplierThread = new Thread(matrixMultiplier);

        systemExplorerThread.start();
        taskCoordinatorThread.start();
        matrixExtractorThread.start();
        matrixBrainThread.start();
        matrixMultiplierThread.start();

    }


    //https://stackoverflow.com/questions/10961714/how-to-properly-stop-the-thread-in-java


    //TODO ne zaboravi da stavis i posisone za ostale queue-ove

    private void begginCommandLine(){
        Scanner scanner = new Scanner(System.in);
        String line;
        String[] tokens;
        String mainCommand; //moze biti dir,info,multiply,save,clear,stop
        String path;


        while (working) {
            line = scanner.nextLine().trim();
            tokens = line.split(" ");

            if (line.isEmpty()) continue;
            mainCommand=tokens[0];


            switch (mainCommand) {

                case "dir" -> {

                    //TODO UVEDI PROVERU DA LI SE TAJ FOLDER VEC SKENIRA
                    //implementirao sam metodu koja to radi.

                    try{
                        path=line.substring(4); //Zato sto moze da se splituje cela putanja: npr C:/User/Ime Nekog Direktorijuma
                    }catch (IndexOutOfBoundsException e){
                        System.err.println("Navedite ime direktorijuma");
                        continue;
                    }

                    if (path.isEmpty()){
                        System.err.println("Navedite ime direktorijuma");
                        continue;
                    }else{
                        File file = new File(path);
                        if (!file.exists()) {
                            System.err.println("Datoteka ne postoji.");
                            continue;
                        }

                        if (!file.isDirectory()) {
                            System.err.println("Navedeni put nije direktorijum.");
                            continue;
                        }
                        //try to read file or throw exception


                        if(systemExplorer.isDirectoryAlreadyScanning(path)){ //radi,testirano
                            System.err.println("Navedeni direktorijum se vec nalazi u listi za skeniranje!");
                            continue;
                        }
                        System.out.println("Dodajem dir "+path+" u listu za skeniranje...");
                        filesToScan.add(path);
                        //Unutar ovog direktorijuma moze postojati proizvoljno mnogo direktorijuma
                        //jobQueue.add(new WebJob(ScanType.WEB, tokens[1], PropertyStorage.getInstance().getHop_count()));
                    }
                }
                //info matrixName
                //info all
                //info all -asc/desc
                //info -all -asc -s N
                //info -all -asc -e N
                case "info" -> {
                    if (tokens.length == 1) {
                        System.err.println("Navedite ime matrice ili alternative parametre");
                        continue;
                    }
                    if (tokens.length == 2) {
                        String param2 = tokens[1];
                        if (param2.equals("-all")) {
                            printMatricesList(matrixBrain.getMatrixList());
                            continue;
                            //Ako nije ni jedan parametar,tada znamo da je prosledjeno ime matrice
                        } else {
                            String matrixName = tokens[1];
                            System.out.println(matrixBrain.getMatrixInfoWithName(matrixName));
                        }
                    }
                    if (tokens.length == 3) { //info -all -asc/desc
                        String param = tokens[1];

                        if (param.equals("-all")) {
                            if (tokens[2].equals("-asc")) {
                                printMatricesList(matrixBrain.getAllMatricesSorted(1));

                            } else if (tokens[2].equals("-desc")) {
                                printMatricesList(matrixBrain.getAllMatricesSorted(-1));

                            }else{
                                System.err.println("Nevalidan parametar za sorting");
                            }
                        }
                    }
                    if(tokens.length==5){
                        int number=-1;
                        int asc=1;
                        if(tokens[1].equals("-all") && (tokens[2].equals("-asc") || tokens[2].equals("-desc")) && tokens[3].equals("-s")){

                            if(tokens[2].equals("-desc"))
                                asc=-1;
                            try{
                                number=Integer.parseInt(tokens[4]);
                                printMatricesList(matrixBrain.getAllMatricesSorted(asc).subList(0,number));
                            }catch (NumberFormatException n){
                                System.out.println("Niste prosledili broj!");
                            }catch (IndexOutOfBoundsException i){
                                System.out.println("Nema toliko matrica u listi!");
                            }
                        }else if(tokens[1].equals("-all") && (tokens[2].equals("-asc") || tokens[2].equals("-desc")) && tokens[3].equals("-e")){

                            if(tokens[2].equals("-desc"))
                                asc=-1;
                            try{
                             number=Integer.parseInt(tokens[4]);
                             List<M_Matrix>matrixList=matrixBrain.getAllMatricesSorted(asc);
                             printMatricesList(matrixList.subList(matrixList.size()-number,matrixList.size()));
                            }catch (NumberFormatException n){
                                System.out.println("Niste prosledili broj!");
                            }catch (IndexOutOfBoundsException i){
                                System.out.println("Nema toliko matrica u listi!");
                            }
                        }else{
                            System.err.println("Nevalidni parametri za info");
                        }
                    }
                }

                //multiply mat1,mat2
                //multiply mat1,mat2 -async
                //multiply mat1,mat2 -async -name matrix_name


                case "multiply" -> {
                    String tokens2[];
                    if(tokens.length==1){
                        System.err.println("Unesite ostale parametre za multiply");
                        continue;
                    }

                    if (tokens.length==2) {//multiply mat1,mat2
                        if(!tokens[1].contains(",")){
                            System.err.println("Nevalidni parametri za multiply");
                            continue;
                        }
                         tokens2=tokens[1].split(",");


                        matrixBrain.multiplySync(tokens2[0],tokens2[1],null);


                    }else if (tokens.length==3) {//multiply mat1,mat2 -async
                        if(!tokens[2].equals("-async")){
                            System.err.println("Nevalidni parametri za multiply");
                        }
                         tokens2=tokens[1].split(",");
                        matrixBrain.multiplyAsync(tokens2[0],tokens2[1],null);

                    }else if (tokens.length==4) {//multiply mat1,mat2 -name matrix_name
                        if(!tokens[2].equals("-name")){
                            System.err.println("Nevalidni parametri za multiply");
                        }
                        tokens2=tokens[1].split(",");
                        matrixBrain.multiplySync(tokens2[0],tokens2[1],tokens[3]);
                    }
                    else if(tokens.length==5){//multiply mat1,mat2 -async -name matrix_name  || multiply mat1,mat2 -name matrix_name -async

                        //Ako nije ni jedno ni drugo
                        if( !((tokens[2].equals("-async") && tokens[3].equals("-name")) || (tokens[2].equals("-name") && tokens[4].equals("-async")))){
                            System.err.println("Nevalidni parametri za multiply");
                            continue;
                        }
                         tokens2=tokens[1].split(",");
                        if(tokens[2].equals("-async")) {
                            matrixBrain.multiplyAsync(tokens2[0], tokens2[1], tokens[4]);
                        }else{
                            matrixBrain.multiplyAsync(tokens2[0], tokens2[1], tokens[3]);
                        }

                    }

                }
                case "save" -> { //save -name mat_name -file file_name

                    if (tokens.length!=5) {
                        System.err.println("Nevalidni parametri za SAVE");
                        continue;
                    }
                    if( !((tokens[1].equals("-name") && tokens[3].equals("-file")) || (tokens[1].equals("-file") && tokens[3].equals("-name")))){
                        System.err.println("Nevalidni parametri za Save");
                        continue;

                    }
                    //NE SME DA BUDE BLOKIRAJUCA!
                    //NE SME DA BUDE BLOKIRAJUCA!
                    //NE SME DA BUDE BLOKIRAJUCA!
                    //NE SME DA BUDE BLOKIRAJUCA!
                    //ZNACI NAPRAVI POOOL I POKRENI!

                    String matName="";
                    String fileName="";
                    if(tokens[1].equals("-name")){
                        matName=tokens[2];
                        fileName=tokens[4];
                    }else{
                        matName=tokens[4];
                        fileName=tokens[2];
                    }
                    matrixBrain.saveMatrix(matName,fileName);

                    continue;
                }

                case "clear" -> { //clear mat_name ili clear file_name
                    //da li je matrica ili fajl u pitanju znamo po ekstenziji. Ako sadrzi ekstenziju onda je fajl.
                    //Treba javiti matrixBrainu da li treba da obrise matricu iz svoje memorije,ili da obrise sve matrice koje su procitaje iz fajla
                    if(tokens.length!=2) return;
                    String name=tokens[1];


                    File file=matrixBrain.deleteAndGetMatrix(name);//vraca matricu(ako postoji) ili null
                    if(file!=null) {
                        //Obrisi sve matrice koje su procitane iz ovog fajla
                        systemExplorer.reloadFile(file);
                    }

                }

                case "stop" ->{

                    stopThreads();
                    working=false;
                    scanner.close();
                }
                default -> System.err.println("Nepostojeca komanda");
            }
        }
    }

    private void printMatricesList(List<M_Matrix> matrixResultMap){

        for(M_Matrix matrix:matrixResultMap){
            System.out.println(matrix.toString());
        }

    }

    private void stopThreads(){


        //TaskCoordinator gasimo poison-om.
        System.out.println("Ubacujem poison-e...");
        try {
            //Job queue ce da posalje otrov na ostale queue-ove
            jobQueue.put(new TaskCreate(true));

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        systemExplorer.stop();
        //matrixBrain.stop();
        //matrixExtractor.stop();
        //matrixMultiplier.stop();




    }

}



























