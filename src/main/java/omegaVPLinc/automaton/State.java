package omegaVPLinc.automaton;

import omegaVPLinc.utility.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class State {
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

    public static Set<Map<State, Set<State>>> composeS(
            Set<Map<State, Set<State>>> E,
            Set<Map<State, Set<State>>> D
    ) {
        Set<Map<State, Set<State>>> ED = new HashSet<>();
        for (Map<State, Set<State>> e : E) {
            for (Map<State, Set<State>> d : D) {
                ED.add(composeM(e, d));
            }
        }
        return ED;
    }

    public static Map<State, Set<State>> composeM(
            Map<State, Set<State>> e,
            Map<State, Set<State>> d
    ) {
        Map<State, Set<State>> ed = new HashMap<>();
        for (State p : e.keySet()) {
            for (State q : e.get(p)) {
                if (d.containsKey(q)) {
                    Set<State> existingOrNull = ed.putIfAbsent(p, d.get(q));
                    if (existingOrNull != null) existingOrNull.addAll(d.get(q));
                }
            }
        }
        return ed;
    }

    public static Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> composeP(
            Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> E,
            Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> D
    ) {
        Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> ED = new HashSet<>();
        for (Pair<Map<State, Set<State>>, Map<State, Set<State>>> e : E) {
            for (Pair<Map<State, Set<State>>, Map<State, Set<State>>> d : D) {
                ED.add(Pair.of(
                        composeM(e.fst(), d.fst()),
                        Stream.concat(
                                composeM(e.fst(), d.snd()).entrySet().stream(),
                                composeM(e.snd(), d.fst()).entrySet().stream()
                        ).collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (v1, v2) -> Stream.concat(v1.stream(), v2.stream()).collect(Collectors.toSet())))
                ));
            }
        }
        return ED;
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
                transitionMap.get(symbol).put(stackSymbol, Set.of(state));
            }
        } else {
            transitionMap.put(symbol,
                    new HashMap<String, Set<State>>(Map.of(stackSymbol, Set.of(state))));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return isFinal == state.isFinal && name.equals(state.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isFinal);
    }

    @Override
    public String toString() {
        return name;
    }
}
