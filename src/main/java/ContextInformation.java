package main.java;

import main.java.solver.Metaheuristic;

public class ContextInformation {
    int[] variables;
    int cost;
    Metaheuristic.Type type;

    public ContextInformation(int[] variables, int cost, Metaheuristic.Type type) {
        this.variables = variables;
        this.cost = cost;
        this.type = type;
    }


    public int[] getVariables() {
        return variables;
    }

    public int getCost() {
        return cost;
    }

    public Metaheuristic.Type getType() {
        return type;
    }

    public void setVariables(int[] variables) {
        this.variables = variables;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setType(Metaheuristic.Type type) {
        this.type = type;
    }
}
