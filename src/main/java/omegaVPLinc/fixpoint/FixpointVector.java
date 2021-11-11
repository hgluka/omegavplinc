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
    protected Map<Pair<State, State>, Set<T>> innerVectorCopy;

    protected PartialComparator<T> comparator;

    public FixpointVector(VPA a, VPA b, PartialComparator<T> comparator) {
        this.a = a;
        this.b = b;
        this.innerVector = new HashMap<>();
        this.innerVectorCopy = new HashMap<>();
        for (State p : a.getStates()) {
            for (State q : a.getStates()) {
                this.innerVector.put(Pair.of(p, q), new HashSet<>());
                this.innerVectorCopy.put(Pair.of(p, q), new HashSet<>());
            }
        }
        this.comparator = comparator;
    }

    public abstract Set<Pair<State, State>> iterateOnce(
            Set<Pair<State, State>> frontier
    );

    public abstract Set<Pair<State, State>> frontier(Set<Pair<State, State>> changed);

    public boolean antichainInsert(Pair<State, State> statePair, Set<T> toAdd) {
        boolean removed = false;
        boolean added = false;
        for (T mapToAdd : toAdd) {
            removed = removed || innerVector.get(statePair).removeIf(
                    existingMap ->
                            !existingMap.equals(mapToAdd)
                                    && comparator.lesserOrEqual(mapToAdd, existingMap));
            boolean existsLesser = innerVector.get(statePair).stream()
                    .anyMatch(existingMap -> comparator.lesserOrEqual(existingMap, mapToAdd));
            if (!existsLesser) {
                innerVector.get(statePair).add(mapToAdd);
                added = true;
            }
        }
        return removed || added;
    }

    public abstract Map<Pair<State, State>, Set<T>> deepCopy();

    public void updateCopy(Set<Pair<State, State>> changed) {
        for (Pair<State, State> pq : changed) {
            innerVectorCopy.put(pq, new HashSet<>(innerVector.get(pq)));
        }
    }



    public Map<Pair<State, State>, Set<T>> getInnerVector() {
        return innerVector;
    }
}
