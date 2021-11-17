package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.compare.PairComparator;
import omegaVPLinc.fixpoint.compare.PartialComparator;
import omegaVPLinc.utility.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PeriodRVector extends RVector<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> {
    public PeriodRVector(VPA a, VPA b, WVector<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> wVector) {
        super(a, b, new PairComparator(), wVector);
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();

            // X_{p, q}
            if (!wVector.innerVectorCopy.isEmpty()) {
                if (antichainInsert(pq, wVector.innerVectorCopy.get(pq))) {
                    changed.add(pq);
                }
            }
            // Union of cZ_{p', q} for (p, r, g, p') in callTransitions
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (String g : p.getCallSuccessors().get(c).keySet()) {
                    for (State pPrime : p.getCallSuccessors().get(c).get(g)) {
                        Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> toAdd =
                                State.composeP(
                                        Set.of(Pair.of(b.context(c), b.finalContext(c))),
                                        innerVectorCopy.get(Pair.of(pPrime, q))
                                );
                        if (antichainInsert(pq, toAdd))
                            changed.add(pq);
                    }
                }
            }
            for (State qPrime : a.getStates()) {
                Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> toAdd =
                        State.composeP(
                                innerVectorCopy.get(Pair.of(p, qPrime)),
                                innerVectorCopy.get(Pair.of(qPrime, q))
                        );
                if (antichainInsert(pq, toAdd))
                    changed.add(pq);
            }
        }
        return new HashSet<>(changed);
    }
}
