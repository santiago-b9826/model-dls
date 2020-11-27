package main.java.model;

import javafx.scene.control.cell.TextFieldListCell;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Logger;

public class QAPModel {
    private int[][] flow;
    private int[][] dist;
    private int[][] delta;
    private int opt = 0;    /** optimal cost (0 if unknown) */
    private int bound = 0;  /** best bound (0 if unknown) */
    private int bks = 0;    /** best known solution cost (0 if unknown) */
    private int size;
    private Logger logger;
    private int baseValue;

    public QAPModel(int size){
        this.size = size;
        this.logger = Logger.getLogger(QAPModel.class.getName());
        //System.out.println("Constructor de QAPModel invocado");
    }

    public QAPModel(int size, String inPathDataProblem, String inPathVectorSol, int baseValue){
        this.size = size;
        this.flow = new int[size][size];
        this.dist = new int[size][size];
        this.delta = new int[size][size];
    }

    public QAPModel(int size, int[][] mf, int[][] md){
        this.size = size;
        this.flow = mf;
        this.dist = md;
        this.delta = new int[size][size];
    }

    /**
     *  Compute the cost difference if elements i and j are permuted
     */
    public int computeDelta(int size, int i, int j, int[] variables) {
        int pI = variables[i];
        int pJ = variables[j];
        int k, pK;
        int dis = (flow[i][i] - flow[j][j]) * (this.dist[pJ][pJ] - this.dist[pI][pI]) +
                (flow[i][j] - flow[j][i]) * (this.dist[pJ][pI] - this.dist[pI][pJ]);

        for(k = 0; k < i; k++){
            pK = variables[k];
            dis += (this.flow[k][i] - this.flow[k][j]) * (this.dist[pK][pJ] - this.dist[pK][pI]) +
                    (this.flow[i][k] - this.flow[j][k]) * (this.dist[pJ][pK] - this.dist[pI][pK]);
        }

        while(++k < j){
            pK = variables[k];
            dis += (this.flow[k][i] - this.flow[k][j]) * (this.dist[pK][pJ] - this.dist[pK][pI]) +
                    (this.flow[i][k] - this.flow[j][k]) * (this.dist[pJ][pK] - this.dist[pI][pK]);
        }

        while(++k < size){
            pK = variables[k];
            dis += (this.flow[k][i] - this.flow[k][j]) * (this.dist[pK][pJ] - this.dist[pK][pI]) +
                    (this.flow[i][k] - this.flow[j][k]) * (this.dist[pJ][pK] - this.dist[pI][pK]);
        }
        return dis;
    }

    /**
     *  As above, compute the cost difference if elements i and j are permuted
     *  but the value of delta[i][j] is supposed to be known before
     *  the transposition of elements r and s.
     */
    public int computeDeltaPart(int size, int i, int j, int  r, int s, int[] variables) {
        int pI = variables[i];
        int pJ = variables[j];
        int pR = variables[r];
        int pS = variables[s];

        return (delta[i][j] +
                (this.flow[r][i] - this.flow[r][j] + this.flow[s][j] - this.flow[s][i]) *
        (this.dist[pS][pI] - this.dist[pS][pJ] + this.dist[pR][pJ] - this.dist[pR][pI]) +
                (this.flow[i][r] - this.flow[j][r] + this.flow[j][s] - this.flow[i][s]) *
        (this.dist[pI][pS] - this.dist[pJ][pS] + this.dist[pJ][pR] - this.dist[pI][pR]));
    }

    public int costOfSolution(int size, boolean shouldBeRecorded, int[] variables) {
        int r = 0;
        for(int i = 0; i < size; i++)
            for(int j = 0; j < size; j++)
                r += this.flow[i][j] * this.dist[variables[i]][variables[j]];
        if (shouldBeRecorded)
            for(int i = 0; i < size; i++)
                for(int j = i + 1; j < size; j++){
                    delta[i][j] = computeDelta(size, i, j, variables);
                    //System.out.println("DeltaPart calculado");
                }
        //System.out.println("MsgType_0. CostOfSolution ejecutado en QAPModel");
        return r;
    }

    //Jason: New method for calculate costofSolution for an input solution
    public int costOfSolution(int size, int[] solution) {
        int r = 0;
        for(int i = 0; i < size; i++)
            for(int j = 0; j < size; j++)
                r += this.flow[i][j] * this.dist[solution[i]][solution[j]];
        return r;
    }

    public int costIfSwap(int currentCost, int i1, int i2) {
        //return currentCost + delta(i1 as Int , i2 as Int) as Int;
        int i1v = i1;
        int i2v = i2;
        if (i1 > i2){
            i1v = i2;
            i2v = i1;
        }
        //System.out.println("currentCost + delta(i1v, i2v): " + (currentCost + delta(i1v, i2v)));
        return currentCost + delta[i1v][i2v];
    }

    public void executedSwap(int size, int i1, int i2, int[] variables) {
        int temp = variables[i1];
        if (i1 >= i2){
            int tmp = i1;
            i1 = i2;
            i2 = tmp;
        }
        for (int i = 0; i < size; i++)
            for (int j = i + 1; j < size; j++)
                if (i != i1 && i != i2 && j != i1 && j != i2)
                    delta[i][j] = computeDeltaPart(size, i, j, i1, i2, variables);
 				else
                    delta[i][j] = computeDelta(size, i, j, variables);
    }

