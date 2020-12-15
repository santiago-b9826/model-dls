package main.java.solver;

import main.java.model.QAPModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EOSearch extends Metaheuristic{

    private double[] pdf;
    private int[] fit;

    public enum Func {
        POWER(0), EXPONENTIAL(1), GAMMA(2);

        private static final Map<Integer, Func> mappingMap = new HashMap<Integer, Func>();

        static {
            for (Func m : Func.values()) {
                mappingMap.put(m.getValue(), m);
            }
        }
        private final int func;

        Func(int aFunc) {
            func = aFunc;
        }

        public int getValue() {
            return func;
        }
        public static Func getByFunc(int aFunc) {
            return mappingMap.get(aFunc);
        }
    }

    private static final RandomEnum<Func> randomFunc =
            new RandomEnum<Func>(Func.class);

    // var index is stored in the 10 LSB and the cost is stored on the remaining MSB
    /*public static int cmp (int a, int b){
        return((b >> 10) - (a >> 10));
    }*/

    private double powFunc(double tau, int x){ return Math.pow(x, -tau);}
    private double expFunc(double tau, int x){ return Math.exp(-tau * x);}
    private double gammaFunc(double tau, int x){
        double theta = Math.exp(tau);
        double constK =  Math.pow(theta, tau) * this.gamma(tau);
        double f =  Math.pow(x, tau - 1) * Math.exp(-x / theta) / constK;
        return f;
    }

    private double gamma(double n) {
        double invn = 1.0 / n;
        double g = ( 2.506628274631 * Math.sqrt(invn) +
                0.208885689552583 * Math.pow(invn, 1.5) +
                0.00870357039802431 * Math.pow(invn, 2.5) -
                (174.210665086855 * Math.pow(invn, 3.5)) / 25920.0 -
                (715.642372407151 * Math.pow(invn, 4.5)) / 1244160.0
        ) * Math.exp((-Math.log(invn) - 1) * n);
        return g;
    }

    // Communication Variables
    private double  tauUserSel;
    private double  tau;
    private int pdfUserSel; //pdf initial selection
    private Func pdfS = Func.POWER;  //pdf state
    private int selSecond;
    private double expDown;
    private double expUp;
    private double powDown;
    private double powUp;

    public EOSearch (int size){
        super(size);
        // TODO: create enum
        super.setMySolverType(1); 
    }

    public void configHeuristic(QAPModel problemModel, ParamManager opts){
        super.configHeuristic(problemModel, opts);
        this.pdf = new double[problemModel.getSize() + 1];// +1 since x in 1..size
        this.fit = new int[problemModel.getSize()];
        this.expDown = 6.385378048 * Math.pow(problemModel.getSize(), -1.033400799);
        this.expUp = 8.867754442 * Math.pow(problemModel.getSize(), -0.895936426);
        this.powDown = 1.575467001 * Math.pow(problemModel.getSize(), -0.1448643794);
        this.powUp = 2.426369897 * Math.pow(problemModel.getSize(), -0.1435045369);
        //Jason: Se cambia la forma como estan siendo leidos los parametros.
        this.tauUserSel = opts("-EO_t", (1.0 + 1.0 / Math.log(problemModel.getSize())));
        this.pdfUserSel = opts("-EO_p", -1);
        this.selSecond = opts("-EO_ss", 1);
    }

    /**
     *  Initialize variables of the solver
     *  Executed once before the main solving loop
     */
    public void initVar(){
        super.initVar();
        //Console.OUT.println("Se llega al initvar de EOSearch");
        if ( this.pdfUserSel == -1 ) { // Select a random PDF
            this.pdfS = randomFunc.random();
        } else {
            if (pdfUserSel == 1) {
                this.pdfS = Func.POWER;
            } else if(pdfUserSel == 2){
                this.pdfS = Func.EXPONENTIAL;
            } else { //if(pdfUserSel == 3){
                this.pdfS = Func.GAMMA;
            }
        }

        if ( this.tauUserSel == -1.0 ) { // Select a random tau from 0 to tau
            if ( this.pdfS == Func.POWER) {
                this.tau = this.powDown + (powUp - powDown) * random.nextDouble();
            }else if (this.pdfS == Func.EXPONENTIAL) {
                this.tau = this.expDown + (expUp - expDown) * random.nextDouble();
            }
        }else {
            this.tau = this.tauUserSel;
        }
        initPDF(this.pdfS);
    }

    private void initPDF(Func func){
        double sum = 0.0;
        double  y = 0.0;

        for (int x = 1; x <= super.problemModel.getSize(); x++){
            if (func == Func.GAMMA){
                y = this.gammaFunc(this.tau, x);
            } else if (func == Func.EXPONENTIAL){
                y = this.expFunc(this.tau, x);
            }
            else if (func == Func.POWER){ //( this.pdfS == 1n )
                y = this.powFunc(this.tau, x);
            }
            this.pdf[x] = y;
            sum += y;
        }
        for (int x = 1; x <= super.problemModel.getSize(); x++){
            this.pdf[x] /= sum;
            //Console.OUT.println("Se inicializa pdf, posiciion: " + x + ". Valor: " + this.pdf(x));
        }
        // for (x in pdf.range())
    }

    /**
     *  Extremal Search process (in loop functionality)
     */
    public int search(int currentCost, int bestCost, int nIter) {
        this.selFirstVar(this.move, problemModel);
        //val sz = this.problemModel.getSize();
        int newCost = currentCost;
        if (this.selSecond == 0){
            newCost = this.selSecondRandom(super.move, problemModel, currentCost);
        } else {
            newCost = this.selSecondMinConf(super.move, problemModel, currentCost);
        }
        swapVariables(super.move.getFirst(), super.move.getSecond()); //adSwap(maxI, minJ,csp);
        super.nSwap++;
        this.problemModel.executedSwap(size, super.move.getFirst(), super.move.getSecond(), super.variables);
 		/*if(newCost < bestCost){
 			Console.OUT.print("Costo (EOSearch) in " + here + ". " + Runtime.worker() + ": " + newCost);
 			Utils.show(". Con variables: " , super.variables);
 			//displaySolution();
 		}*/
        return newCost;
    }


    private int pdfPick() {
        double p = random.nextDouble();
        double fx;
        int x = 0;
        while( (fx = pdf[++x]) < p ){
            //Console.OUT.println("Valor fx: " + fx + ". Valor p: " + p);
            p -= fx;
        }
        return x - 1 ;
    }

    private void selFirstVar(MovePermutation move, QAPModel problemModel){
        int i = -1;
        int cost;
        int selIndex = 0;
        boolean locMin = true;
        while((i = i + 1) < super.problemModel.getSize()) { //False if i < 0
            cost = problemModel.costOnVariable(i);
            // each position on the fit array is divided to contain both i and cost
            // variable index "i" is stored in the 10 LSB
            // the cost is stored in the remaining MSB
            this.fit[i] = cost << 10 | i;
            //Console.OUT.printf("%d %X %X \n",cost,cost,fit(i));
            // Detect local min:
            // can be applied on "first variable selction" only for QAP
            if ( cost > 0 ) // cost is -delta for QAP.
                locMin = false;
        }

        this.fit = Arrays.stream(fit)
                .boxed()
                .sorted((a, b) -> (b >> 10) - (a >> 10))
                .mapToInt(si -> si)
                .toArray();

        if (locMin) this.onLocMin();
        // for (v in fit)
        //   Console.OUT.printf("%d %d \n",(v & 0xFFF),(v >>12));
        int index = this.pdfPick();
        int sVar = this.fit[index] & 0x3FF;
        int sCost = this.fit[index] >> 10;
        //Console.OUT.printf("svar %d scost %d \n",sVar,sCost);
        int nSameFit = 0;
        for(int k =0; k < super.problemModel.getSize(); k++){
            int cCost = this.fit[k] >> 10;
            //Console.OUT.printf("cCost %d scost %d \n",cCost,sCost);
            if ( cCost < sCost)   // descending order
                break;

            if (cCost == sCost && random.nextInt(++nSameFit) == 0)
            selIndex = fit[k] & 0x3FF;
        }
        // Save first variable selected into the move object
        this.move.setFirst(selIndex);
    }

    /**
     * 	selectVarMinConflict( csp : ModelAS) : Int
     * 	Computes swap and selects the minimum of cost if swap
     * 	@param problemModel problem model
     *   @param move object
     * 	@return the cost of the best move
     */
    private int selSecondMinConf(MovePermutation move, QAPModel problemModel, int currentCost) {
        int j;
        int cost;
        int second = 0;
        int nSameMin = 0;
        int minCost = Integer.MAX_VALUE;
        int first = this.move.getFirst();
        for (j = 0; j < super.problemModel.getSize(); j++){
            if (first == j) continue;
            cost = problemModel.costIfSwap(currentCost, j, first);
            if (cost < minCost){
                minCost = cost;
                second = j;
                nSameMin = 1;
            } else if (cost == minCost && random.nextInt(++nSameMin) == 0){
                second = j;
            }
        }
        this.move.setSecond(second);
        return minCost;
    }

    private int selSecondRandom(MovePermutation move, QAPModel problemModel, int currentCost) {
        int randomJ = random.nextInt(super.problemModel.getSize());
        int newCost = problemModel.costIfSwap(currentCost, randomJ, this.move.getFirst());
        this.move.setSecond(randomJ);
        return newCost;
    }

    /**
     *  Interact when Loc min is reached
     */
    private void onLocMin(){
        // communicate Local Minimum
        // solver.communicateLM( this.currentCost, cop.getVariables() as Valuation(sz));
        int[] solverState = this.createSolverState();
        //this.solver.communicateLM( new State(sz, this.currentCost, problemModel.getVariables() as Valuation(sz), here.id as Int, solverState) );
    }

    /**
     *  Create EO Solver State array to be send to Pool
     *  oeState(0) = solverType
     *  oeState(1) = EO pdf type
     *  oeState(2) = EO "tau" value
     */
    private int[] createSolverState() {
        int[] eoState = new int[3];
        eoState[0] = super.getMySolverType();
        eoState[1] = this.pdfS.getValue();
        eoState[2] = (int)(this.tau * 1000.0); // TODO: convert double to Int??? levels ranges ???
        return eoState;
    }

    /**
     *  Process Solver State Array received from Pool
     *
     */
    protected void processSolverState( int[] state){
        // Random Search has no parameters to process
        int inSolverType = state[0];
        if (inSolverType == super.mySolverType){
            int inpdf = state[1];
            int intau = (int) (state[2] / 1000.0);
            if (this.pdfS.getValue() == inpdf) {
                this.tau = intau;
            } else {
                if (this.pdfS == Func.POWER) {
                    this.tau = this.powDown + (powUp - powDown) * random.nextDouble();
                }else if (this.pdfS == Func.EXPONENTIAL) {
                    this.tau = this.expDown + (expUp - expDown) * random.nextDouble();
                }
            }
            initPDF(this.pdfS);
        }
    }
    private static class RandomEnum<E extends Enum<E>> {

        private static final Random RND = new Random();
        private final E[] values;

        public RandomEnum(Class<E> token) {
            values = token.getEnumConstants();
        }

        public E random() {
            return values[RND.nextInt(values.length)];
        }
    }
}