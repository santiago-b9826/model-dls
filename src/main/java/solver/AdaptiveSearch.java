package main.java.solver;

import main.java.model.QAPModel;

import java.util.Arrays;

public class AdaptiveSearch extends Metaheuristic{
    private int[] mark;
    private int listInb;

    private MovePermutation[] listIJ;
    private int[] listI;

    private int nVarMarked = 0;

    /**	Statistics	*/
    private int nReset;
    private int nSameVar;
    private int nLocalMin;

    /** Total Statistics */
    private int nResetTot;
    private int nSameVarTot;
    private int nLocalMinTot;
    private int nInPlateau;

    /** Parameters of the AS solver */
    private int nVarToReset;
    private int resetPercent;
    private boolean exhaustive;
    private int freezeLocMin;
    private int freezeSwap;
    private int resetLimit;
    private int probSelectLocMin;
    private boolean firstBest;

    public AdaptiveSearch(int size){
        super(size);
        super.mySolverType = 2; //TODO CPLSOptionsEnum.HeuristicsSupported.AS_SOL;
    }

    public void configHeuristic(QAPModel problemModel/*, opts:ParamManager*/){
        super.configHeuristic(problemModel/*, opts*/);
        this.mark = new int[problemModel.getSize()];
        this.listIJ = new MovePermutation[problemModel.getSize()];
        this.listI = new int[problemModel.getSize()];
        /*this.nVarToReset = opts("--AS_varToReset",-1);
        this.resetPercent = opts("--AS_resetPer",10n);
        this.freezeLocMin = opts("--AS_freezeLocMin",5n);
        this.freezeSwap = opts("--AS_freezeSwap",5n);
        this.resetLimit = opts("--AS_resetLimit",5n);
        this.probSelectLocMin = opts("--AS_probSelecLocMin", 0n);
        this.firstBest = opts("--AS_firstBest",0n) == 1n;
        this.exhaustive = opts("--AS_exhaustive",0n) == 1n;
        */
    }


    public void initVar(){
        super.initVar();
        if (this.nVarToReset == -1){
            this.nVarToReset = (((super.problemModel.getSize() * resetPercent) + 99) / 100);
            if (this.nVarToReset < 2){
                this.nVarToReset = 2;
            }
        }
        Arrays.fill(mark, 0);
        Arrays.fill(listI, 0);
        Arrays.fill(listIJ, null);
        //nRestart = 0n;
        nSameVar = 0;
        nLocalMin = 0;
        nReset = 0;
        nInPlateau = 0;
        nResetTot = 0;
        nSameVarTot = 0;
        nLocalMinTot = 0;
    }

    /**
     *  Search process (in loop functionality)
     *  To be overwrited for each child class (solver) 
     */
    public int search(int currentCost, int bestCost, int nIter) {
        int newCost = -1;
        //sz = this.problemModel.size;
        if( !this.exhaustive ){
            selectVarHighCost(problemModel);
            newCost = selectVarMinConflict(currentCost);
        } else {
            newCost = selectVarsToSwap(currentCost);
        }
        if (currentCost != newCost) {
            nInPlateau = 0;
        }
        nInPlateau++;
        int returnCost = currentCost;
        if (move.getFirst() == move.getSecond()){
            this.nLocalMin++;
            mark[move.getFirst()] = super.nSwap + this.freezeLocMin; //Mark(maxI, freeze_loc_min);

            if (this.nVarMarked + 1 >= this.resetLimit){
                onLocMin();
                returnCost = doReset(this.nVarToReset, currentCost);//doReset(nb_var_to_reset,csp);
                //returnCost = currentCost;
            }
            //return otherCost;
        }else{
            mark[move.getFirst()] = super.nSwap + this.freezeSwap; //Mark(maxI, ad.freeze_swap);
            mark[move.getSecond()] = super.nSwap + this.freezeSwap; //Mark(minJ, ad.freeze_swap);
            swapVariables(move.getFirst(), move.getSecond()); //adSwap(maxI, minJ,csp);
            super.nSwap++;
            problemModel.executedSwap(size, move.getFirst(), move.getSecond(), super.variables);
            //currentCost = newCost;
            returnCost = newCost ;
        }
        //sz = problemModel.getSize();
        //variables:Rail[Int] = new Rail[Int](sz as Int, 0n);
        //Rail.copy(problemModel.getVariables(), variables as Valuation(sz));
 		/*if(returnCost < currentCost){
 			Console.OUT.print("Costo (AdaptiveSearch): " + returnCost);
 			Utils.show(". Con variables: " , super.variables);
 		}*/
        //displaySolution();
        //Console.OUT.print("\n");
        return returnCost;
    }

    public void restartVar(){
        //super.restartVar();
        Arrays.fill(mark, 0);
    }

    private int doReset(int n, int currentCost) {
        int cost = -1;		//reset(n, csp);
        cost = reset(n, currentCost);
        Arrays.fill(mark, 0);
        nReset++;
        return (cost < 0) ? this.problemModel.costOfSolution(size, true, super.variables) : cost; //Arg costofsol(1)
    }

