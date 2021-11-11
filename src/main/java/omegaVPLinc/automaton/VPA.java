package omegaVPLinc.automaton;

import java.util.*;
import java.util.stream.Collectors;

public class VPA {

    private final Set<Symbol> callAlphabet;
    private final Set<Symbol> internalAlphabet;
    private final Set<Symbol> returnAlphabet;
    private final Set<String> stackAlphabet;
    private final Set<Symbol> fullAlphabet;

    private final Set<State> states;
    private final State initialState;

    private final Map<State, Set<State>> epsilonContext;

    public VPA(Set<Symbol> callAlphabet,
               Set<Symbol> internalAlphabet,
               Set<Symbol> returnAlphabet,
               Set<String> stackAlphabet,
               Set<State> states,
               State initialState) {
        this.callAlphabet = callAlphabet;
        this.internalAlphabet = internalAlphabet;
        this.returnAlphabet = returnAlphabet;
        this.fullAlphabet = new HashSet<>(this.callAlphabet);
        this.fullAlphabet.addAll(this.internalAlphabet);
        this.fullAlphabet.addAll(this.returnAlphabet);
        this.stackAlphabet = stackAlphabet;
        this.states = states;
        this.initialState = initialState;

        this.epsilonContext = new HashMap<>();
        for (State p : states) {
            this.epsilonContext.put(p, new HashSet<>(Set.of(p)));
        }
    }

    public Map<State, Set<State>> getEpsilonContext() {
        return epsilonContext;
    }

    public Map<Symbol, Map<State, Set<State>>> context() throws IllegalArgumentException {
        Map<Symbol, Map<State, Set<State>>> ctx = new HashMap<>();
        for (Symbol symbol : fullAlphabet) {
            Map<State, Set<State>> ctxOfSymbol = context(symbol);
            if (!ctxOfSymbol.isEmpty()) ctx.put(symbol, ctxOfSymbol);
        }
        return ctx;
    }

    public Map<State, Set<State>> context(Symbol symbol) throws IllegalArgumentException {
        Map<State, Set<State>> ctx = new HashMap<>();
        switch (symbol.getType()) {
            case CALL -> {
                for (State from : states) {
                    Set<State> ctxFromState = new HashSet<>();
                    for (String stackSymbol : stackAlphabet) {
                        Set<State> ctxOfStackSymbol = from.getCallSuccessors()
                                .getOrDefault(symbol, new HashMap<>())
                                .getOrDefault(stackSymbol, new HashSet<>());
                        ctxFromState.addAll(ctxOfStackSymbol);
                    }
                    if (!ctxFromState.isEmpty()) ctx.put(from, ctxFromState);
                }
            }
            case INTERNAL -> {
                for (State from : states) {
                    Set<State> ctxFromState = from.getInternalSuccessors()
                            .getOrDefault(symbol, new HashSet<>());
                    if (!ctxFromState.isEmpty()) ctx.put(from, ctxFromState);
                }
            }
            case RETURN -> {
                for (State from : states) {
                    Set<State> ctxFromState = new HashSet<>();
                    for (String stackSymbol : stackAlphabet) {
                        Set<State> ctxOfStackSymbol = from.getReturnSuccessors()
                                .getOrDefault(symbol, new HashMap<>())
                                .getOrDefault(stackSymbol, new HashSet<>());
                        ctxFromState.addAll(ctxOfStackSymbol);
                    }
                    if (!ctxFromState.isEmpty()) ctx.put(from, ctxFromState);
                }
            }
            default -> throw new IllegalArgumentException("Symbol type doesn't exist: " + symbol.getType());
        }
        return ctx;
    }

    public Map<State, Set<State>> finalContext(Symbol symbol) {
        Map<State, Set<State>> ctx = context(symbol);
        for (State p : ctx.keySet()) {
            if (!p.isFinal()) {
                ctx.get(p).removeIf(q -> !q.isFinal());
            }
        }
        return ctx;
    }

    public Map<Symbol, Map<State, Set<State>>> finalContext() throws IllegalArgumentException {
        Map<Symbol, Map<State, Set<State>>> ctx = new HashMap<>();
        for (Symbol symbol : fullAlphabet) {
            Map<State, Set<State>> ctxOfSymbol = finalContext(symbol);
            if (!ctxOfSymbol.isEmpty()) ctx.put(symbol, ctxOfSymbol);
        }
        return ctx;
    }

    public Set<Symbol> getCallAlphabet() {
        return callAlphabet;
    }

    public Set<Symbol> getInternalAlphabet() {
        return internalAlphabet;
    }

    public Set<Symbol> getReturnAlphabet() {
        return returnAlphabet;
    }

    public Set<String> getStackAlphabet() {
        return stackAlphabet;
    }

    public Set<State> getStates() {
        return states;
    }

    public Optional<State> getState(String name) {
        for (State q : states) {
            if (q.getName().equals(name))
                return Optional.of(q);
        }
        return Optional.empty();
    }

    public State getInitialState() {
        return initialState;
    }
}
