package main.java.solver;

import com.sun.org.apache.xml.internal.res.XMLErrorResources_tr;
import main.java.model.QAPModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Metaheuristic {
    //protected move =new MovePermutation(-1n, -1n);

    public enum Type {
        AS(0), EO(1), ROT(2);
        private static final Map<Integer, Type> mappingMap = new HashMap<Integer, Type>();
        static {
            for (Type m : Type.values()) {
                mappingMap.put(m.getValue(), m);
            }
        }
        private final int type;
        Type(int aType) {
            type = aType;
        }
        public int getValue() {
            return type;
        }
        public static Type getByType(int aType) {
            return mappingMap.get(aType);
        }
    }

    Random random;
    protected int nSwap;
    protected QAPModel problemModel;

    protected Type mySolverType;
    public int[] variables;
    protected MovePermutation move = new MovePermutation(-1, -1);
    protected int size;

    public Metaheuristic(int size) {
        this.size = size;
        variables = new int[size];
        random = new Random();
    }


    public static Metaheuristic make(Type MHtype, int size){
        switch (MHtype){
            case EO:
                return new EOSearch(size);
            case ROT:
                return new RoTSearch(size);
            default:
                return new AdaptiveSearch(size);
        }
    }


    public void setMySolverType(Type mySolverType) {
        this.mySolverType = mySolverType;
    }

    public Type getMySolverType() {
        return mySolverType;
    }

    public void configHeuristic(QAPModel problemModel/*, ParamManager opts*/) {
        System.out.println("MetaH: config problem");
        this.problemModel = problemModel;
    }

    /**
     * Search process (in loop functionality)
     * To be overwrited for each child class (solver)
     */
    public int search(int currentCost, int bestCost, int nIter) {
        // Swap two random variables
        int size = problemModel.getSize();
        move.setFirst(random.nextInt(problemModel.getSize()));
        move.setSecond(random.nextInt(problemModel.getSize()));
        swapVariables(move.getFirst(), move.getSecond());
        nSwap++;
        problemModel.executedSwap(move.getFirst(), move.getSecond(), variables);
        /*if(costo < currentCost){
 			System.out.print("Costo (RandomSearch): " + costo + ". Con variables: ");
 			System.out.print("\n");
 		}*/
        return problemModel.costOfSolution(size, true, variables);
    }

//public def setBaseVariables(variablesIn:Rail[Int]){
//	Rail.copy(variablesIn as Valuation(size), this.variables as Valuation(size));
//}

    public void initVar() {
        nSwap = 0;
    }

    public void applyLS() {
    }

    public void clearNSwap() {
        this.nSwap = 0;
    }

    public int getNSwap() {
        return nSwap;
    }

    public void setSeed(long inSeed) {
        this.random = new Random(inSeed/* + here.id*/);
    }

    //public void setSolverType(int mySolverType) {
    //    this.mySolverType = mySolverType;
    //}

    public int[] getVariables() {
        //System.out.println("Nodo: " + here.id + " pidiendo variables para crear State");
        return this.variables;
    }

    public void setVariables(int[] variables) {
        this.variables = variables;
    }

    /*public void setVariables(int size, int[] variables) {
        this.variables = variables;
    }*/

    public void swapVariables(int i, int j) {
        int x = variables[i];
        variables[i] = variables[j];
        variables[j] = x;
    }

    //Jason: Migration
    public boolean tryInsertIndividual(int[] variables, int size) {
        System.out.println("BadInvocation of tryInsertIndividual, this its not a GA Heuristic");
        return false;
    }

    public int getWorstCost() {
        System.out.println("BadInvocation of getWorstCost, this its not a GA Heuristic");
        return Integer.MAX_VALUE;
    }

    //Jason: Migration
    public int[] getConfigForPop(boolean replace) {
        System.out.println("BadInvocation of getConfigForPop, this is not a GA Heuristic");
        return null;
    }

    /*public getRandomConfigSol():ConfigSol
    {
        System.out.println("BadInvocation of getRandomConfigSol, this its not a GA Heuristic");
        return null;
    }*/

    public boolean launchEventForStagnation() {
        return false;//System.out.println("Error. launchEventForStagnation() invokation in HeuristicSolver");
    }

    public void setKill(boolean value) {

    }

    public void displayInfo(String s) {
        System.out.println("Error. displayInfo() invokation in HeuristicSolver");
    }

    public int reset(int n, int totalCost) {
        while (n-- != 0) {
            int i = random.nextInt(variables.length);
            int j = random.nextInt(variables.length);
            swapVariables(i, j);
            //System.out.println("Este es el While problematico");
        }
        return -1;
    }

    public void restartVar() {

    }

    public void initVariables() {
        System.out.println(mySolverType.toString()+": initilizing model");
        int baseValue = 0;
        for (int i = 0; i < variables.length; i++){
            variables[i] = baseValue + i;
        }
        for (int i = size - 1 ; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int x = variables[i];
            variables[i] = variables[j];
            variables[j] = x;
        }
    }

    public void clearProblemModel() {
        problemModel.clearProblemModel();
    }

    /*public void createNewSol() {
        val conf = this.problemModel.createNewSol();
        //System.out.println("at Heuristic Sover created Solution: " + conf);
        return conf;
    }*/

    /**
     * Método agregado para Migración, para crear una nueva solución cuando en el stackForDiv
     * no hay nada
     * su solución
     */
    //public int[] createNewConf() {
    //    return this.problemModel.initialize(this.random.nextInt());
    //}

    public int costOfSolution() {
        return problemModel.costOfSolution(size, true, variables);
    }

    public int getSizeProblem() {
        return problemModel.getSize();
    }

    public double getDistance(int[] a, int[] b) {
        //val size = this.problemModel.size;
        return problemModel.distance(size, a, b);

    }

    public boolean verify(int[] conf) {
        //val size = this.problemModel.size;
        return problemModel.verify(size, conf);

    }

    /*public int displaySolution(int conf) {
        int size = this.problemModel.getSize();
        this.problemModel.displaySolution(size, conf);
    }*/

 /*   public int costOfSolution(int size, int[] conf) {
        return problemModel.costOfSolution(size, conf);
    }*/




    class MovePermutation {
        private int first = -1;
        private int second = -1;

        public MovePermutation(int f, int s){
            this.first = f;
            this.second = s;
        }

        public int getFirst() {
            return this.first;
        }
        public int getSecond() {
            return this.second;
        }
        public void setFirst(int f){
            this.first = f;
        }
        public void setSecond(int s){
            this.second = s;
        }
    }

}
