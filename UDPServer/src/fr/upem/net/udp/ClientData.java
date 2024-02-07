package fr.upem.net.udp;

import java.util.BitSet;

public class ClientData {
    private long sum;
    private final BitSet checkOperators;

    private final long totalOp;

    ClientData(long totalOp){
        checkOperators = new BitSet((int) totalOp);
        this.totalOp = totalOp;
    }

    public void add(long op, long posOp){
        if(checkOperators.get((int) posOp) || posOp < 0 || posOp > totalOp){
            return;
        };
        sum += op;
        checkOperators.set((int) posOp);
    }

    public boolean checkAllOp(){
        return checkOperators.cardinality() == totalOp;
    };

    public long getSum(){
        return sum;
    };
}