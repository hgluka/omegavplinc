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
        if (e.isEmpty() || d.isEmpty())
            return ed;
        for (State p : e.keySet()) {
            for (State q : new HashSet<>(e.get(p))) {
                if (d.containsKey(q)) {
                    ed.computeIfAbsent(p, k -> new HashSet<>()).addAll(d.get(q));
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

    private static boolean addTransitive(Map<State, Set<State>> m) {
        boolean added = false;
        for (State p1 : m.keySet()) {
            for (State q1 : new HashSet<>(m.get(p1))) {
                for (State q2 : new HashSet<>(m.getOrDefault(q1, new HashSet<>()))) {
                    if (!m.get(p1).contains(q2)) {
                        m.get(p1).add(q2);
                        added = true;
                    }
                }
            }
        }
        return added;
    }

    public static Map<State, Set<State>> transitiveClosure(Map<State, Set<State>> m) {
        Map<State, Set<State>> transitiveClosure = new HashMap<>(m);
        boolean added;
        do {
            added = addTransitive(transitiveClosure);
        } while (added);
        return transitiveClosure;
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
        return callSuccessors
                .getOrDefault(c, new HashMap<>())
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public Set<State> getCallSuccessors(Symbol c, String s) {
        return callSuccessors.getOrDefault(c, new HashMap<>()).getOrDefault(s, new HashSet<>());
    }

    public void addCallSuccessor(Symbol symbol, String stackSymbol, State succ) {
        addTransition(symbol, stackSymbol, succ, callSuccessors);
    }

    public HashMap<Symbol, HashMap<String, Set<State>>> getCallPredecessors() {
        return callPredecessors;
    }

    public Set<State> getCallPredecessors(Symbol c) {
        return callPredecessors
                .getOrDefault(c, new HashMap<>())
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
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
        return returnSuccessors
                .getOrDefault(c, new HashMap<>())
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
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
        return returnPredecessors
                .getOrDefault(c, new HashMap<>())
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
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
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
