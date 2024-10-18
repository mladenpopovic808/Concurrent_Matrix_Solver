package ThreadComponents;

import Main.MainCLI;
import config.Config;
import task.TaskCreate;
import task.TaskDeleteMatrix;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class SystemExplorer implements Runnable {

    private volatile boolean working = true;
    private final HashMap<File, Long> lastModifiedMap;
    private final CopyOnWriteArrayList<String> directoriesToScan;
    private final HashMap<String,TaskCreate>taskCreateHashMap; //Ova mapa sluzi iskljucivo za brisanje taskova iz queue-a ako je fajl modifikovan
    private final HashMap<String, Boolean> scannedFiles;//da li je skeniran/da li treba ponovo da se skenira u novom ciklusu

    public SystemExplorer(CopyOnWriteArrayList<String> directoriesToScan) {//prosledjuje se prazna lista,koja ce se puniti iz maina preko komandi
        scannedFiles = new HashMap<>();
        taskCreateHashMap = new HashMap<>();
        lastModifiedMap = new HashMap<>();
        this.directoriesToScan = directoriesToScan;
    }


    @Override
    public void run() {
        while (working) {
            try {
                for (String path : directoriesToScan) {
                    File file=new File(path);
                    if(file.isDirectory()){
                        if(file.canRead()){ //trazi se u zadatku provera
                            scanDirectory(file, path); //izvuci sve matrice iz datog direktorijuma
                        }else{
                            System.err.println("Nije dozvoljeno citati iz direktorijuma "+file.getName());
                        }
                    }
                }
                Thread.sleep(Config.getInstance().getSys_explorer_sleep_time());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }


    private void scanDirectory(File directory, String path) throws InterruptedException {

        //Ako direktorijum koji se nalazi na nasoj listi za skeniranje vise ne postoji,treba ga obrisati
        if(!directory.exists()){
            System.err.println("Direktorijum \" "+directory.getName()+"\" vise ne postoji,brisem ga iz liste pretrage...");
            directoriesToScan.remove(directory.getAbsolutePath());
            return;
        }
        if(!scannedFiles.containsKey(directory.getAbsolutePath())){
             System.err.println("Direktorijum "+directory.getName()+" Je pusten na skeniranje...");
        }

        File[] fileList = directory.listFiles();//todo napravi listu dirova, tako da ne saljes dir 3 puta ako ima 3 fajla

        if (fileList == null) return;

        boolean foundRixFile=false;
        for (File file : fileList) {
            if (file.isDirectory()) {

                scanDirectory(file, path);
            }else {
                if (file.getName().endsWith(Config.getInstance().getFileExtension())) {
                    foundRixFile=true;
                    checkLastModifiedOfFile(file);//Zapamti ga prvi put,a posleproverava da li se promenio.
                }
            }
        }
        //Ako u direktorijumu postoje .rix fajlovi,
        if(foundRixFile){
            //Ako ranije nismo izvukli matrice iz tog foldera,izvuci
            if(!scannedFiles.containsKey(directory.getAbsolutePath())){

                //Ako task create-u prosledimo direktorijum,on ce izvudi matrice iz tog direktorijuma
                //ali nece ici u dubinu,zato sto se odlazak u dubinu radi ovde
                //a ako mu prosledimo fajl koji nije direktorijum,tada ce samo taj fajl da izvuce


                MainCLI.jobQueue.put(new TaskCreate(directory));
                //MainCLI.taskCreateQueue.put(task);
                scannedFiles.put(directory.getAbsolutePath(),true);
            }
        }
    }
    private void checkLastModifiedOfFile(File fileToScan) throws InterruptedException {

        String filePath = fileToScan.getAbsolutePath();
        long lastModified;
        lastModified = fileToScan.lastModified();

        //ako ne postoji u skeniranim fajlovima to znaci da ga dodajes prvi put i samo ga dodajes u queue
        if (!scannedFiles.containsKey(filePath)) {
            System.out.println("Pronadjen "+fileToScan.getName());
            scannedFiles.put(filePath, true);
            lastModifiedMap.put(fileToScan, lastModified);
            TaskCreate task=new TaskCreate(fileToScan);
            taskCreateHashMap.put(filePath,task);//Ova mapa sluzi iskljucivo za brisanje taskova iz queue-a ako je fajl modifikovan


        }else{  //Ako postoji,proveravas last modified
            //Ignorisi cinjenicu da moze da se promeni ime fajla
            /**
             * Međutim, ako je došlo do promene u fajlu, System Explorer treba da osigura da se učitaju samo izmenjene matrice,
             * sprečavajući dodavanje duplikata. Potrebno je ukloniti stare verzije izmenjenih matrica i dodati nove (ili izmeniti stare vrednosti novim).
             * Svaka matrica je interno “povezana” sa fajlom iz kog se pročitala.
             *
             * Ako obrises matrixu za koju je vec napravljen posao mnozenja matrice,treba da se taj posao izmeni, MAKAR DA SE OBRISE
             */
            if(lastModifiedMap.get(fileToScan)!=lastModified){//Ako je fajl izmenjen
                lastModifiedMap.put(fileToScan, lastModified);
                System.out.println("Izmenjen fajl : "+fileToScan.getName());

                //TODO potrebno je obrisati matricu iz brain-a. Da li treba da se napravi novi task TaskDelete?
                MainCLI.jobQueue.remove(taskCreateHashMap.get(filePath));
                MainCLI.jobQueue.put(new TaskDeleteMatrix(fileToScan));
                MainCLI.jobQueue.put(new TaskCreate(fileToScan));//skenira se novi fajl
            }
        }
}
    public static void reloadFile(File file){
        try {
            MainCLI.jobQueue.put(new TaskCreate(file));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean doesDirectoryHaveChildren(String parent, String child) {
        File parentDir = new File(parent);
        File[] children = parentDir.listFiles();

        if (children != null) {
            for (File childFile : children) {
                if (childFile.isDirectory()) {
                    if (childFile.getAbsolutePath().equals(child) || doesDirectoryHaveChildren(childFile.getAbsolutePath(), child)) {
                        return true;
                    }
                } else {
                    if (childFile.getAbsolutePath().equals(child)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //Ide u dubinu direktorijuma za skeniranje i pita se da li je navedeni folder dete nekog od direktorijuma
    public boolean isDirectoryAlreadyScanning(String pathToCheck) {
        File file=new File(pathToCheck);
        String absoulutePath=file.getAbsolutePath();;

        Iterator<String> iterator = directoriesToScan.iterator();
        while (iterator.hasNext()) {
            String dirPath= iterator.next();
            if(dirPath.equals(absoulutePath)){
                return true;
            }
            //Rekurzivna metoda koja u dubinu pronalazi dete
            if(doesDirectoryHaveChildren(dirPath,pathToCheck)){
                return true;
            }
        }

        return false;
    }
    public void stop() {
        System.out.println("Gasim SystemExplorer...");
        working = false;
    }

}
