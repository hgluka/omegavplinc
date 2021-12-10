package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.compare.PartialComparator;
import omegaVPLinc.utility.Pair;

import java.util.*;

public abstract class WVector<T> extends FixpointVector<T> {
    public WVector(VPA a, VPA b, PartialComparator<T> comparator) {
        super(a, b, comparator);
        for (State p : a.getStates()) {
            frontier.add(Pair.of(p, p));
            for (Symbol s : p.getInternalSuccessors().keySet()) {
                for (State q : p.getInternalSuccessors(s)) {
                    frontier.add(Pair.of(p, q));
                }
            }
        }
    }

    @Override
    public void frontier() {
        frontier = new HashSet<>();
        for (Pair<State, State> pq : changed) {
            State p = pq.fst();
            State q = pq.snd();

            for (Symbol c : p.getCallPredecessors().keySet()) {
                for (State pPrime : p.getCallPredecessors(c)) {
                    for (Symbol r : q.getReturnSuccessors().keySet()) {
                        for (State qPrime : q.getReturnSuccessors(r, pPrime.getName())) {
                            frontier.add(Pair.of(pPrime, qPrime));
                        }
                    }
                }
            }

            for (State pPrime : a.getStates()) {
                if (!getInnerVector().get(Pair.of(q, pPrime)).isEmpty()) {
                    frontier.add(Pair.of(p, pPrime));
                }
                if (!getInnerVector().get(Pair.of(pPrime, p)).isEmpty()) {
                    frontier.add(Pair.of(pPrime, q));
                }
            }
        }
    }
}
