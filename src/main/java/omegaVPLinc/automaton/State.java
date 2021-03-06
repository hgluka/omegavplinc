package omegaVPLinc.automaton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class State {
    private static final Logger logger = LoggerFactory.getLogger(State.class);
    private String name;
    private boolean isFinal;

    private final Map<Symbol, Set<State>> internalSuccessors;
    private final Map<Symbol, Set<State>> internalPredecessors;

    private final HashMap<Symbol, HashMap<String, Set<State>>> callSuccessors;
    private final HashMap<Symbol, HashMap<String, Set<State>>> callPredecessors;

    private final HashMap<Symbol, HashMap<String, Set<State>>> returnSuccessors;
    private final HashMap<Symbol, HashMap<String, Set<State>>> returnPredecessors;

    public State(String name) {
        this.name = name;
        this.isFinal = false;
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

    public Set<State> getInternalSuccessors(Symbol c) {
        return internalSuccessors.getOrDefault(c, new HashSet<>());
    }

    public void addInternalSuccessor(Symbol symbol, State succ) {
        if (symbol == null || succ == null) {
            throw new IllegalArgumentException("Cannot add transition with null values.");
        }
        Set<State> intSucc = internalSuccessors.putIfAbsent(symbol, new HashSet<>(Set.of(succ)));
        if (intSucc != null) {
            intSucc.add(succ);
        }
    }

    public Map<Symbol, Set<State>> getInternalPredecessors() {
        return internalPredecessors;
    }

    public Set<State> getInternalPredecessors(Symbol c) {
        return internalPredecessors.getOrDefault(c, new HashSet<>());
    }

    public void addInternalPredecessor(Symbol symbol, State pred) {
        if (symbol == null || pred == null) {
            throw new IllegalArgumentException("Cannot add transition with null values.");
        }
        Set<State> intPred = internalPredecessors.putIfAbsent(symbol, new HashSet<>(Set.of(pred)));
        if (intPred != null) intPred.add(pred);
    }

    public HashMap<Symbol, HashMap<String, Set<State>>> getCallSuccessors() {
        return callSuccessors;
    }

    public Set<State> getCallSuccessors(Symbol c) {
        Set<State> callSuccessorsOfC = new HashSet<>();
        for (String s : callSuccessors.getOrDefault(c, new HashMap<>()).keySet()) {
            callSuccessorsOfC.addAll(callSuccessors.get(c).get(s));
        }
        return callSuccessorsOfC;
    }

    public Set<State> getCallSuccessors(Symbol c, String s) {
        return callPredecessors.getOrDefault(c, new HashMap<>()).getOrDefault(s, new HashSet<>());
    }

    public void addCallSuccessor(Symbol symbol, String stackSymbol, State succ) {
        addTransition(symbol, stackSymbol, succ, callSuccessors);
    }

    public HashMap<Symbol, HashMap<String, Set<State>>> getCallPredecessors() {
        return callPredecessors;
    }

    public Set<State> getCallPredecessors(Symbol c) {
        Set<State> callPredecessorsOfC = new HashSet<>();
        for (String s : callPredecessors.getOrDefault(c, new HashMap<>()).keySet()) {
            callPredecessorsOfC.addAll(callPredecessors.get(c).get(s));
        }
        return callPredecessorsOfC;
    }

    public Set<State> getCallPredecessors(Symbol c, String s) {
        return callPredecessors.getOrDefault(c, new HashMap<>()).getOrDefault(s, new HashSet<>());
    }

    public void addCallPredecessor(Symbol symbol, String stackSymbol, State pred) {
        addTransition(symbol, stackSymbol, pred, callPredecessors);
    }

    public HashMap<Symbol, HashMap<String, Set<State>>> getReturnSuccessors() {
        return returnSuccessors;
    }

    public Set<State> getReturnSuccessors(Symbol c) {
        Set<State> returnSuccessorsOfC = new HashSet<>();
        for (String s : returnSuccessors.getOrDefault(c, new HashMap<>()).keySet()) {
            returnSuccessorsOfC.addAll(returnSuccessors.get(c).get(s));
        }
        return returnSuccessorsOfC;
    }

    public Set<State> getReturnSuccessors(Symbol c, String s) {
        return returnSuccessors.getOrDefault(c, new HashMap<>()).getOrDefault(s, new HashSet<>());
    }

    public void addReturnSuccessor(Symbol symbol, String stackSymbol, State succ) {
        addTransition(symbol, stackSymbol, succ, returnSuccessors);
    }

    public HashMap<Symbol, HashMap<String, Set<State>>> getReturnPredecessors() {
        return returnPredecessors;
    }

    public Set<State> getReturnPredecessors(Symbol c) {
        Set<State> returnPredecessorsOfC = new HashSet<>();
        for (String s : returnPredecessors.getOrDefault(c, new HashMap<>()).keySet()) {
            returnPredecessorsOfC.addAll(returnPredecessors.get(c).get(s));
        }
        return returnPredecessorsOfC;
    }

    public Set<State> getReturnPredecessors(Symbol c, String s) {
        return returnPredecessors.getOrDefault(c, new HashMap<>()).getOrDefault(s, new HashSet<>());
    }

    public void addReturnPredecessor(Symbol symbol, String stackSymbol, State pred) {
        addTransition(symbol, stackSymbol, pred, returnPredecessors);
    }

    private void addTransition(
            Symbol symbol,
            String stackSymbol,
            State state,
            HashMap<Symbol, HashMap<String, Set<State>>> transitionMap) throws IllegalArgumentException {
        if (symbol == null || stackSymbol == null || state == null) {
            throw new IllegalArgumentException("Cannot add transition with null values.");
        }
        if (transitionMap.containsKey(symbol)) {
            if (transitionMap.get(symbol).containsKey(stackSymbol)) {
                transitionMap.get(symbol).get(stackSymbol).add(state);
            } else {
                transitionMap.get(symbol).put(stackSymbol, new HashSet<>(Set.of(state)));
            }
        } else {
            transitionMap.put(symbol,
                    new HashMap<String, Set<State>>(Map.of(stackSymbol, new HashSet<>(Set.of(state)))));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return name.equals(state.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
