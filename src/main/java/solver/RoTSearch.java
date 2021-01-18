package main.java.solver;

import main.java.model.QAPModel;

import java.util.Arrays;

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
        super.mySolverType = 1; //TODO CPLSOptionsEnum.HeuristicsSupported.RoTS_SOL;
    }

    public void configHeuristic(QAPModel problemModel /*, opts:ParamManager*/){
        super.configHeuristic(problemModel/*, opts*/);
        this.tabuList = new int[problemModel.getSize()][problemModel.getSize()];
        //this.tabuDurationFactorUS = opts("-RoTS_t", -1.0);
        //this.aspirationFactorUS = opts("-RoTS_a", -1.0);
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
        if (this.tabuDurationFactorUS < 0){
            this.tabuDurationFactor = -this.tabuDurationFactorUS;
        } else {
            this.tabuDurationFactor = this.tabuDurationFactorUS;
        }
        //Console.OUT.println("this.tabuDurationFactor: " + this.tabuDurationFactor);
        this.tabuDuration = (int)(this.tabuDurationFactor * super.problemModel.getSize());
        //Console.OUT.println("this.tabuDuration: " + this.tabuDuration);
        if (this.aspirationFactorUS == -1.0)
            this.aspirationFactor = al + (au - al) * random.nextDouble();
        else
            this.aspirationFactor = this.aspirationFactorUS;
        //Console.OUT.println("this.aspirationFactor: " + this.aspirationFactor);

        this.aspiration = (int) (this.aspirationFactor * super.problemModel.getSize() * super.problemModel.getSize());
        //Console.OUT.println("this.aspiration: " + this.aspiration + "\n" + "Matriz tabulist: ");
        for (int i = 0 ; i < super.problemModel.getSize(); i++){
            for (int j = 0 ; j < super.problemModel.getSize(); j++){
                this.tabuList[i][j] = -(super.problemModel.getSize() * i + j);
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
        this.alreadyAspired = false;

        //Utils.show("Solution",super.variables);

        for (i = 0; i < super.problemModel.getSize() - 1; i++){
            for (j = i + 1; j < super.problemModel.getSize(); j++) {
                newCost = problemModel.costIfSwap(currentCost,i,j);
                //Console.OUT.println("Costifswap in RoTS " + i + "," + j + ": " + newCost);
                delta = newCost - currentCost;

                this.autorized =
                        (tabuList [i][super.variables[j]] < nIter) ||
                                (tabuList [j][super.variables[i]] < nIter);

                this.aspired =
                        (tabuList[i][super.variables[j]] < nIter - this.aspiration) ||
                                (tabuList[j][super.variables[i]] < nIter - this.aspiration) ||
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
            System.out.println("All moves are tabu!");
            return currentCost;
        }else{
            //Console.OUT.println("swap pos "+move.getFirst()+" "+move.getSecond());
            swapVariables(move.getFirst(), move.getSecond()); //adSwap(maxI, minJ,csp);	
            nSwap++;
            //sz =  super.problemModel.size;
            super.problemModel.executedSwap(size, move.getFirst(), move.getSecond(), super.variables);
            /* forbid reverse move for a random number of iterations */

            //tabuList( move.getFirst(), cop_.variables(move.getSecond())) = this.nIter + (cube() * this.tabuDuration) as Int;
            int t1, t2;
            // t1 = (cube() * this.tabuDuration) as Int; 
            // t2 = (cube() * this.tabuDuration) as Int; 
            do t1 = (int) (cube() * this.tabuDuration); while(t1 <= 2);

            do t2 = (int) (cube() * this.tabuDuration); while(t2 <= 2);


            tabuList[move.getFirst()][super.variables[move.getSecond()]] = nIter + t1;
            tabuList[move.getSecond()][super.variables[move.getFirst()]] = nIter + t2;

            //Utils.show("after swap", super.variables);
            // detect loc min
            //if (minDelta >= 0)
            //	onLocMin();

            return currentCost + minDelta;
        }

    }

    public int randomInterval(int low, int up) {
        return (int)(random.nextDouble()*(up - low + 1)) + low;
    }

    private double cube(){
        double ran1 = random.nextDouble();
        if (this.tabuDurationFactorUS < 0)
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
        rotsState[0] = super.mySolverType;
        rotsState[1] = (int) (this.tabuDurationFactor * 10.0);
        rotsState[2] = (int) (this.aspirationFactor * 10.0);
        return rotsState;
    }

    /**
     *  Process Solver State Array received from Pool
     *
     */
    protected void processSolverState(int[] state){
        // Random Search has no parameters to process

        int inSolverType = state[0];

        if (inSolverType == super.mySolverType){
            int intdf = (int) (state[1]/ 10.0);
            int inaf = (int) (state[2] / 10.0);

            // this.tabuDurationFactor = intdf;
            // this.aspirationFactor = inaf;

            this.tabuDurationFactor = (this.tabuDurationFactor + intdf) / 2.0;
            this.aspirationFactor = (this.aspirationFactor + inaf) / 2.0;

            if (this.tabuDuration != -1)
            this.tabuDuration = (int) (this.tabuDurationFactor * super.problemModel.getSize());

            this.aspiration = (int) (this.aspirationFactor * super.problemModel.getSize() * super.problemModel.getSize());
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