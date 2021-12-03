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

public class WStarVector extends FixpointVector<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> {
    private final PeriodWVector wVector;

    public WStarVector(VPA a, VPA b, PeriodWVector wVector) {
        super(a, b, new PairComparator());
        this.wVector = wVector;
    }

    @Override
    public Set<Pair<State, State>> initial(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // Union of aX_{p', q} for (p, a, p') in internalTransitions
            // if p or p' are final
            for (Symbol s : p.getInternalSuccessors().keySet()) {
                if ((p.isFinal() || q.isFinal()) && p.getInternalSuccessors(s).contains(q)) {
                    if (antichainInsert(pq, Set.of(b.contextPair(s))))
                        changed.add(pq);
                }
            }
        }
        return new HashSet<>(changed);
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();

            // Union of cX'_{p', q'}r for
            // (p, c, p', g) in callTransitions and
            // (q', r, g, q) in returnTransitions
            // and cX_{p', q'}r if p or q are final
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (State pPrime : p.getCallSuccessors(c)) {
                    for (Symbol r : q.getReturnPredecessors().keySet()) {
                        for (State qPrime : q.getReturnPredecessors(r, p.getName())) {
                            if (p.isFinal() || q.isFinal()) {
                                if (antichainInsert(pq, State.composeP(Set.of(b.contextPair(c)), State.composeP(wVector.getOldInnerFrontier(pPrime, qPrime), Set.of(b.contextPair(r))))))
                                    changed.add(pq);
                            }
                            if (antichainInsert(pq, State.composeP(Set.of(b.contextPair(c)), State.composeP(getOldInnerFrontier(pPrime, qPrime), Set.of(b.contextPair(r))))))
                                changed.add(pq);
                        }
                    }
                }
            }
            // Phi(X, X')_{p, q}
            for (State qPrime : a.getStates()) {
                if (!getOldInnerFrontier(p, qPrime).isEmpty() || !wVector.getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, State.composeP(innerVectorCopy.get(Pair.of(p, qPrime)), wVector.getInnerVectorCopy().get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                }
                if (!wVector.getOldInnerFrontier(p, qPrime).isEmpty() || !getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, State.composeP(wVector.getInnerVectorCopy().get(Pair.of(p, qPrime)), innerVectorCopy.get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                }
            }
        }
        return new HashSet<>(changed);
    }

    @Override
    public Set<Pair<State, State>> frontier() {
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