    /**
     * 	SelectVarHighCost 
     * 	Select the maximum cost variable of the problem 
     *   (Modify the first variable of the move object )
     *   Also computes the number of marked variables.
     *
     *
     */
    private void selectVarHighCost(QAPModel problemModel){
        int i =-1;
        int maxCost = 0;
        int maxVar = -1;
        this.listInb = 0; //Number of elements
        this.nVarMarked = 0;

        while((i = problemModel.nextI(i)) < super.problemModel.getSize()) { //False if i < 0
            if (super.nSwap < this.mark[i]) {
                this.nVarMarked++;
                continue;
            }
            int x = problemModel.costOnVariable(i);
            if (x >= maxCost){
                if (x > maxCost){
                    maxCost = x;
                    this.listInb = 0;
                }
                this.listI[this.listInb++] = i;
            }
        }
        if (this.listInb == 0) // all variables are OK but the global cost is > 0 (can occur in SMTI with no BP but singles)
        maxVar = random.nextInt(super.problemModel.getSize());
 		else {
            // select a maxCost variable from array
            int sel = random.nextInt(this.listInb);
            maxVar = this.listI[sel]; //This maxI must be local or only returns the value
        }
        this.nSameVar += this.listInb;
        super.move.setFirst(maxVar);
    }

    /**
     * 	selectVarMinConflict( csp : ModelAS) : Int
     * 	Computes swap and selects the minimum of cost if swap
     *
     * 	@return new cost of the possible move
     */
    private int selectVarMinConflict(int currentCost) {
        int j;
        int cost;
        boolean flagOut = false;
        int second = -1;
        int nCost;
        int first = move.getFirst();

        do {
            flagOut = false;
            int listJnb = 0;
            nCost = currentCost;
            j = -1;
            while((j = problemModel.nextJ(first, j, false)) < super.problemModel.getSize()){// false if j < 0 //solverP.exhaustive???
                if (super.nSwap < this.mark[j]) {
                    continue;
                }
                cost = problemModel.costIfSwap(currentCost, j, first);
                if (this.probSelectLocMin <= 100 && j == first) continue;

                if (cost < nCost){
                    listJnb = 1;
                    nCost = cost;
                    second = j;
                    if (this.firstBest){
                        super.move.setSecond(second);
                        return nCost;
                    }
                } else if (cost == nCost){
                    if (random.nextInt(++listJnb) == 0)
                    second = j;
                }
            }

            if (this.probSelectLocMin <= 100) {
                if (nCost >= currentCost && (random.nextInt(100) < this.probSelectLocMin ||(this.listInb <= 1 && listJnb <= 1))) {
                    second = first;
                    super.move.setSecond(second);
                    return nCost;
                }
                if (listJnb == 0) {
                    //this.nIter++;
                    int sel = random.nextInt(this.listInb);
                    first = listI[sel];
                    super.move.setFirst(first);
                    flagOut = true;
                }
            }
        } while(flagOut);
        super.move.setSecond(second);
        return nCost;
    }

    /**
     *  Computes maxI and minJ, the 2 variables to swap.
     *  All possible pairs are tested exhaustively.
     *
     *  @return new cost of the possible move
     */
    private int selectVarsToSwap(int currentCost) {
        int first;
        int second;
        //x : Int;
        /** For Exhaustive search */
        int nListIJ = 0;
        int nCost = Integer.MAX_VALUE ;
        this.nVarMarked = 0;
        first = -1;

        while((first = problemModel.nextI(first)) < super.problemModel.getSize()) {
            if (super.nSwap < this.mark[first]) {
                this.nVarMarked++;
                continue;
            }
            second = -1;
            while((second = problemModel.nextJ(first, second, true)) < super.problemModel.getSize()){
                if (super.nSwap < this.mark[second]) {
                    continue;
                }
                int x = problemModel.costIfSwap(currentCost, first, second);
                if (x <= nCost) {
                    if (x < nCost) {
                        nCost = x;
                        nListIJ = 0;
                        if (this.firstBest && x < currentCost) {
                            super.move.setFirst(first);
                            super.move.setSecond(second);
                            return nCost;
                        }
                    }
                    this.listIJ[nListIJ] = new MovePermutation(first,second);
                    nListIJ = ((nListIJ + 1) % super.problemModel.getSize());
                }
            }
        }

        this.nSameVar += nListIJ;
        if (nCost >= currentCost) {
            if (nListIJ == 0 || (( this.probSelectLocMin <= 100) && random.nextInt(100) < this.probSelectLocMin)) {
                int i;
                for(i = 0; super.nSwap < mark[i]; i++){}
                super.move.setFirst(i);
                super.move.setSecond(i);
                return nCost;//goto end;
            }

            int lm;
            if (!(this.probSelectLocMin <= 100) && (lm = random.nextInt(nListIJ + super.problemModel.getSize())) < super.problemModel.getSize()) {
                super.move.setFirst(lm);
                super.move.setSecond(lm);
                return nCost;//goto end;
            }
        }

        int sel = random.nextInt(nListIJ);
        super.move.setFirst(listIJ[sel].getFirst());
        super.move.setSecond(listIJ[sel].getSecond());
        return nCost;
    }

    /**
     * 	Report statistics from the solving process
     */
    /*public void reportStats( GlobalStats c){
        //super.reportStats(c);
        c.locmin = this.nLocalMinTot;
        c.reset = this.nResetTot;
        c.same = this.nSameVarTot;
    }*/

    protected void updateTotStats(){
        //super.updateTotStats();
        nResetTot += nReset;
        nSameVarTot += nSameVar;
        nLocalMinTot += nLocalMin;
        nSameVar = 0;
        nLocalMin = 0;
        nReset = 0;
    }

    /**
     *  Interact when Loc min is reached
     */
    private void onLocMin(){
        // communicate Local Minimum
        //solver.communicateLM( this.currentCost, cop.getVariables() as Valuation(sz));
        //solverState = createSolverState();
        //solver.communicateLM( new State(sz,this.currentCost, cop.getVariables() as Valuation(sz), here.id as Int, solverState) );
    }
}