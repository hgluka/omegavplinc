package omegaVPLinc.fixpoint.period;

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

public class PeriodCVector extends CVector<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> {
    public PeriodCVector(VPA a, VPA b, WVector<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> wVector) {
        super(a, b, new PairComparator(), wVector);
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // X_{p, q}
            Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> potentialAdditions = new HashSet<>(wVector.getInnerVectorCopy().get(pq));
            // Union of rY_{p', q} for (p, r, |, p') in returnTransitions
            for (Symbol r : p.getReturnSuccessors().keySet()) {
                for (State pPrime : p.getReturnSuccessors(r, a.getEmptyStackSymbol())) {
                    potentialAdditions.addAll(State.composeP(Set.of(b.contextPair(r)), innerVectorCopy.get(Pair.of(pPrime, q))));
                }
            }
            // Union of Y_{p, q'}Y_{q', q}
            for (State qPrime : a.getStates()) {
                if (!innerVectorCopy.get(Pair.of(p, qPrime)).isEmpty() && !innerVectorCopy.get(Pair.of(qPrime, q)).isEmpty()) {
                    potentialAdditions.addAll(State.composeP(innerVectorCopy.get(Pair.of(p, qPrime)), innerVectorCopy.get(Pair.of(qPrime, q))));
                }
            }
            if (antichainInsert(pq, potentialAdditions))
                changed.add(pq);
        }
        return new HashSet<>(changed);
    }
}
