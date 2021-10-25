package omegaVPLinc.automaton;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class State {
    private String name;
    private boolean isFinal;

    private Map<String, Set<State>> internalSuccessors;
    private Map<String, Set<State>> internalPredecessors;

    private HashMap<String, HashMap<String, Set<State>>> callSuccessors;
    private HashMap<String, HashMap<String, Set<State>>> callPredecessors;

    private HashMap<String, HashMap<String, Set<State>>> returnSuccessors;
    private HashMap<String, HashMap<String, Set<State>>> returnPredecessors;

    public State(String name) {
        this.name = name;
        this.internalSuccessors = new HashMap<>();
        this.internalPredecessors = new HashMap<>();

        this.callSuccessors = new HashMap<String, HashMap<String, Set<State>>>();
        this.callPredecessors = new HashMap<String, HashMap<String, Set<State>>>();

        this.returnSuccessors = new HashMap<String, HashMap<String, Set<State>>>();
        this.returnPredecessors = new HashMap<String, HashMap<String, Set<State>>>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public Map<String, Set<State>> getInternalSuccessors() {
        return internalSuccessors;
    }

    public void addInternalSuccessor(String symbol, State succ) {
        Set<State> intSucc = internalSuccessors.putIfAbsent(symbol, Set.of(succ));
        if (intSucc != null) intSucc.add(succ);
    }

    public Map<String, Set<State>> getInternalPredecessors() {
        return internalPredecessors;
    }

    public void addInternalPredecessor(String symbol, State pred) {
        Set<State> intPred = internalPredecessors.putIfAbsent(symbol, Set.of(pred));
        if (intPred != null) intPred.add(pred);
    }

    public HashMap<String, HashMap<String, Set<State>>> getCallSuccessors() {
        return callSuccessors;
    }

    public void addCallSuccessor(String symbol, String stackSymbol, State succ) {
        addTransition(symbol, stackSymbol, succ, callSuccessors);
    }

    public HashMap<String, HashMap<String, Set<State>>> getCallPredecessors() {
        return callPredecessors;
    }

    public void addCallPredecessor(String symbol, String stackSymbol, State pred) {
        addTransition(symbol, stackSymbol, pred, callPredecessors);
    }

    public HashMap<String, HashMap<String, Set<State>>> getReturnSuccessors() {
        return returnSuccessors;
    }

    public void addReturnSuccessor(String symbol, String stackSymbol, State succ) {
        addTransition(symbol, stackSymbol, succ, returnSuccessors);
    }

    public HashMap<String, HashMap<String, Set<State>>> getReturnPredecessors() {
        return returnPredecessors;
    }

    public void addReturnPredecessor(String symbol, String stackSymbol, State pred) {
        addTransition(symbol, stackSymbol, pred, returnPredecessors);
    }

    private void addTransition(String symbol, String stackSymbol, State state, HashMap<String, HashMap<String, Set<State>>> transitionMap) {
        if (transitionMap.containsKey(symbol)) {
            if (transitionMap.get(symbol).containsKey(stackSymbol)) {
                transitionMap.get(symbol).get(stackSymbol).add(state);
            } else {
                transitionMap.get(symbol).put(stackSymbol, Set.of(state));
            }
        } else {
            transitionMap.put(symbol,
                    new HashMap<String, Set<State>>(Map.of(stackSymbol, Set.of(state))));
        }
    }
}
