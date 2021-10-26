package omegaVPLinc.automaton;

import java.util.Collections;
import java.util.Set;

public class VPABuilder {
    private Set<Symbol> callAlphabet;
    private Set<Symbol> internalAlphabet;
    private Set<Symbol> returnAlphabet;
    private Set<String> stackAlphabet;

    private Set<State> states;
    private State initialState;

    public VPABuilder callAlphabet(Set<Symbol> callAlphabet) {
        this.callAlphabet = callAlphabet;
        return this;
    }
    public VPABuilder internalAlphabet(Set<Symbol> internalAlphabet) {
        this.internalAlphabet = internalAlphabet;
        return this;
    }

    public VPABuilder returnAlphabet(Set<Symbol> returnAlphabet) {
        this.returnAlphabet = returnAlphabet;
        return this;
    }

    public VPABuilder stackAlphabet(Set<String> stackAlphabet) {
        this.stackAlphabet = stackAlphabet;
        return this;
    }

    public VPABuilder states(Set<State> states) {
        this.states = states;
        return this;
    }

    public VPABuilder initialState(State initialState) {
        this.initialState = initialState;
        return this;
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
