package main.java;

import main.java.model.QAPModel;
import main.java.solver.Metaheuristic;

import java.util.Random;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

public class Worker extends RecursiveAction {
    private Metaheuristic metaheuristic;
    private int id;

    private int reportI;
    private int updateI;

    private Pool reportPool;
    private Pool updatePool;


    private int currentCost;
    private int[] bestConf;
    private int bestCost;
    private boolean bestSent = false;

    private int target = 0;
    private boolean strictLow = false;
    private boolean targetSucc = false;

    // -> Statistics
    private int nRestart = 0;
    private int nIter;
    /** Total Statistics */
    private int nIterTot;
    private int nSwapTot;
    private int itersWhitoutImprovements;

    public int getnChange() {
        return nChange;
    }

    private int nChange;

    private long initialTime;

    private Metaheuristic.Type MHType;

    private AtomicBoolean kill;

    public Worker(int id, int size, int type, QAPModel model, int reportI, int updateI, Pool reportPool, Pool updatePool) {
        super();
        this.updatePool = updatePool;
        this.reportPool = reportPool;
        this.reportI = reportI;
        this.updateI = updateI;
        this.id = id;
        bestConf = new int[size];
        this.MHType = Metaheuristic.Type.getByType(type);
        System.out.println("Worker in QAP: "+ model);
        metaheuristic =  Metaheuristic.make(MHType, size);
        metaheuristic.configHeuristic(model);
        //metaheuristic.configHeuristic(new QAPModel(model));
    }

    @Override
    public void compute() {
        int cost;

        System.out.println("Starting solving process "+ MHType.toString()+"-"+id);
        initialTime = System.nanoTime();
        cost = solve();
        double exTime = (System.nanoTime() - initialTime)/1e6;

        System.out.println("Solving process finished Meta type "+ MHType.toString()+"-"+id+". Time: "+exTime+" ms best cost: "+ bestCost);
    }


    protected void initVar(int tCost, boolean sLow){
        metaheuristic.initVar();
        metaheuristic.initVariables();

        target = tCost;
        strictLow = sLow;
        targetSucc = false;
        nIter = 0;
        nRestart = 0;
        bestConf = new int[metaheuristic.getSizeProblem()]; //new Rail[Int](this.heuristicSolver.getSizeProblem(), 0n);
        // clear Tot stats
        nIterTot = 0;
        //Jason: Migration begin
        itersWhitoutImprovements = 0;
        //this.nItersForUpdate = 0n;
        //Jason: Migration end
        nSwapTot = 0;
        initialTime = System.nanoTime();
        // Comm
        //bestSent = false;
        //nForceRestart = 0n;
        nChange = 0;
        //nChangeforiwi = 0n;
        //attempsChangeForIwi = 0n;

        //if (this.nodeConfig.getAdaptiveComm())
          //  this.nodeConfig.setUpdateI(2n * this.nodeConfig.getReportI());
    }

    public void setWorker(AtomicBoolean kill, int tCost, boolean sLow){
        this.kill = kill;
        target = tCost;
        strictLow = sLow;
    }


