package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;

import java.util.*;

public class WVector {

    private VPA a;
    private VPA b;

    private Map<Pair<State, State>, Set<Map<State, Set<State>>>> innerW;

    public WVector(VPA a, VPA b) {
        this.a = a;
        this.b = b;
        this.innerW = new HashMap<>();
        for (State p : a.getStates()) {
            for (State q : a.getStates()) {
                if (p.equals(q)) {
                    innerW.put(Pair.of(p, q), new HashSet<>(Set.of(b.getEpsilonContext())));
                } else {
                    innerW.put(Pair.of(p, q), new HashSet<>());
                }
            }
        }
    }

    public Map<Pair<State, State>, Set<Map<State, Set<State>>>> getInnerW() {
        return innerW;
    }

    public Set<Pair<State, State>> iterateOnce(
            Map<Pair<State, State>, Set<Map<State, Set<State>>>> innerWcopy,
            Set<Pair<State, State>> frontier) {
        innerWcopy = deepCopy();
        Set<Pair<State, State>> changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // Union of aX_{p', q} for (p, a, p') in internalTransitions
            for (Symbol s : p.getInternalSuccessors().keySet()) {
                Map<State, Set<State>> bCtxOfS = b.context(s);
                for (State pPrime : p.getInternalSuccessors().getOrDefault(s, new HashSet<>())) {
                    Set<Map<State, Set<State>>> toAdd =
                            State.compose(Set.of(bCtxOfS), innerWcopy.get(Pair.of(pPrime, q)));
                    if (!toAdd.isEmpty() && !innerWcopy.get(pq).containsAll(toAdd)) {
                        innerW.get(pq).addAll(toAdd);
                        changed.add(pq);
                    }
                }
            }
            // Union of cX_{p', q'}r for
            // (p, c, p', g) in callTransitions and
            // (q', r, g, q) in returnTransitions
            for (Symbol callSymbol : p.getCallSuccessors().keySet()) {
                for (String stackSymbol : p.getCallSuccessors().get(callSymbol).keySet()) {
                    Set<State> successorsOfCallSymbol = p
                            .getCallPredecessors()
                            .getOrDefault(callSymbol, new HashMap<>())
                            .getOrDefault(stackSymbol, new HashSet<>());
                    for (Symbol retSymbol : q.getReturnPredecessors().keySet()) {
                        HashMap<String, Set<State>> predecessorsOfRetSymbol =
                                q.getReturnPredecessors()
                                .getOrDefault(retSymbol, new HashMap<>());
                        if (predecessorsOfRetSymbol.containsKey(stackSymbol)) {
                            for (State pPrime : successorsOfCallSymbol) {
                                for (State qPrime : predecessorsOfRetSymbol.get(stackSymbol)) {
                                    Set<Map<State, Set<State>>> toAdd =
                                            State.compose(
                                                    State.compose(
                                                            Set.of(b.context(callSymbol)),
                                                            innerWcopy.get(Pair.of(pPrime, qPrime))
                                                    ),
                                                    Set.of(b.context(retSymbol))
                                            );
                                    if (!toAdd.isEmpty() && !innerWcopy.get(pq).containsAll(toAdd)) {
                                        innerW.get(pq).addAll(toAdd);
                                        changed.add(pq);
                                    }
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
                                innerWcopy.get(Pair.of(p, qPrime)),
                                innerWcopy.get(Pair.of(qPrime, q))
                        );
                if (!toAdd.isEmpty() && !innerWcopy.get(pq).containsAll(toAdd)) {
                    innerW.get(pq).addAll(toAdd);
                    changed.add(pq);
                }
            }
        }
        return changed;
    }

    public Map<Pair<State, State>, Set<Map<State, Set<State>>>> deepCopy() {
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> innerWcopy = new HashMap<>();
        for (Pair<State, State> pq : innerW.keySet()) {
            innerWcopy.put(pq, new HashSet<>(innerW.get(pq)));
        }
        return innerWcopy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Pair<State, State> p_q : innerW.keySet()) {
            if (!innerW.get(p_q).isEmpty()) {
                sb.append(p_q + " {\n");
                for (Map<State, Set<State>> mp : innerW.get(p_q)) {
                    sb.append("\t" + mp + "\n");
                }
                sb.append("}\n");
            }
        }
        return sb.toString();
    }
}
