package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CVector extends FixpointVector<Map<State, Set<State>>> {
    private final WVector wVector;

    public CVector(VPA a, VPA b, WVector wVector) {
        super(a, b, new MapComparator());
        this.wVector = wVector;
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();

            // X_{p, q}
            if (antichainInsert(pq, wVector.innerVectorCopy.get(pq))) {
                changed.add(pq);
            }
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
        return new HashSet<>(changed);
    }

    @Override
    public Set<Pair<State, State>> frontier() {
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

    @Override
    public Map<Pair<State, State>, Set<Map<State, Set<State>>>> deepCopy() {
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> innerWcopy = new HashMap<>();
        for (Pair<State, State> pq : innerVector.keySet()) {
            innerWcopy.put(pq, new HashSet<>(innerVector.get(pq)));
        }
        return innerWcopy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Pair<State, State> p_q : innerVector.keySet()) {
            if (!innerVector.get(p_q).isEmpty()) {
                sb.append(p_q).append(" {\n");
                for (Map<State, Set<State>> mp : innerVector.get(p_q)) {
                    sb.append("\t").append(mp).append("\n");
                }
                sb.append("}\n");
            }
        }
        return sb.toString();
    }
}