    public int solve() {
        System.out.println("WORKER " +MHType+" pasando por solve.");
        initVar(target, strictLow);
        currentCost = metaheuristic.costOfSolution();
        bestConf = metaheuristic.getVariables().clone();

        System.out.println(MHType.toString()+": initial cost= "+ currentCost);

        bestCost = currentCost; //Best solution is the initial solution

        while(currentCost >= 0){
            if (nIter >= 1000000){ //TODO: get parameter//this.nodeConfig.getMaxIters()){
                //restart or finish
                if(nRestart >= 0){ //TODO: get parameter //this.nodeConfig.getMaxRestarts()){
                    break;
                }else{
                    nRestart++;
                    metaheuristic.initVariables();
                    currentCost = metaheuristic.costOfSolution();
                    updateTotStats();
                    restartVar();
                    //bestSent = false;
                    continue;
                }
            }

            nIter++;
            currentCost = metaheuristic.search(currentCost, bestCost, nIter);

            //Update the best configuration found so far
            updateCosts();

            //Kill solving process
            if(kill.get()){
                break;  // kill: End solving process
            }

            //System.out.println("Type:"+MHType.toString()+"-"+id+" In main LOOP  time "+(System.nanoTime() - this.initialTime)/1e6 +" cost="+this.currentCost);
            //Time out
            int maxTime = 100000;
            if(maxTime > 0 ){ //TODO: get parameter //nodeConfig.getMaxTime() > 0){
                double eTime = System.nanoTime() - this.initialTime;
                if(eTime/1e6 >= maxTime){ //comparison in miliseconds
                    //Console.OUT.print("MsgType_0. Nodo " + here.id + ", finalizacion por Time Out: (tiempo) " + eTime/1e6
                    //	 + ". mi costo: " + bestCost + ", mis variables: ");
                    //printVector(bestConf);
                    //Logger.debug(()=>{" Time Out"});
                    System.out.println("Time out!");
                    break;
                }
            }
            //val eTime = System.nanoTime() - this.initialTime;
            //if((eTime/1e6)/30000 > count){
            //	count++;
            //	this.heuristicSolver.displayInfo();
            //}
            interact();
        }
        //this.heuristicSolver.printPopulation();
        updateTotStats();
        System.out.println(MHType.toString()+"-"+id+": Saliendo best cost: "+ bestCost+ "  iters: "+ nIterTot + " Changes: "+nChange);
        metaheuristic.verify(bestConf);

        for(int i = 0; i < bestConf.length; i++)
            System.out.print(" "+bestConf[i]);
        System.out.println(" ");

        return this.bestCost;
    }



    private void updateCosts(){
        //val sz = this.heuristicSolver.getSizeProblem();
        if(currentCost < bestCost){ //(totalCost <= bestCost)
            bestConf = metaheuristic.getVariables().clone();

            System.out.println("                    "+ MHType.toString() +"-"+id+ ": Current time: " + (System.nanoTime()-initialTime)/1e6 + ". Cost: " + this.currentCost);
            metaheuristic.verify(bestConf);
            bestCost = currentCost;

            bestSent = false; // new best found, I must send it!

            /*if (nodeConfig.getReportPart()){
                val eT = (System.nanoTime() - initialTime)/1e9;
                val gap = (this.bestCost-this.target)/(this.bestCost as Double)*100.0;

                Utils.show("Solution",this.bestConf);
                Console.OUT.printf("%s\ttime: %5.1f s\tbest cost: %10d\tgap: %5.2f%% \n",here,eT,this.bestCost,gap);
            }*/
            if ((strictLow && bestCost < target)
                    || (!strictLow && bestCost <= target)){
                targetSucc = true;
                kill.set(true);
                //Console.OUT.println("Soy nodo " + here + " y he encontrado la solucion");
            }
            //Console.OUT.println("La heuristica consigue mejorar el costo. CPLSNode en " + here);
            itersWhitoutImprovements = 0;

        }else{
            itersWhitoutImprovements++;
        }

        /*if(heuristicSolver instanceof PopulBasedHeuristic){
            if(this.itersWhitoutImprovements%this.iwi == 0n){//this.nodeConfig.getItersWhitoutImprovements())
                this.attempsChangeForIwi++;
                if(this.heuristicSolver.launchEventForStagnation()){
                    this.itersWhitoutImprovements = 0n;
                    this.nChangeforiwi++;
                    val solverState = createSolverState();
                    bestSent = false;
                    Rail.copy(this.heuristicSolver.getVariables() as Valuation(sz), this.bestConf as Valuation(sz));
                    this.bestCost = this.heuristicSolver.costOfSolution();
                }else{
                    //Acá no se pone nada porque se deduce: attempsChangeForIwi - nChangeforiwi = noChangesIWIForDistance
                }
            }
        }*/
        //else{
        //	if(this.itersWhitoutImprovements == this.iwi ){
        //		val result = getIPVector(this.currentCost);
        //		if (result) {
        //			//this.nChangeV++;
        //			restartVar();
        //		}
        //	}
        //}
    }

