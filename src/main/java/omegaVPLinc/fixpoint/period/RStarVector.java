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
            Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> potentialAdditions = new HashSet<>(wStarVector.getInnerVectorCopy().get(pq));
            // Union of cZ_{p', q} for (p, r, g, p') in callTransitions
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (State pPrime : p.getCallSuccessors(c)) {
                    if (p.isFinal() || pPrime.isFinal()) {
                        potentialAdditions.addAll(State.composeP(Set.of(b.contextPair(c)), rVector.getInnerVectorCopy().get(Pair.of(pPrime, q))));
                    }
                    potentialAdditions.addAll(State.composeP(Set.of(b.contextPair(c)), innerVectorCopy.get(Pair.of(pPrime, q))));
                }
            }
            // Phi(X, X')_{p, q}
            for (State qPrime : a.getStates()) {
                if (!innerVectorCopy.get(Pair.of(p, qPrime)).isEmpty() && ! rVector.getInnerVectorCopy().get(Pair.of(qPrime, q)).isEmpty()) {
                    potentialAdditions.addAll(State.composeP(innerVectorCopy.get(Pair.of(p, qPrime)), rVector.getInnerVectorCopy().get(Pair.of(qPrime, q))));
                }
                if (!rVector.getInnerVectorCopy().get(Pair.of(p, qPrime)).isEmpty() && ! innerVectorCopy.get(Pair.of(qPrime, q)).isEmpty()) {
                    potentialAdditions.addAll(State.composeP(rVector.getInnerVectorCopy().get(Pair.of(p, qPrime)), innerVectorCopy.get(Pair.of(qPrime, q))));
                }
            }
            if (antichainInsert(pq, potentialAdditions))
                changed.add(pq);
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
