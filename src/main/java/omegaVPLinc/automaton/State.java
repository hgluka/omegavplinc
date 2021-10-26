package omegaVPLinc.automaton;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class State {
    private String name;
    private boolean isFinal;

    private Map<Symbol, Set<State>> internalSuccessors;
    private Map<Symbol, Set<State>> internalPredecessors;

    private HashMap<Symbol, HashMap<String, Set<State>>> callSuccessors;
    private HashMap<Symbol, HashMap<String, Set<State>>> callPredecessors;

    private HashMap<Symbol, HashMap<String, Set<State>>> returnSuccessors;
    private HashMap<Symbol, HashMap<String, Set<State>>> returnPredecessors;

    public State(String name) {
        this.name = name;
        this.internalSuccessors = new HashMap<>();
        this.internalPredecessors = new HashMap<>();

        this.callSuccessors = new HashMap<Symbol, HashMap<String, Set<State>>>();
        this.callPredecessors = new HashMap<Symbol, HashMap<String, Set<State>>>();

        this.returnSuccessors = new HashMap<Symbol, HashMap<String, Set<State>>>();
        this.returnPredecessors = new HashMap<Symbol, HashMap<String, Set<State>>>();
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

    public Map<Symbol, Set<State>> getInternalSuccessors() {
        return internalSuccessors;
    }

    public void addInternalSuccessor(Symbol symbol, State succ) {
        Set<State> intSucc = internalSuccessors.putIfAbsent(symbol, Set.of(succ));
        if (intSucc != null) intSucc.add(succ);
    }

    public Map<Symbol, Set<State>> getInternalPredecessors() {
        return internalPredecessors;
    }

    public void addInternalPredecessor(Symbol symbol, State pred) {
        Set<State> intPred = internalPredecessors.putIfAbsent(symbol, Set.of(pred));
        if (intPred != null) intPred.add(pred);
    }

    public HashMap<Symbol, HashMap<String, Set<State>>> getCallSuccessors() {
        return callSuccessors;
    }

    public void addCallSuccessor(Symbol symbol, String stackSymbol, State succ) {
        addTransition(symbol, stackSymbol, succ, callSuccessors);
    }

    public HashMap<Symbol, HashMap<String, Set<State>>> getCallPredecessors() {
        return callPredecessors;
    }

    public void addCallPredecessor(Symbol symbol, String stackSymbol, State pred) {
        addTransition(symbol, stackSymbol, pred, callPredecessors);
    }

    public HashMap<Symbol, HashMap<String, Set<State>>> getReturnSuccessors() {
        return returnSuccessors;
    }

    public void addReturnSuccessor(Symbol symbol, String stackSymbol, State succ) {
        addTransition(symbol, stackSymbol, succ, returnSuccessors);
    }

    public HashMap<Symbol, HashMap<String, Set<State>>> getReturnPredecessors() {
        return returnPredecessors;
    }

    public void addReturnPredecessor(Symbol symbol, String stackSymbol, State pred) {
        addTransition(symbol, stackSymbol, pred, returnPredecessors);
    }

    private void addTransition(Symbol symbol, String stackSymbol, State state, HashMap<Symbol, HashMap<String, Set<State>>> transitionMap) {
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
