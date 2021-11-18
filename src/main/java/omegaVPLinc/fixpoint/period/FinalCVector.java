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

public class FinalCVector extends FixpointVector<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> {
    private final FinalWVector finalWVector;
    private final PeriodCVector cVector;

    public FinalCVector(VPA a, VPA b, FinalWVector finalWVector, PeriodCVector cVector) {
        super(a, b, new PairComparator());
        this.finalWVector = finalWVector;
        this.cVector = cVector;
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> toAdd;
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();

            if (!finalWVector.getInnerVectorCopy().get(pq).isEmpty()) {
                if (antichainInsert(pq, finalWVector.getInnerVectorCopy().get(pq)))
                    changed.add(pq);
            }

            // Union of rY_{p', q} for (p, r, |, p') in returnTransitions
            for (Symbol r : p.getReturnSuccessors().keySet()) {
                if (p.getReturnSuccessors().get(r)
                        .containsKey(a.getEmptyStackSymbol())
                ) {
                    for (State pPrime : p.getReturnSuccessors().get(r).get(a.getEmptyStackSymbol())) {
                        toAdd = State.composeP(
                                Set.of(Pair.of(b.context(r), b.finalContext(r))),
                                innerVectorCopy.get(Pair.of(pPrime, q))
                        );
                        if (p.isFinal() || pPrime.isFinal()) {
                            toAdd.addAll(State.composeP(
                                    Set.of(Pair.of(b.context(r), b.finalContext(r))),
                                    cVector.getInnerVector().get(Pair.of(pPrime, q))
                            ));
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
                        cVector.getInnerVectorCopy().get(Pair.of(qPrime, q))
                );
                toAdd.addAll(
                        State.composeP(
                                cVector.getInnerVectorCopy().get(Pair.of(p, qPrime)),
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
        Set<Pair<State, State>> frontier = new HashSet<>(finalWVector.getChanged());
        for (Pair<State, State> pq : cVector.getChanged()) {
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
