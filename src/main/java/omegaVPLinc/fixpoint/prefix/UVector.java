package omegaVPLinc.fixpoint.prefix;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.CVector;
import omegaVPLinc.fixpoint.FixpointVector;
import omegaVPLinc.fixpoint.RVector;
import omegaVPLinc.fixpoint.compare.MapComparator;
import omegaVPLinc.utility.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UVector extends FixpointVector<Map<State, Set<State>>> {
    private final CVector<Map<State, Set<State>>> cVector;
    private final RVector<Map<State, Set<State>>> rVector;

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

            for (State pPrime : a.getStates()) {
                for (State qPrime : a.getStates()) {
                    Set<Map<State, Set<State>>> toAdd =
                            State.composeS(
                                    State.composeS(
                                            cVector.getInnerVectorCopy().get(Pair.of(p, pPrime)),
                                            innerVectorCopy.get(Pair.of(pPrime, qPrime))),
                                    rVector.getInnerVectorCopy().get(Pair.of(qPrime, q))
                            );
                    if (antichainInsert(pq, toAdd))
                        changed.add(pq);
                }
            }
        }
        return new HashSet<>(changed);
    }

    @Override
    public Set<Pair<State, State>> frontier() {
        if (!changed.isEmpty() || (cVector.getChanged().isEmpty() && rVector.getChanged().isEmpty())) {
            return a.getAllStatePairs();
        }
        Set<Pair<State, State>> frontier = new HashSet<>();
        for (Pair<State, State> pq : cVector.getChanged()) {
            State p = pq.fst();
            for (State qPrime : a.getStates()) {
                frontier.add(Pair.of(p, qPrime));
            }
        }
        for (Pair<State, State> pq : rVector.getChanged()) {
            State q = pq.snd();
            for (State qPrime : a.getStates()) {
                frontier.add(Pair.of(qPrime, q));
            }
        }
        return frontier;
    }
}
