package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.compare.MapComparator;
import omegaVPLinc.utility.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UVector extends FixpointVector<Map<State, Set<State>>> {
    private CVector<Map<State, Set<State>>> cVector;
    private RVector<Map<State, Set<State>>> rVector;

    public UVector(VPA a,
                   VPA b,
                   CVector<Map<State, Set<State>>> cVector,
                   RVector<Map<State, Set<State>>> rVector) {
        super(a, b, new MapComparator());
        this.cVector = cVector;
        this.rVector = rVector;
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();

            for (Symbol c : p.getCallSuccessors().keySet()) {
                if (p.getCallSuccessors().get(c)
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())
                        .contains(q)) {
                    if (antichainInsert(pq, Set.of(b.context(c))))
                        changed.add(pq);
                }
            }

            for (State qPrime : a.getStates()) {
                Set<Map<State, Set<State>>> toAdd =
                        State.compose(
                                cVector.innerVectorCopy.get(Pair.of(p, qPrime)),
                                State.compose(
                                        innerVectorCopy.get(Pair.of(qPrime, q)),
                                        rVector.innerVectorCopy.get(Pair.of(q, p))
                                )
                        );
                if (antichainInsert(pq, toAdd))
                    changed.add(pq);
            }
        }
        return new HashSet<>(changed);
    }

    @Override
    public Set<Pair<State, State>> frontier() {
        Set<Pair<State, State>> frontier = new HashSet<>(rVector.changed);
        for (Pair<State, State> pq : cVector.changed) {
            State p = pq.fst();
            for (State qPrime : a.getStates()) {
                frontier.add(Pair.of(p, qPrime));
            }
        }
        for (Pair<State, State> pq : changed) {
            State q = pq.snd();
            for (State qPrime : a.getStates()) {
                frontier.add(Pair.of(qPrime, q));
            }
        }
        return frontier;
    }
}
