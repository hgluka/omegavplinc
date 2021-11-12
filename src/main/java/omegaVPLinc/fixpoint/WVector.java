package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;

import java.util.*;

public class WVector extends FixpointVector<Map<State, Set<State>>> {
    public WVector(VPA a, VPA b) {
        super(a, b, new MapComparator());
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // Epsilon context if p == q
            if (p.equals(q)) {
                if (antichainInsert(pq, Set.of(b.getEpsilonContext())))
                    changed.add(pq);
            }
            // Union of aX_{p', q} for (p, a, p') in internalTransitions
            for (Symbol s : p.getInternalSuccessors().keySet()) {
                Map<State, Set<State>> bCtxOfS = b.context(s);
                for (State pPrime : p.getInternalSuccessors().getOrDefault(s, new HashSet<>())) {
                    Set<Map<State, Set<State>>> toAdd =
                            State.compose(Set.of(bCtxOfS), innerVectorCopy.get(Pair.of(pPrime, q)));
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
                                    Set<Map<State, Set<State>>> toAdd =
                                            State.compose(
                                                    State.compose(
                                                            Set.of(b.context(callSymbol)),
                                                            innerVectorCopy.get(Pair.of(pPrime, qPrime))
                                                    ),
                                                    Set.of(b.context(retSymbol))
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
    public Set<Pair<State, State>> frontier() {
        Set<Pair<State, State>> frontier = new HashSet<>();
        for (Pair<State, State> pq : changed) {
            State p = pq.fst();
            State q = pq.snd();

            for (Symbol c : p.getCallPredecessors().keySet()) {
                for (String g : p.getCallPredecessors().get(c).keySet()) {
                    for (State pPrime : p.getCallPredecessors().get(c).get(g)) {
                        for (Symbol r : q.getCallSuccessors().keySet()) {
                            for (State qPrime : q.getCallSuccessors().get(r).getOrDefault(g, new HashSet<>())) {
                                frontier.add(Pair.of(pPrime, qPrime));
                            }
                        }
                    }
                }
            }

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
