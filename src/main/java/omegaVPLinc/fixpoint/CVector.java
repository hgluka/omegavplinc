package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.compare.PartialComparator;
import omegaVPLinc.utility.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class CVector<T> extends FixpointVector<T> {
    protected WVector<T> wVector;

    public CVector(VPA a, VPA b, PartialComparator<T> comparator, WVector<T> wVector) {
        super(a, b, comparator);
        this.wVector = wVector;
    }

    @Override
    public Set<Pair<State, State>> frontier() {
        if (changed.isEmpty() && wVector.changed.isEmpty()) {
            return a.getAllStatePairs();
        }
        // Whatever changed in the W vector in the last iteration
        // needs to be added to this frontier as well
        Set<Pair<State, State>> frontier = new HashSet<>(wVector.changed);
        for (Pair<State, State> pq : changed) {
            State p = pq.fst();
            State q = pq.snd();

            for (State pPrime : a.getStates()) {
                frontier.add(Pair.of(p, pPrime));
                frontier.add(Pair.of(pPrime, q));
            }
        }
        return frontier;
    }
}
