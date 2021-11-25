package omegaVPLinc.automaton;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VPABuilder {
    private Set<Symbol> callAlphabet;
    private final Map<String, Symbol> callAlphabetMap;
    private Set<Symbol> internalAlphabet;
    private final Map<String, Symbol> internalAlphabetMap;
    private Set<Symbol> returnAlphabet;
    private final Map<String, Symbol> returnAlphabetMap;
    private Set<String> stackAlphabet;

    private Set<State> states;
    private final Map<String, State> stateMap;
    private State initialState;

    public VPABuilder() {
        this.callAlphabetMap = new HashMap<>();
        this.internalAlphabetMap = new HashMap<>();
        this.returnAlphabetMap = new HashMap<>();
        this.stateMap = new HashMap<>();
    }

    public VPABuilder callAlphabet(Set<Symbol> callAlphabet) {
        this.callAlphabet = callAlphabet;
        for (Symbol c : callAlphabet) {
            callAlphabetMap.put(c.getSymbol(), c);
        }
        return this;
    }
    public VPABuilder internalAlphabet(Set<Symbol> internalAlphabet) {
        this.internalAlphabet = internalAlphabet;
        for (Symbol a : internalAlphabet) {
            internalAlphabetMap.put(a.getSymbol(), a);
        }
        return this;
    }

    public VPABuilder returnAlphabet(Set<Symbol> returnAlphabet) {
        this.returnAlphabet = returnAlphabet;
        for (Symbol r : returnAlphabet) {
            returnAlphabetMap.put(r.getSymbol(), r);
        }
        return this;
    }

    public VPABuilder stackAlphabet(Set<String> stackAlphabet) {
        this.stackAlphabet = stackAlphabet;
        return this;
    }

    public VPABuilder states(Set<State> states) {
        this.states = states;
        for (State state : states) {
            this.stateMap.put(state.getName(), state);
        }
        return this;
    }

    public VPABuilder initialState(State initialState) {
        this.initialState = initialState;
        return this;
    }

    public Set<State> getStates() {
        return states;
    }

    public State getState(String name) {
        return stateMap.get(name);
    }

    public Symbol getCallSymbol(String name) {
        return callAlphabetMap.get(name);
    }

    public Symbol getInternalSymbol(String name) {
        return internalAlphabetMap.get(name);
    }

    public Symbol getReturnSymbol(String name) {
        return returnAlphabetMap.get(name);
    }

    public Map<String, State> getStateMap() {
        return stateMap;
    }

    public VPA build() throws IllegalArgumentException {
        if (Collections.disjoint(this.callAlphabet, this.internalAlphabet)
                && Collections.disjoint(this.internalAlphabet, this.returnAlphabet)
                && Collections.disjoint(this.callAlphabet, this.returnAlphabet)) {
            if (this.callAlphabet.stream().noneMatch(s -> s.getType() != Symbol.SymbolType.CALL)
            && this.internalAlphabet.stream().noneMatch(s -> s.getType() != Symbol.SymbolType.INTERNAL)
            && this.returnAlphabet.stream().noneMatch(s -> s.getType() != Symbol.SymbolType.RETURN)) {
                if (this.states.contains(initialState)) {
                    return new VPA(this.callAlphabet,
                            this.internalAlphabet,
                            this.returnAlphabet,
                            this.stackAlphabet,
                            this.states,
                            this.initialState);
                } else {
                    throw new IllegalArgumentException("Initial state must be part of the state set.");
                }
            } else {
                throw new IllegalArgumentException("Wrong symbol type in alphabet partition.");
            }
        } else {
            throw new IllegalArgumentException("VPA must have disjoint call, internal and return alphabets.");
        }
    }
}
