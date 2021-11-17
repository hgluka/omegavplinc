package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
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
            if (!wVector.innerVectorCopy.isEmpty()) {
                if (antichainInsert(pq, wVector.innerVectorCopy.get(pq))) {
                    changed.add(pq);
                }
            }
            // Union of rY_{p', q} for (p, r, |, p') in returnTransitions
            for (Symbol r : p.getReturnSuccessors().keySet()) {
                if (p.getReturnSuccessors()
                        .get(r)
                        .containsKey(a.getEmptyStackSymbol())
                ) {
                    for (State pPrime : p.getReturnSuccessors().get(r).get(a.getEmptyStackSymbol())) {
                        Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> toAdd =
                                State.composeP(
                                        Set.of(Pair.of(b.context(r), b.finalContext(r))),
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
