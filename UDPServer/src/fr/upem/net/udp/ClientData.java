package fr.upem.net.udp;

import java.util.BitSet;

public class ClientData {
    private long sum;
    private final BitSet opSet;

    public ClientData(long totalPosOp){
        sum = 0;
        opSet = new BitSet(Math.toIntExact(totalPosOp));
    }

    public long getSum(){
        return sum;
    }

    public void add(long ope, long idPosOp){
        if(!opSet.get(Math.toIntExact(idPosOp)) && idPosOp < opSet.length()){
            sum += ope;
            opSet.set(Math.toIntExact(idPosOp));
        }
    }

    public long size(){
        return opSet.length();
    }
}
