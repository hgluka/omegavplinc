package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.FixpointVector;
import omegaVPLinc.fixpoint.WVector;
import omegaVPLinc.fixpoint.compare.PairComparator;
import omegaVPLinc.utility.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WStarVector extends FixpointVector<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> {
    private final PeriodWVector wVector;

    public WStarVector(VPA a, VPA b, PeriodWVector wVector) {
        super(a, b, new PairComparator());
        this.wVector = wVector;
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> toAdd;
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();

            for (Symbol a : p.getInternalSuccessors().keySet()) {
                for (State pPrime : p.getInternalSuccessors().get(a)) {
                    if (p.isFinal() || pPrime.isFinal()) {
                        toAdd = State.composeP(
                                Set.of(Pair.of(b.context(a), b.finalContext(a))),
                                wVector.getInnerVectorCopy().get(Pair.of(pPrime, q))
                        );
                        if (antichainInsert(pq, toAdd))
                            changed.add(pq);
                    }
                }
            }
            // Union of cX'_{p', q'}r for
            // (p, c, p', g) in callTransitions and
            // (q', r, g, q) in returnTransitions
            // and cX_{p', q'}r if p or q are final
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
                                    toAdd = State.composeP(
                                            State.composeP(
                                                    Set.of(Pair.of(b.context(callSymbol), b.finalContext(callSymbol))),
                                                    innerVectorCopy.get(Pair.of(pPrime, qPrime))
                                            ),
                                            Set.of(Pair.of(b.context(retSymbol), b.finalContext(retSymbol)))
                                    );
                                    if (p.isFinal() || q.isFinal()) {
                                        toAdd.addAll(
                                                State.composeP(
                                                        State.composeP(
                                                                Set.of(Pair.of(b.context(callSymbol), b.finalContext(callSymbol))),
                                                                wVector.getInnerVectorCopy().get(Pair.of(pPrime, qPrime))
                                                        ),
                                                        Set.of(Pair.of(b.context(retSymbol), b.finalContext(retSymbol)))
                                                )
                                        );
                                    }
                                    if (antichainInsert(pq, toAdd))
                                        changed.add(pq);
                                }
                            }
                        }
                    }
                }
            }
            // Phi(X, X')_{p, q}
            for (State qPrime : a.getStates()) {
                toAdd = State.composeP(
                        innerVectorCopy.get(Pair.of(p, qPrime)),
                        wVector.getInnerVectorCopy().get(Pair.of(qPrime, q))
                );
                toAdd.addAll(
                        State.composeP(
                                wVector.getInnerVectorCopy().get(Pair.of(p, qPrime)),
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
        if (changed.isEmpty() && wVector.getChanged().isEmpty()) {
            return a.getAllStatePairs();
        }
        Set<Pair<State, State>> frontier = new HashSet<>();
        for (Pair<State, State> pq : wVector.getChanged()) {
            State p = pq.fst();
            State q = pq.snd();
            for (Symbol c : p.getCallPredecessors().keySet()) {
                for (String g : p.getCallPredecessors().get(c).keySet()) {
                    for (State pPrime : p.getCallPredecessors().get(c).get(g)) {
                        for (Symbol r : q.getReturnSuccessors().keySet()) {
                            for (State qPrime : q.getReturnSuccessors().get(r).getOrDefault(g, new HashSet<>())) {
                                if (pPrime.isFinal() || qPrime.isFinal())
                                    frontier.add(Pair.of(pPrime, qPrime));
                            }
                        }
                    }
                }
            }
            for (State qPrime : a.getStates()) {
                frontier.add(Pair.of(p, qPrime));
                frontier.add(Pair.of(qPrime, q));
            }
        }
        for (Pair<State, State> pq : changed) {
            State p = pq.fst();
            State q = pq.snd();

            for (Symbol c : p.getCallPredecessors().keySet()) {
                for (String g : p.getCallPredecessors().get(c).keySet()) {
                    for (State pPrime : p.getCallPredecessors().get(c).get(g)) {
                        for (Symbol r : q.getReturnSuccessors().keySet()) {
                            for (State qPrime : q.getReturnSuccessors().get(r).getOrDefault(g, new HashSet<>())) {
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
}
