package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.CVector;
import omegaVPLinc.fixpoint.WVector;
import omegaVPLinc.fixpoint.compare.PairComparator;
import omegaVPLinc.utility.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PeriodCVector extends CVector {
    public PeriodCVector(VPA a, VPA b, WVector wVector) {
        super(a, b, new PairComparator(), wVector);
    }

    @Override
    public void initial() {
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // Union of rY_{p', q} for (p, r, |, p') in returnTransitions
            for (Symbol r : p.getReturnSuccessors().keySet()) {
                if (p.getReturnSuccessors(r, a.getEmptyStackSymbol()).contains(q))
                    if (antichainInsert(pq, Set.of(b.contextPair(r))))
                        changed.add(pq);
            }
        }
    }

    @Override
    public void iterateOnce() {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // X_{p, q}
            if (antichainInsert(pq, wVector.getOldInnerFrontier(p, q)))
                changed.add(pq);
            // Union of Y_{p, q'}Y_{q', q}
            for (State qPrime : a.getStates()) {
                if (!getOldInnerFrontier(p, qPrime).isEmpty() || !getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, Context.compose(getOldInnerFrontier(p, qPrime), innerVectorCopy.get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                    if (antichainInsert(pq, Context.compose(innerVectorCopy.get(Pair.of(p, qPrime)), getOldInnerFrontier(qPrime, q))))
                        changed.add(pq);
                }
            }
        }
    }
}
