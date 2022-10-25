package tech.yakov.AtemProxy.models.atem;

import java.util.Collection;
import java.util.HashMap;

public class Atem {
    private String atemName;
    private HashMap<Integer, Signal> inputSignals;

    public Atem(String atemName){
        this.atemName = atemName;
        this.inputSignals = new HashMap<>();
    }

    public boolean findSignal(int signalId){
        return inputSignals.containsKey(signalId);
    }

    public void putNewSignal(int signalId, Signal signal){
        inputSignals.put(signalId, signal);
    }

    public int getNumOfInputs(){
        return inputSignals.size();
    }

    public Signal getSignalById(int id){
        return inputSignals.get(id);
    }

    public String getAtemName() {
        return atemName;
    }

    public Collection<Signal> getSignals(){
        return inputSignals.values();
    }
}
