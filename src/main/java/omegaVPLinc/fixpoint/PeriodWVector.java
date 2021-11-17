package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
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
            // Epsilon context if p == q
            if (p.equals(q)) {
                if (antichainInsert(pq, Set.of(Pair.of(b.getEpsilonContext(), b.getFinalEpsilonContext()))))
                    changed.add(pq);
            }
            // Union of aX_{p', q} for (p, a, p') in internalTransitions
            for (Symbol s : p.getInternalSuccessors().keySet()) {
                Map<State, Set<State>> bCtxOfS = b.context(s);
                Map<State, Set<State>> finalBCtxOfS = b.finalContext(s);
                for (State pPrime : p.getInternalSuccessors().getOrDefault(s, new HashSet<>())) {
                    Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> toAdd =
                            State.composeP(Set.of(Pair.of(bCtxOfS, finalBCtxOfS)), innerVectorCopy.get(Pair.of(pPrime, q)));
                    if (antichainInsert(pq, toAdd))
                        changed.add(pq);
                }
            }
            // Union of cX_{p', q'}r for
            // (p, c, p', g) in callTransitions and
            // (q', r, g, q) in returnTransitions
            for (Symbol callSymbol : p.getCallSuccessors().keySet()) {
                for (String stackSymbol : p.getCallSuccessors().get(callSymbol).keySet()) {
                    Set<State> successorsOfCallSymbol = p
                            .getCallSuccessors()
                            .get(callSymbol)
                            .get(stackSymbol);
                    for (Symbol retSymbol : q.getReturnPredecessors().keySet()) {
                        HashMap<String, Set<State>> predecessorsOfRetSymbol =
                                q.getReturnPredecessors()
                                        .get(retSymbol);
                        if (predecessorsOfRetSymbol.containsKey(stackSymbol)) {
                            for (State pPrime : successorsOfCallSymbol) {
                                for (State qPrime : predecessorsOfRetSymbol.get(stackSymbol)) {
                                    Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> toAdd =
                                            State.composeP(
                                                    State.composeP(
                                                            Set.of(Pair.of(b.context(callSymbol), b.finalContext(callSymbol))),
                                                            innerVectorCopy.get(Pair.of(pPrime, qPrime))
                                                    ),
                                                    Set.of(Pair.of(b.context(retSymbol), b.finalContext(retSymbol)))
                                            );
                                    if (antichainInsert(pq, toAdd))
                                        changed.add(pq);
                                }
                            }
                        }
                    }
                }
            }
            // Union of X_{p, q'}X_{q', q} for q' in states of A
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
