package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CVector extends FixpointVector<Map<State, Set<State>>> {
    private Map<Pair<State, State>, Set<Map<State, Set<State>>>> wVectorCopy;

    public CVector(VPA a, VPA b, Map<Pair<State, State>, Set<Map<State, Set<State>>>> wVectorCopy) {
        super(a, b, new MapComparator());
        this.wVectorCopy = wVectorCopy;
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        Set<Pair<State, State>> changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();

            // X_{p, q}
            if (antichainInsert(pq, wVectorCopy.get(pq)))
                changed.add(pq);
            // Union of rY_{p', q} for (p, r, |, p') in returnTransitions
            for (Symbol r : p.getReturnSuccessors().keySet()) {
                if (p.getReturnSuccessors()
                        .get(r)
                        .containsKey(a.getEmptyStackSymbol())
                ) {
                    for (State pPrime : p.getReturnSuccessors().get(r).get(a.getEmptyStackSymbol())) {
                        Set<Map<State, Set<State>>> toAdd =
                                State.compose(
                                        Set.of(b.context(r)),
                                        innerVectorCopy.get(Pair.of(pPrime, q))
                                );
                        if (antichainInsert(pq, toAdd))
                            changed.add(pq);
                    }
                }
            }
            for (State qPrime : a.getStates()) {
                Set<Map<State, Set<State>>> toAdd =
                        State.compose(
                                innerVectorCopy.get(Pair.of(p, qPrime)),
                                innerVectorCopy.get(Pair.of(qPrime, q))
                        );
                if (antichainInsert(pq, toAdd))
                    changed.add(pq);
            }
        }
        return changed;
    }

    @Override
    public Set<Pair<State, State>> frontier(Set<Pair<State, State>> changed) {
        // Whatever changed in the W vector in the last iteration
        // needs to be added to this frontier as well
        Set<Pair<State, State>> frontier = new HashSet<>();
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

    @Override
    public Map<Pair<State, State>, Set<Map<State, Set<State>>>> deepCopy() {
        return null;
    }
}
