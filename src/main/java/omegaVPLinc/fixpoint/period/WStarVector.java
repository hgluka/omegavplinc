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
        for (State p : a.getStates()) {
            for (Symbol s : p.getInternalSuccessors().keySet()) {
                for (State q : p.getInternalSuccessors(s)) {
                    if (p.isFinal() || q.isFinal()) {
                        frontier.add(Pair.of(p, q));
                    }
                }
            }
        }
    }

    @Override
    public void initial() {
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
    }

    @Override
    public void iterateOnce() {
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
                                if (antichainInsert(pq, State.cErP(c, wVector.getOldInnerFrontier(pPrime, qPrime), r)))
                                    changed.add(pq);
                                /*
                                if (antichainInsert(pq, State.composeP(Set.of(b.contextPair(c)), State.composeP(wVector.getOldInnerFrontier(pPrime, qPrime), Set.of(b.contextPair(r))))))
                                    changed.add(pq);
                                 */
                            }
                            if (antichainInsert(pq, State.cErP(c, getOldInnerFrontier(pPrime, qPrime), r)))
                                changed.add(pq);
                            /*
                            if (antichainInsert(pq, State.composeP(Set.of(b.contextPair(c)), State.composeP(getOldInnerFrontier(pPrime, qPrime), Set.of(b.contextPair(r))))))
                                changed.add(pq);
                             */
                        }
                    }
                }
            }
            // Phi(X, X')_{p, q}
            for (State qPrime : a.getStates()) {
                if (!getOldInnerFrontier(p, qPrime).isEmpty() || !wVector.getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, State.composeP(getOldInnerFrontier(p, qPrime), wVector.getInnerVectorCopy().get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                    if (antichainInsert(pq, State.composeP(innerVectorCopy.get(Pair.of(p, qPrime)), wVector.getOldInnerFrontier(qPrime, q))))
                        changed.add(pq);
                }
                if (!wVector.getOldInnerFrontier(p, qPrime).isEmpty() || !getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, State.composeP(wVector.getOldInnerFrontier(p, qPrime), innerVectorCopy.get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                    if (antichainInsert(pq, State.composeP(wVector.getInnerVectorCopy().get(Pair.of(p, qPrime)), getOldInnerFrontier(qPrime, q))))
                        changed.add(pq);
                }
            }
        }
    }

    @Override
    public void frontier() {
        frontier = new HashSet<>();
        for (Pair<State, State> pq : wVector.getChanged()) {
            State p = pq.fst();
            State q = pq.snd();
            for (Symbol c : p.getCallPredecessors().keySet()) {
                for (State pPrime : p.getCallPredecessors(c)) {
                    for (Symbol r : q.getReturnSuccessors().keySet()) {
                        for (State qPrime : q.getReturnSuccessors(r, pPrime.getName())) {
                            if (pPrime.isFinal() || qPrime.isFinal())
                                frontier.add(Pair.of(pPrime, qPrime));
                        }
                    }
                }
            }
            for (State qPrime : a.getStates()) {
                if (!getInnerVector().get(Pair.of(q, qPrime)).isEmpty())
                    frontier.add(Pair.of(p, qPrime));
                if (!getInnerVector().get(Pair.of(qPrime, p)).isEmpty())
                    frontier.add(Pair.of(qPrime, q));
            }
        }
        for (Pair<State, State> pq : changed) {
            State p = pq.fst();
            State q = pq.snd();

            for (Symbol c : p.getCallPredecessors().keySet()) {
                for (State pPrime : p.getCallPredecessors(c)) {
                    for (Symbol r : q.getReturnSuccessors().keySet()) {
                        for (State qPrime : q.getReturnSuccessors(r, pPrime.getName())) {
                            frontier.add(Pair.of(pPrime, qPrime));
                        }
                    }
                }
            }
            for (State pPrime : a.getStates()) {
                if (!wVector.getInnerVector().get(Pair.of(q, pPrime)).isEmpty())
                    frontier.add(Pair.of(p, pPrime));
                if (!wVector.getInnerVector().get(Pair.of(pPrime, p)).isEmpty())
                    frontier.add(Pair.of(pPrime, q));
            }
        }
    }
}
