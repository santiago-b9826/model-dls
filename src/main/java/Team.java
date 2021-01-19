package main.java;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Team {

    private List<Worker> workers;
    private List<Pool> pools;

    public Team(List<Worker> workers, List<Pool> pools) {
        this.workers = workers;
        this.pools = pools;
    }

    public List<Worker> getWorkers() {
        return workers;
    }

    public List<Pool> getPools() {
        return pools;
    }

    public void start(){
        int targetCost = 306; //TODO: get parameters from configuration
        boolean strictLow = false;
        int nProc = Runtime.getRuntime().availableProcessors();
        System.out.println("Número de procesos = "+nProc);
        //ForkJoinPool myPool = new ForkJoinPool(nProc);

        for (Worker w: workers) {
            //w.setLimits(targetCost, strictLow);
            //myPool.invoke(w);
            //w.compute();
            w.fork();
        }

        for (Worker w: workers) {
            w.join();
        }
        System.out.println("Team: all workers have finished");
    }
}
