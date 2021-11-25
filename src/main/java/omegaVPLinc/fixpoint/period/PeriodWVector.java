package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.WVector;
import omegaVPLinc.fixpoint.compare.PairComparator;
import omegaVPLinc.utility.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PeriodWVector extends WVector<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> {
    public PeriodWVector(VPA a, VPA b) {
        super(a, b, new PairComparator());
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> potentialAdditions = new HashSet<>();
            // Epsilon context if p == q
            if (p.equals(q))
                potentialAdditions.add(Pair.of(b.getEpsilonContext(), b.getFinalEpsilonContext()));
            // Union of aX_{p', q} for (p, a, p') in internalTransitions
            for (Symbol a : p.getInternalSuccessors().keySet()) {
                for (State pPrime : p.getInternalSuccessors(a)) {
                    potentialAdditions.addAll(State.composeP(Set.of(b.contextPair(a)), innerVectorCopy.get(Pair.of(pPrime, q))));
                }
            }
            // Union of cX_{p', q'}r for
            // (p, c, p', g) in callTransitions and
            // (q', r, g, q) in returnTransitions
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (State pPrime : p.getCallSuccessors(c)) {
                    for (Symbol r : q.getReturnPredecessors().keySet()) {
                        for (State qPrime : q.getReturnPredecessors(r, p.getName())) {
                            potentialAdditions.addAll(State.composeP(Set.of(b.contextPair(c)), State.composeP(innerVectorCopy.get(Pair.of(pPrime, qPrime)), Set.of(b.contextPair(r)))));
                        }
                    }
                }
            }
            // Union of X_{p, q'}X_{q', q} for q' in states of A
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
