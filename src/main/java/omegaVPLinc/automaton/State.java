package omegaVPLinc.automaton;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class State {
    private String name;
    private boolean isFinal;
    private Map<String, Set<State>> successors;
    private Map<String, Set<State>> predecessors;

    public State(String name, boolean isFinal) {
        this.name = name;
        this.isFinal = isFinal;
        this.successors = new HashMap<>();
        this.predecessors = new HashMap<>();

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

    public Map<String, Set<State>> getSuccessors() {
        return successors;
    }

    public void setSuccessors(Map<String, Set<State>> successors) {
        this.successors = successors;
    }

    public Map<String, Set<State>> getPredecessors() {
        return predecessors;
    }

    public void setPredecessors(Map<String, Set<State>> predecessors) {
        this.predecessors = predecessors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return isFinal == state.isFinal && name.equals(state.name) && Objects.equals(successors, state.successors) && Objects.equals(predecessors, state.predecessors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isFinal, successors, predecessors);
    }
}
