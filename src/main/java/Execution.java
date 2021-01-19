package main.java;

import main.java.model.QAPModel;
import main.java.solver.Metaheuristic;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Execution {
    private void loadConfig(Config config){

    }
    private void init(){

    }
    private void start(){

    }

    public void testQAP(String file, int size) throws FileNotFoundException {
        // Load model
        QAPModel model = new QAPModel(size);
        model.loadData(file);

        List<Team> teams = new ArrayList<Team>();
        // TEAM 1
        List<Worker> workersT1 = new ArrayList<Worker>();
        List<Pool> poolsT1 = new ArrayList<Pool>();
        poolsT1.add(new Pool(10));
        poolsT1.add(new Pool(10));

        for (int i = 0; i < 10; i++){
            workersT1.add(new Worker(i, size, i%3, new QAPModel(model), 1000, 200000, poolsT1.get(0), poolsT1.get(0)));
        }



        teams.add(new Team(workersT1, poolsT1));

        for (Team t: teams) {
            t.start();
        }
    }

}
