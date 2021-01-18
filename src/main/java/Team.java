package main.java;

import java.util.List;

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

    // TODO: start each worker on a different thread
    public void start(){
        int targetCost = 0; //TODO: get parameters from configuration
        boolean strictLow = false;
        for (Worker w: workers) {
            w.start(targetCost, strictLow);
        }
    }
}
