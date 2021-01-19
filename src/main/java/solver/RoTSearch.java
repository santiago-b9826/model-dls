package main.java.solver;

import main.java.model.QAPModel;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class RoTSearch extends Metaheuristic{

    private double tabuDurationFactorUS;
    private double aspirationFactorUS;
    private double tabuDurationFactor;
    private double aspirationFactor;
    private int tabuDuration;
    private int aspiration;

    private boolean autorized;
    private boolean aspired;
    private boolean alreadyAspired;

    /** Tabu List Matrix */
    private int[][] tabuList;

    //tdl = 0.9;
    private double tdl = 0.8;
    private double tdu = 1.2;

    private double al = 2.0;
    private double au = 5.0;

    public RoTSearch(int size){
        super(size);
        mySolverType = Type.ROT;
    }

    public void configHeuristic(QAPModel problemModel){
        super.configHeuristic(problemModel/*, opts*/);
        tabuList = new int[problemModel.getSize()][problemModel.getSize()];
        tabuDurationFactorUS = -1; //opts("-RoTS_t", -1.0);
        aspirationFactorUS = -1; //opts("-RoTS_a", -1.0);
    }

    private int tabuDurationLower;
    private int tabuDurationUpper;

    /**
     *  Initialize variables of the solver
     *  Executed once before the main solving loop
     */
    public void initVar(){
        super.initVar();
        for(int x = 0; x < tabuList.length; x++){
            Arrays.fill(tabuList[x], 0);
        }
        if (tabuDurationFactorUS < 0){
            tabuDurationFactor = -tabuDurationFactorUS;
        } else {
            tabuDurationFactor = tabuDurationFactorUS;
        }
        //Console.OUT.println("this.tabuDurationFactor: " + this.tabuDurationFactor);
        tabuDuration = (int)(tabuDurationFactor * problemModel.getSize());
        //Console.OUT.println("this.tabuDuration: " + this.tabuDuration);
        if (aspirationFactorUS == -1.0)
            aspirationFactor = al + (au - al) * ThreadLocalRandom.current().nextDouble();
        else
            aspirationFactor = aspirationFactorUS;
        //Console.OUT.println("this.aspirationFactor: " + this.aspirationFactor);

        aspiration = (int) (aspirationFactor * problemModel.getSize() * problemModel.getSize());
        //Console.OUT.println("this.aspiration: " + this.aspiration + "\n" + "Matriz tabulist: ");
        for (int i = 0 ; i < problemModel.getSize(); i++){
            for (int j = 0 ; j < problemModel.getSize(); j++){
                this.tabuList[i][j] = -(problemModel.getSize() * i + j);
                //Console.OUT.print(this.tabuList(i,j) + " ");
            }
            //Console.OUT.println("\n");
        }
    }

    public int search(int currentCost, int bestCost, int nIter) {
        int i, j;
        int newCost;
        int delta;
        int minDelta = Integer.MAX_VALUE;
        move.setFirst(Integer.MAX_VALUE);
        move.setSecond(Integer.MAX_VALUE);
        alreadyAspired = false;

        //Utils.show("Solution",super.variables);

        for (i = 0; i < problemModel.getSize() - 1; i++){
            for (j = i + 1; j < problemModel.getSize(); j++) {
                newCost = problemModel.costIfSwap(currentCost,i,j);
                //System.out.println("Costifswap in RoTS " + i + "," + j + ": " + newCost);
                delta = newCost - currentCost;

                autorized =
                        (tabuList [i][variables[j]] < nIter) ||
                                (tabuList [j][variables[i]] < nIter);

                aspired =
                        (tabuList[i][variables[j]] < (nIter - aspiration)) ||
                                (tabuList[j][variables[i]] < (nIter - aspiration)) ||
                                (newCost < bestCost);

                if ((aspired && !alreadyAspired) ||	/* first move aspired */
                        (aspired && alreadyAspired &&	/* many move aspired */
                                (delta <= minDelta)) ||	/* => take best one */
                        (!aspired && !alreadyAspired &&	/* no move aspired yet */
                                (delta <= minDelta) && autorized)) {

                    //   #ifdef USE_RANDOM_ON_BEST
                    //   if (delta[i][j] == min_delta){
                    // 		if (Random(++best_nb) > 0)
                    // 			 continue;
                    //   }
                    //   else
                    // 		best_nb = 1;
                    //   #endif

                    move.setFirst(i);
                    move.setSecond(j);
                    minDelta = delta;

                    // #ifdef FIRST_BEST
                    // if (current_cost + min_delta < best_cost)
                    // goto found;
                    // #endif

                    if (aspired)
                        alreadyAspired = true;
                }
            }
        }


        if(move.getFirst() == Integer.MAX_VALUE){
            //System.out.println("All moves are tabu!");
            return currentCost;
        }else{
            //System.out.println("swap pos "+move.getFirst()+" "+move.getSecond());
            swapVariables(move.getFirst(), move.getSecond()); //adSwap(maxI, minJ,csp);	
            nSwap++;
            //sz =  super.problemModel.size;
            problemModel.executedSwap(move.getFirst(), move.getSecond(), variables);
            /* forbid reverse move for a random number of iterations */

            //tabuList( move.getFirst(), cop_.variables(move.getSecond())) = this.nIter + (cube() * this.tabuDuration) as Int;
            int t1, t2;
            // t1 = (cube() * this.tabuDuration) as Int; 
            // t2 = (cube() * this.tabuDuration) as Int;
            do t1 = (int) (cube() * tabuDuration); while(t1 <= 2);
            do t2 = (int) (cube() * tabuDuration); while(t2 <= 2);


            tabuList[move.getFirst()][variables[move.getSecond()]] = nIter + t1;
            tabuList[move.getSecond()][variables[move.getFirst()]] = nIter + t2;

            //Utils.show("after swap", super.variables);
            // detect loc min
            //if (minDelta >= 0)
            //	onLocMin();

            //System.out.println("ROT end search iteratrion ");
            return currentCost + minDelta;
        }
    }

    public int randomInterval(int low, int up) {
        return (int)(ThreadLocalRandom.current().nextDouble()*(up - low + 1)) + low;
    }

    private double cube(){
        double ran1 = ThreadLocalRandom.current().nextDouble();
        if (tabuDurationFactorUS < 0)
            return ran1;
        return ran1 * ran1 * ran1;
    }

    /**
     *  Create RoTS Solver State array to be send to Pool
     *  oeState(0) = solverType  
     *  oeState(1) = RoTS tabu duration Factor * 100
     *  oeState(2) = RoTS aspiration Factor * 100
     */
    protected int[] createSolverState() {
        int[] rotsState = new int[3];
        rotsState[0] = getMySolverType().getValue();
        rotsState[1] = (int) (tabuDurationFactor * 10.0);
        rotsState[2] = (int) (aspirationFactor * 10.0);
        return rotsState;
    }

    /**
     *  Process Solver State Array received from Pool
     *
     */
    protected void processSolverState(int[] state){
        // Random Search has no parameters to process

        int inSolverType = state[0];

        if (inSolverType == getMySolverType().getValue()){
            int intdf = (int) (state[1]/ 10.0);
            int inaf = (int) (state[2] / 10.0);

            // this.tabuDurationFactor = intdf;
            // this.aspirationFactor = inaf;

            tabuDurationFactor = (tabuDurationFactor + intdf) / 2.0;
            aspirationFactor = (aspirationFactor + inaf) / 2.0;

            if (tabuDuration != -1)
            tabuDuration = (int) (tabuDurationFactor * problemModel.getSize());

            aspiration = (int) (aspirationFactor * problemModel.getSize() * problemModel.getSize());
        }
    }

    //public def restartVar(){
    //	this.tabuList.clear();
    //}

    /**
     *  Interact when Loc min is reached
     */
    //private def onLocMin(){
    // communicate Local Minimum
    // solver.communicateLM( this.currentCost, cop.getVariables() as Valuation(sz));
    //solverState = this.createSolverState();
    //this.solver.communicateLM( new State(sz, this.currentCost, cop.getVariables() as Valuation(sz), here.id as Int, solverState) );
    //}
}