    /**
     * 	costOnVariable( i : Int ) : Int
     * 	This function computes the cost of individual variable i
     * 	@param i This is the variable that we want to know the cost
     *  @return Int value with the cost of this variable
     */
    public int costOnVariable(int i) {
        int r = Integer.MIN_VALUE;
        for (int j = 0; j < this.size; j++){
            if (i == j) continue;
            int d = (i < j) ? delta[i][j] : delta[j][i];
            d = -d;
            if (d > r) r = d;
        }
        return r;
    }

    /** load data
     *  load the data in filePath to the data structures matrixFlow and matrixDist
     *  @param filePath path of the data file to be loaded
     *  @return true if success, false if filePath is a directory
     */
    public boolean loadData(String filePath) throws FileNotFoundException {
        long loadTime = -System.nanoTime();
        File filep = new File(filePath);
        if (filep.isDirectory()) return false;
        System.out.println("\n--   Solving "+filePath+" ");
        //Load first line with headers size p1 p2
        Scanner fr = new Scanner(filep);
        String fLine = fr.nextLine(); //get first line
        int[] header = readParameters(fLine);
        int sizeF = header[0];
        int opt = header[1];
        int bks = header[2];
        int bound = 0;
        if(opt < 0){
            bound = -opt;
            opt = 0;
        }else{
            bound = opt;
        }
        logger.info("file: "+filePath+" size: "+sizeF+" bound: "+bound+" opt: "+opt+" bks: "+bks);

        //Load Problem
        readMatrix(fr, sizeF);
        fr.close();
        return true;
    }

    static int[] readParameters(String line) {
        int j = 0;
        String buffer =  "";
        int[] x = new int[3]; // three parameters are expected
        for(int i = 0 ; i < line.length() ; i++){
            if( line.toCharArray()[i] == ' ' || line.toCharArray()[i] == '\n' ){// Skip blank spaces and new line
                x[j++] = Integer.parseInt(buffer);
                //System.out.println("x "+(j-1)+" = "+x(j-1));
                buffer = "";
            }else{
                buffer += line.toCharArray()[i];
            }
        }
        x[j] = Integer.parseInt(buffer);
        return x;
    }

    private void readMatrix(Scanner fr, int sizeF){
        try{
            int i = 0;
            int j;
            String buffer, line;
            int fLine = 0;
            int dLine = 0;
            while (fr.hasNextLine()){// It seems that the end of line characters '\n' are removed from the line
                line = fr.nextLine();
                //System.out.println("Línea: " + line);
                i++;
                buffer = ""; j = 0;
                if (i >= 2 && i < sizeF + 2){
                    for(char c : line.toCharArray()){
                        if(c == ' '){
                            if(!buffer.equals("")){
                                if (j < sizeF){
                                    this.flow[fLine][j++] = Integer.parseInt(buffer);
                                }
                            }
                            buffer = "";
                        }else{
                            buffer += c;
                        }
                    }
                    // Get the last number before the end of line
                    if(!buffer.equals("")){
                        flow[fLine][j++] = Integer.parseInt(buffer);
                    }
                    fLine++;
                }else if (i > sizeF + 2 && i <= sizeF * 2 + 2){
                    //System.out.println("mD:"+i+" :"+line);
                    // Reading Distance Matrix
                    for(char c : line.toCharArray()){
                        if(c == ' '){
                            if(!buffer.equals("")){
                                if (j < sizeF){
                                dist[dLine][j++] = Integer.parseInt(buffer);
                                    //System.out.println("mDist "+(dLine)+","+(j-1)+" = "+(mD(dLine)(j-1)));
                                }
                            }
                            buffer = "";
                        }else{
                            buffer += c;
                        }
                    }
                    // Get the last number before the end of line
                    if(!buffer.equals("")) {
                        dist[dLine][j++] = Integer.parseInt(buffer);
                        //System.out.println("mDist "+(dLine)+","+(j-1)+" = "+(mD(dLine)(j-1)));
                    }
                    dLine++;
                }
            }
            //printMatrix(sizeF, flow);
            //printMatrix(sizeF, dist);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void clearProblemModel(){
        this.delta = new int[this.size][this.size];
    }

    public void printMatrix(int size, int[][] matrix){
        System.out.println("*******");
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println("");
        }
    }

    /**
     *  CHECK_SOLUTION
     *
     *  Checks if the solution is valid.
     */

    public boolean verify(int size, int[] match) {
        //Check Permutation
        int[] permutV = new int[size];
        int baseV = this.baseValue;
        for (int i = 0; i < match.length; i++){
            int value = match[i];
            permutV[value - baseV]++;
            if (permutV[value-baseV] > 1){
                System.out.println("Not valid permutation, value "+ value +" is repeted");
            }
        }
        int r  = 0;
        for(int i = 0; i < size; i++)
            for(int j = 0; j < size; j++)
                r += this.flow[i][j] * this.dist[match[i]][match[j]];
        return (r == 0);
    }

    private void printMatrices(){
        System.out.println("\nMatrix1");
        int i = 0;
        for (i = 0; i < size; i++ ){
            System.out.print( (i+1) + " : ");
            for(int j: this.flow[i])
                System.out.print( j + " ");
            System.out.println("");
        }
        System.out.println("Matrix2");
        for ( i = 0; i < size; i++ ){
            System.out.print((i+1) + ": ");
            for(int j: this.dist[i])
                System.out.print( j + " ");
            System.out.println("");
        }
    }
}