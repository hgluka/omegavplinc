package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.compare.PartialComparator;
import omegaVPLinc.utility.Pair;

import java.util.*;

public abstract class FixpointVector<T> {
    protected VPA a;
    protected VPA b;

    private Map<Pair<State, State>, Set<T>> innerVector;
    protected Map<Pair<State, State>, Set<T>> innerVectorCopy;
    protected Map<Pair<State, State>, Set<T>> innerFrontier;
    protected Map<Pair<State, State>, Set<T>> oldInnerFrontier;

    protected Set<Pair<State, State>> changed;

    protected Set<Pair<State, State>> frontier;

    protected PartialComparator<T> comparator;

    public FixpointVector(VPA a, VPA b, PartialComparator<T> comparator) {
        this.a = a;
        this.b = b;
        this.innerVector = new HashMap<>();
        this.innerVectorCopy = new HashMap<>();
        this.innerFrontier = new HashMap<>();
        this.oldInnerFrontier = new HashMap<>();
        for (State p : a.getStates()) {
            for (State q : a.getStates()) {
                this.innerVector.put(Pair.of(p, q), new HashSet<>());
                this.innerVectorCopy.put(Pair.of(p, q), new HashSet<>());
                this.innerFrontier.put(Pair.of(p, q), new HashSet<>());
                this.oldInnerFrontier.put(Pair.of(p, q), new HashSet<>());
            }
        }
        this.changed = new HashSet<>();
        this.frontier = new HashSet<>();
        this.comparator = comparator;
    }

    public abstract void initial();

    public abstract void iterateOnce();

    public abstract void frontier();

    public Set<Pair<State, State>> getFrontier() {
        return frontier;
    }

    public Set<T> getOldInnerFrontier(State p, State q) {
        return oldInnerFrontier.getOrDefault(Pair.of(p, q), new HashSet<>());
    }

    public boolean antichainInsert(Pair<State, State> statePair, Set<T> toAdd) {
        boolean removed = false;
        boolean added = false;
        for (T t : toAdd) {
            removed = innerVector.get(statePair).removeIf(e -> {
                boolean toRemove = !e.equals(t) && comparator.lesserOrEqual(t, e);
                if (oldInnerFrontier.get(statePair).contains(e) && toRemove)
                    oldInnerFrontier.get(statePair).remove(e);
                return toRemove;
            }) || removed;
            boolean existsLesser = innerVector.get(statePair).stream().anyMatch(e -> comparator.lesserOrEqual(e, t));
            if (!existsLesser) {
                boolean addedT = innerVector.get(statePair).add(t);
                if (addedT)
                    added = true;
                    innerFrontier.get(statePair).add(t);
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

    public void updateInnerFrontier() {
        this.oldInnerFrontier = new HashMap<>(innerFrontier);
        for (Pair<State, State> statePair : a.getAllStatePairs()) {
            innerFrontier.put(statePair, new HashSet<>());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FixpointVector fVector = (FixpointVector) o;
        return a.equals(fVector.a) && b.equals(fVector.b) && innerVector.equals(fVector.innerVector);
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

    public Set<Pair<State, State>> getChanged() {
        return changed;
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
