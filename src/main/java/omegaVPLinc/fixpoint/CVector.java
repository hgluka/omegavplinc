package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
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
        for (State p : a.getStates()) {
            for (Symbol r : p.getReturnSuccessors().keySet()) {
                for (State q : p.getReturnSuccessors(r, a.getEmptyStackSymbol())) {
                    frontier.add(Pair.of(p, q));
                }
            }
        }
    }

    @Override
    public int computeFixpoint() {
        int i = 0;
        iterateOnce();
        updateCopy();
        updateInnerFrontier();
        wVector.noChanged();
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
        wVector.allChanged();
        return i;
    }

    @Override
    public void frontier() {
        frontier = new HashSet<>(wVector.changed);
        for (Pair<State, State> pq : changed) {
            State p = pq.fst();
            State q = pq.snd();

            for (State pPrime : a.getStates()) {
                if (!getInnerVector().get(Pair.of(q, pPrime)).isEmpty())
                    frontier.add(Pair.of(p, pPrime));
                if (!getInnerVector().get(Pair.of(pPrime, p)).isEmpty())
                    frontier.add(Pair.of(pPrime, q));
            }
        }
    }
}
