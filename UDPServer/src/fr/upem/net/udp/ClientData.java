package fr.upem.net.udp;

import java.util.BitSet;

public class ClientData {
    private long sum;
    private final BitSet checkOperators;

    ClientData(long totalOp){
        checkOperators = new BitSet((int) totalOp);
    }

    public void add(long op, long posOp){
        if(checkOperators.get((int) posOp) || posOp < 0 || posOp > checkOperators.length()){
            return;
        };
        sum += op;
        checkOperators.set((int) posOp);
    }

    public boolean checkAllOp(){
        return checkOperators.cardinality() == checkOperators.length();
    };

    public long getSum(){
        return sum;
    };
}