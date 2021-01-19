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
        for (int i = 0; i < 3; i++){
            workersT1.add(new Worker(size, Metaheuristic.Type.getByType(i), model));
        }
        List<Pool> poolsT1 = new ArrayList<Pool>();
        poolsT1.add(new Pool(size));

        teams.add(new Team(workersT1, poolsT1));

        for (Team t: teams) {
            t.start();
        }
    }

}
