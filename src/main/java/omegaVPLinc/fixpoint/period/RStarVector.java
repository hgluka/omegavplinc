package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.FixpointVector;
import omegaVPLinc.fixpoint.compare.PairComparator;
import omegaVPLinc.utility.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RStarVector extends FixpointVector<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> {
    private final WStarVector wStarVector;
    private final PeriodRVector rVector;

    public RStarVector(VPA a, VPA b, WStarVector wStarVector, PeriodRVector rVector) {
        super(a, b, new PairComparator());
        this.wStarVector = wStarVector;
        this.rVector = rVector;
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> toAdd;
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();

            // X_{p, q}
            if (!wStarVector.getInnerVectorCopy().get(pq).isEmpty()) {
                if (antichainInsert(pq, wStarVector.getInnerVectorCopy().get(pq))) {
                    changed.add(pq);
                }
            }
            // Union of cZ_{p', q} for (p, r, g, p') in callTransitions
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (String g : p.getCallSuccessors().get(c).keySet()) {
                    for (State pPrime : p.getCallSuccessors().get(c).get(g)) {
                        toAdd =
                                State.composeP(
                                        Set.of(Pair.of(b.context(c), b.finalContext(c))),
                                        innerVectorCopy.get(Pair.of(pPrime, q))
                                );
                        if (p.isFinal() || pPrime.isFinal()) {
                            toAdd.addAll(
                                    State.composeP(
                                            Set.of(Pair.of(b.context(c), b.finalContext(c))),
                                            rVector.getInnerVector().get(Pair.of(pPrime, q))
                                    )
                            );
                        }
                        if (antichainInsert(pq, toAdd))
                            changed.add(pq);
                    }
                }
            }

            // Phi(X, X')_{p, q}
            for (State qPrime : a.getStates()) {
                toAdd = State.composeP(
                        innerVectorCopy.get(Pair.of(p, qPrime)),
                        rVector.getInnerVectorCopy().get(Pair.of(qPrime, q))
                );
                toAdd.addAll(
                        State.composeP(
                                rVector.getInnerVectorCopy().get(Pair.of(p, qPrime)),
                                innerVectorCopy.get(Pair.of(qPrime, q))
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
        if (changed.isEmpty() && wStarVector.getChanged().isEmpty() && rVector.getChanged().isEmpty()) {
            return a.getAllStatePairs();
        }
        Set<Pair<State, State>> frontier = new HashSet<>(wStarVector.getChanged());
        for (Pair<State, State> pq : rVector.getChanged()) {
            State p = pq.fst();
            State q = pq.snd();

            for (State pPrime : a.getStates()) {
                frontier.add(Pair.of(p, pPrime));
                frontier.add(Pair.of(pPrime, q));
            }
        }
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
}
