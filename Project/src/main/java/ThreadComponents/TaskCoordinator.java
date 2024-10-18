package ThreadComponents;


import Main.MainCLI;
import task.Task;
import task.TaskDeleteMatrix;
import task.TaskType;

public class TaskCoordinator implements Runnable{


    //Ova komponenta jedina moze da cita iz queue-a,tako da cemo ga gasiti preko poisona a ne preko working-a


    @Override
    public void run() {

        while (true) {//uzima poslove sa jobQueue i rasporedjuje ih u File ili Web queue


            //u dokumentaciji pise da BlockingQueue.take() se blokira sve dok ne stigne novi element.
            //Tako da wait nije potreban.
            try {
                Task task = MainCLI.jobQueue.take();

                //Gasenje thread-a
                if(task.isPoison()){
                    MainCLI.taskCreateDeleteQueue.put(task);
                    MainCLI.matrixMultiplierQueue.put(task);
                    System.out.println("TaskCoordinator je otrovan...");
                    break;
                }

                //Create i ddelete moraju da budu u jednom queue-u
                if (task.getType() == TaskType.CREATE) {

                    MainCLI.taskCreateDeleteQueue.put(task);
                }
                else if (task.getType() == TaskType.MULTIPLY || task.getType() == TaskType.SQUARE){
                    //System.out.println("TaskCoordinator: Prosledio sam TaskMultiply");
                    MainCLI.matrixMultiplierQueue.put(task);
                }

                else if (task.getType() == TaskType.DELETE) {

                    MainCLI.taskCreateDeleteQueue.put(task);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
