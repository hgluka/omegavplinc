package omegaVPLinc.automaton;

import java.util.Optional;
import java.util.Set;

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
