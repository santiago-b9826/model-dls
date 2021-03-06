package main.java;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BinaryOperator;

public class Pool {
    private int id;
    private int size;
    private List<ContextInformation> pool;

    public Pool(int size) {
        this.size = size;
        pool = new ArrayList<>(size);
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public synchronized void sendInfo(ContextInformation message){
        if (pool.size() < size){
            pool.add(message);
        }else{
            //pool is full
            //int index = pool.stream().min(Comparator.comparing(ContextInformation::getCost)).hashCode();
            int maxCost = -1;
            int maxIndex = -1;
            for(int i = 0; i < pool.size(); i++){
                if(pool.get(i).getCost() > maxCost){
                    maxCost = pool.get(i).getCost();
                    maxIndex = i;
                }
            }
            pool.set(maxIndex, message);
        }
    }

    public synchronized ContextInformation getInfo(){
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }
}
