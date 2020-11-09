package main.java;

public class Config {
    private double executionTime;
    private Object target;
    private double errorRange;
    private int numberOfTeams;
    private int numberOfWorkersPerTeam;
    private double updatePoolTime;
    private double requestPoolTime;

    public Config(double executionTime, Object target, double errorRange, int numberOfTeams, int numberOfWorkersPerTeam, double updatePoolTime, double requestPoolTime) {
        this.executionTime = executionTime;
        this.target = target;
        this.errorRange = errorRange;
        this.numberOfTeams = numberOfTeams;
        this.numberOfWorkersPerTeam = numberOfWorkersPerTeam;
        this.updatePoolTime = updatePoolTime;
        this.requestPoolTime = requestPoolTime;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public double getErrorRange() {
        return errorRange;
    }

    public void setErrorRange(double errorRange) {
        this.errorRange = errorRange;
    }

    public int getNumberOfTeams() {
        return numberOfTeams;
    }

    public void setNumberOfTeams(int numberOfTeams) {
        this.numberOfTeams = numberOfTeams;
    }

    public int getNumberOfWorkersPerTeam() {
        return numberOfWorkersPerTeam;
    }

    public void setNumberOfWorkersPerTeam(int numberOfWorkersPerTeam) {
        this.numberOfWorkersPerTeam = numberOfWorkersPerTeam;
    }

    public double getUpdatePoolTime() {
        return updatePoolTime;
    }

    public void setUpdatePoolTime(double updatePoolTime) {
        this.updatePoolTime = updatePoolTime;
    }

    public double getRequestPoolTime() {
        return requestPoolTime;
    }

    public void setRequestPoolTime(double requestPoolTime) {
        this.requestPoolTime = requestPoolTime;
    }
}
