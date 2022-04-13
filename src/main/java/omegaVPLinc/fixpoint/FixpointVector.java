package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.compare.PartialComparator;
import omegaVPLinc.utility.Pair;

import java.util.*;

public abstract class FixpointVector {
    protected VPA a;
    protected VPA b;

    protected boolean withFinal;

    protected boolean withWords;

    private final Map<Pair<State, State>, Set<Context>> innerVector;
    protected Map<Pair<State, State>, Set<Context>> innerVectorCopy;
    protected Map<Pair<State, State>, Set<Context>> innerFrontier;
    protected Map<Pair<State, State>, Set<Context>> oldInnerFrontier;

    protected Set<Pair<State, State>> changed;

    protected Set<Pair<State, State>> frontier;

    protected PartialComparator comparator;

    public FixpointVector(VPA a, VPA b, boolean withFinal, boolean withWords) {
        this.a = a;
        this.b = b;
        this.withFinal = withFinal;
        this.withWords = withWords;
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
        this.comparator = new PartialComparator();
    }

    public abstract void initial();

    public abstract void iterateOnce();

    public void allChanged() {
        changed = new HashSet<>();
        for (Pair<State, State> pq : a.getAllStatePairs()) {
            innerFrontier.put(pq, new HashSet<>());
            oldInnerFrontier.put(pq, new HashSet<>());
        }
        for (Pair<State, State> pq : getInnerVectorCopy().keySet()) {
            if (!getInnerVectorCopy().get(pq).isEmpty()) {
                oldInnerFrontier.put(pq, getInnerVectorCopy().get(pq));
                changed.add(pq);
            }
        }
    }
    
    public void noChanged() {
        changed = new HashSet<>();
        for (Pair<State, State> pq : a.getAllStatePairs()) {
            innerFrontier.put(pq, new HashSet<>());
            oldInnerFrontier.put(pq, new HashSet<>());
        }
    }

    public abstract void frontier();

    public Set<Pair<State, State>> getFrontier() {
        return frontier;
    }

    public Set<Context> getOldInnerFrontier(State p, State q) {
        return oldInnerFrontier.getOrDefault(Pair.of(p, q), new HashSet<>());
    }

    public boolean antichainInsert(Pair<State, State> statePair, Set<Context> toAdd) {
        boolean removed = false;
        boolean added = false;
        for (Context t : toAdd) {
            removed = innerVector.get(statePair).removeIf(e -> {
                boolean toRemove = !e.equals(t) && comparator.lesserOrEqual(t, e);
                if (oldInnerFrontier.get(statePair).contains(e) && toRemove)
                    oldInnerFrontier.get(statePair).remove(e);
                if (innerFrontier.get(statePair).contains(e) && toRemove)
                    innerFrontier.get(statePair).remove(e);
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

    public int computeFixpoint() {
        int i = 0;
        iterateOnce();
        updateCopy();
        updateInnerFrontier();
        frontier();
        i++;
        while (!changed.isEmpty()) {
            iterateOnce();
            updateCopy();
            updateInnerFrontier();
            frontier();
            i++;
        }
        allChanged();
        return i;
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

    public Map<Pair<State, State>, Set<Context>> getInnerVector() {
        return innerVector;
    }

    public Map<Pair<State, State>, Set<Context>> getInnerVectorCopy() {
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
                for (Context t : innerVector.get(p_q)) {
                    sb.append("\t").append(t.getCtx()).append("\n");
                }
                sb.append("}\n");
            }
        }
        return sb.toString();
    }
}
