package main.java;

import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] argv) {
        Execution myExec = new Execution();
        try {
            myExec.testQAP("/Users/dannymunera/Documents/repos/model-dls/src/main/java/dre15.qap", 15);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