    private void updateTotStats(){
        //Console.OUT.println("Ingresa a reportar las estadisticas totales");
        nIterTot += nIter;
        nSwapTot += metaheuristic.getNSwap();
        metaheuristic.clearNSwap();
        nIter = 0;
    }

    private void restartVar(){
        bestSent = false;
        //this.heuristicSolver.restartVar();
    }

    private void interact(){
        interactForIntensification();
        //interactForDiversification();
    }

    private void interactForIntensification(){
        if(nIter % reportI == 0){
            reportPool.sendInfo(new ContextInformation(metaheuristic.variables.clone(), currentCost, MHType));
        }
        if(nIter % updateI == 0){
            ContextInformation recv = updatePool.getInfo();
            if(recv.getCost() <= currentCost){
                metaheuristic.variables = recv.getVariables().clone();
                currentCost = metaheuristic.costOfSolution();
                nChange++;
            }
        }

        /*if(nodeConfig.getRol() != CPLSOptionsEnum.NodeRoles.MASTER_NODE){
            if( nodeConfig.getReportI() != 0 && this.nIter %  this.nodeConfig.getReportI() == 0){
                //if(!bestSent){
                //val solverState = createSolverState();
                //communicate();
                bestSent = true;

                //}else{
                //Jason: Esto no parece tener mucho sentido, las probabilidades de que se cumpla el condicional son mínimas
                //	if (random.nextInt(this.nodeConfig.getReportI()) == 0n){
                //val solverState = createSolverState();
                //		communicate();
                //	}
                //}
            }

            if( this.nodeConfig.getUpdateI() != 0n && this.nIter % this.nodeConfig.getUpdateI()  == 0n){// && !this.isOnDiversification)
                //Jason: De acuerdo con las modificaciones realizadas, esto no modifica en nada la ejecución
                if ( this.nodeConfig.getAdaptiveComm() && this.nodeConfig.getUpdateI() < this.nodeConfig.getMaxUpdateI()){
                    this.nodeConfig.setUpdateI(this.nodeConfig.getUpdateI()*2n);
                }
                ///
                //Cambio esta parte para que las LS solo adopten la nueva solución si resulta
                // que ya han pasado iwi iteraciones sin mejora; esto ayuda a dejar que la intensificación
                // opere mientras sea necesario.
                //
                var result:Boolean = false;
                //if(this.heuristicSolver instanceof PopulBasedHeuristic){
                result = getIPVector();
                //}else{
                //	if(this.itersWhitoutImprovements >= this.iwi){
                //		result = getIPVector();
                //	}
                //}
                //
                if (result) {
                    this.nChangeV++;
                    this.itersWhitoutImprovements = 0n;
                    restartVar();
                }
                //this.nItersForUpdate = 0n;
                //Console.OUT.println("Ingresa a Update " + here);
            }
            // Force Restart: Inter Team Communication
            if (this.forceRestart){
                //restart
                Console.OUT.println("Entra en el forceRestart de interactForIntensification() " + here);
                Logger.info(()=>{"   AdaptiveSearch : force Restart"});
                this.forceRestart = false;
                this.nForceRestart++;
                // get a new conf according the diversification approach
                val result = getPR();
                if (result != null){
                    if(this.nodeConfig.getChangeOnDiver() == 1n) {
                        this.heuristicSolver.setVariables(result().vector);
                        this.currentCost = this.heuristicSolver.costOfSolution();
                        restartVar();
                    }
                    //if(this.modParams == 1n) //Solo para RoTS y para EOSearch
                    //processSolverState(result().solverState);
                } else {
                    if(this.nodeConfig.getChangeOnDiver() == 1n) {
                        this.heuristicSolver.initVariables();
                        this.currentCost = this.heuristicSolver.costOfSolution();
                        restartVar();
                    }
                }
            }*/
        }

    //@Override
    //protected void compute() {
    //    start();
    //}
}
