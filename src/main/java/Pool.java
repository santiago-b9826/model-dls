package main.java;

public class Pool {
    private int id;
    private int size;

    public Pool(int size) {
        this.size = size;
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

    public synchronized void comm(ContextInformation message){

    }
}
