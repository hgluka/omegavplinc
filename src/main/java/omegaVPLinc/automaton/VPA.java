package omegaVPLinc.automaton;

import java.util.*;

public class VPA {

    private Set<String> callAlphabet;
    private Set<String> internalAlphabet;
    private Set<String> returnAlphabet;
    private Set<String> stackAlphabet;

    private Set<State> states;
    private State initialState;

    public VPA(Set<String> callAlphabet,
               Set<String> internalAlphabet,
               Set<String> returnAlphabet,
               Set<String> stackAlphabet,
               Set<State> states,
               State initialState) {
        this.callAlphabet = callAlphabet;
        this.internalAlphabet = internalAlphabet;
        this.returnAlphabet = returnAlphabet;
        this.stackAlphabet = stackAlphabet;
        this.states = states;
        this.initialState = initialState;
    }

    public Map<State, Set<State>> context(String symbol) throws IllegalArgumentException {
        Map<State, Set<State>> ctx = new HashMap<>();
        for (State from : states) {
            Set<State> ctxOfSymbol = new HashSet<>();
            if (callAlphabet.contains(symbol)) {
                for (String stackSymbol : stackAlphabet) {
                    Set<State> ctxOfStackSymbol = from.getCallSuccessors()
                            .getOrDefault(symbol, new HashMap<>())
                            .getOrDefault(stackSymbol, new HashSet<>());
                    ctxOfSymbol.addAll(ctxOfStackSymbol);
                }
            } else if (internalAlphabet.contains(symbol)) {
                ctxOfSymbol.addAll(from.getInternalSuccessors().getOrDefault(symbol, new HashSet<>()));
            } else if (returnAlphabet.contains(symbol)) {
                for (String stackSymbol : stackAlphabet) {
                    Set<State> ctxOfStackSymbol = from.getReturnSuccessors()
                            .getOrDefault(symbol, new HashMap<>())
                            .getOrDefault(stackSymbol, new HashSet<>());
                    ctxOfSymbol.addAll(ctxOfStackSymbol);
                }
            } else {
                throw new IllegalArgumentException("Symbol not in alphabet.");
            }
            if (!ctxOfSymbol.isEmpty()) ctx.put(from, ctxOfSymbol);
        }
        return ctx;
    }

    public Set<String> getCallAlphabet() {
        return callAlphabet;
    }

    public Set<String> getInternalAlphabet() {
        return internalAlphabet;
    }

    public Set<String> getReturnAlphabet() {
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
