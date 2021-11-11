package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class FixpointVector<T> {
    protected VPA a;
    protected VPA b;

    protected Map<Pair<State, State>, Set<T>> innerVector;

    public FixpointVector(VPA a, VPA b) {
        this.a = a;
        this.b = b;
        this.innerVector = new HashMap<>();
        for (State p : a.getStates()) {
            for (State q : a.getStates()) {
                innerVector.put(Pair.of(p, q), new HashSet<>());
            }
        }
    }

    public abstract Set<Pair<State, State>> iterateOnce(
            Map<Pair<State, State>, Set<T>> copy,
            Set<Pair<State, State>> frontier
    );

    public abstract Set<Pair<State, State>> frontier(Set<Pair<State, State>> changed);

    public abstract boolean antichainInsert(Pair<State, State> statePair, Set<Map<State, Set<State>>> toAdd);

    public abstract Map<Pair<State, State>, Set<T>> deepCopy();

    public Map<Pair<State, State>, Set<T>> getInnerVector() {
        return innerVector;
    }
}
