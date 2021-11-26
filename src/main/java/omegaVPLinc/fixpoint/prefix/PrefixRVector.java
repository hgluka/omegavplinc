package omegaVPLinc.fixpoint.prefix;

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

public class PrefixRVector extends RVector<Map<State, Set<State>>> {
    public PrefixRVector(VPA a, VPA b, WVector<Map<State, Set<State>>> wVector) {
        super(a, b, new MapComparator(), wVector);
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // X_{p, q}
            if (antichainInsert(pq, wVector.getInnerVectorCopy().get(pq)))
                changed.add(pq);
            // Union of cZ_{p', q} for (p, r, g, p') in callTransitions
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (State pPrime : p.getCallSuccessors(c)) {
                    if (antichainInsert(pq, State.composeS(Set.of(b.context(c)), innerVectorCopy.get(Pair.of(pPrime, q)))))
                        changed.add(pq);
                }
            }
            // Union of Z_{p, q'}Z_{q', q}
            for (State qPrime : a.getStates()) {
                if (!innerVectorCopy.get(Pair.of(p, qPrime)).isEmpty() && !innerVectorCopy.get(Pair.of(qPrime, q)).isEmpty()) {
                    if (antichainInsert(pq, State.composeS(innerVectorCopy.get(Pair.of(p, qPrime)), innerVectorCopy.get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                }
            }
        }
        return new HashSet<>(changed);
    }
}
