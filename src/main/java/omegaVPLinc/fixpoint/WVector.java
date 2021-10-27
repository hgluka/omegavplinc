package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                innerW.put(Pair.of(p, q), new HashSet<>());
            }
        }
    }

    public WVector iterateOnce() {
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> innerWcopy = deepCopy();
        for (State p : a.getStates()) {
            for (State q : a.getStates()) {
                if (p.equals(q)) {
                    innerW.get(Pair.of(p, q)).addAll(Set.of(b.epsilonContext())); // Add context of empty word.
                } else {
                    for (Symbol s : p.getInternalSuccessors().keySet()) {
                        Map<State, Set<State>> bCtxOfS = b.context(s);
                        for (State pp : bCtxOfS.keySet()) {
                            innerW.get(Pair.of(p, q))
                                    .addAll(State.compose(Set.of(b.context(s)), innerWcopy.get(Pair.of(pp, q))));
                        }
                    }
                    // TODO: add cX_{p', q'}r union and X_{p, q'}X_{q', p}
                }
            }
        }
        return this;
    }

    public Map<Pair<State, State>, Set<Map<State, Set<State>>>> deepCopy() {
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> innerWcopy = new HashMap<>();
        for (Pair<State, State> pq : innerW.keySet()) {
            innerWcopy.put(pq, new HashSet<>(innerW.get(pq)));
        }
        return innerWcopy;
    }
}
