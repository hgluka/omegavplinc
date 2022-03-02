package omegaVPLinc.fixpoint.prefix;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.RVector;
import omegaVPLinc.fixpoint.WVector;
import omegaVPLinc.fixpoint.compare.MapComparator;
import omegaVPLinc.utility.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrefixRVector extends RVector {
    public PrefixRVector(VPA a, VPA b, WVector wVector) {
        super(a, b, new MapComparator(), wVector);
    }

    @Override
    public void initial() {
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // Union of cZ_{p', q} for (p, r, g, p') in callTransitions
            for (Symbol c : p.getCallSuccessors().keySet()) {
                if (p.getCallSuccessors(c).contains(q)) {
                    if (antichainInsert(pq, Set.of(b.context(c)))) {
                        changed.add(pq);
                    }
                }
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
            // Union of cZ_{p', q} for (p, r, g, p') in callTransitions
            // Union of Z_{p, q'}Z_{q', q}
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
