package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.compare.PartialComparator;
import omegaVPLinc.utility.Pair;

import java.util.*;

public abstract class FixpointVector<T> {
    protected VPA a;
    protected VPA b;

    protected Map<Pair<State, State>, Set<T>> innerVector;
    protected Map<Pair<State, State>, Set<T>> innerVectorCopy;

    protected Set<Pair<State, State>> changed;

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
        this.changed = new HashSet<>();
        this.comparator = comparator;
    }

    public abstract Set<Pair<State, State>> iterateOnce(
            Set<Pair<State, State>> frontier
    );

    public abstract Set<Pair<State, State>> frontier();

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

    public Map<Pair<State, State>, Set<T>> deepCopy() {
        Map<Pair<State, State>, Set<T>> innerWcopy = new HashMap<>();
        for (Pair<State, State> pq : innerVector.keySet()) {
            innerWcopy.put(pq, new HashSet<>(innerVector.get(pq)));
        }
        return innerWcopy;
    }

    public void updateCopy() {
        for (Pair<State, State> pq : changed) {
            innerVectorCopy.put(pq, new HashSet<>(innerVector.get(pq)));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WVector wVector = (WVector) o;
        return a.equals(wVector.a) && b.equals(wVector.b) && innerVector.equals(wVector.innerVector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, innerVector);
    }

    public Map<Pair<State, State>, Set<T>> getInnerVector() {
        return innerVector;
    }

    public Map<Pair<State, State>, Set<T>> getInnerVectorCopy() {
        return innerVectorCopy;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Pair<State, State> p_q : innerVector.keySet()) {
            if (!innerVector.get(p_q).isEmpty()) {
                sb.append(p_q).append(" {\n");
                for (T t : innerVector.get(p_q)) {
                    sb.append("\t").append(t).append("\n");
                }
                sb.append("}\n");
            }
        }
        return sb.toString();
    }
}
