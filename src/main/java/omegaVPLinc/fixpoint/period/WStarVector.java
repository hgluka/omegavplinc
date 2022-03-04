package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.FixpointVector;
import omegaVPLinc.fixpoint.common.WVector;
import omegaVPLinc.utility.Pair;

import java.util.HashSet;
import java.util.Set;

public class WStarVector extends FixpointVector {
    private final WVector wVector;

    public WStarVector(VPA a, VPA b, WVector wVector) {
        super(a, b, true);
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
                    if (antichainInsert(pq, Set.of(b.context(s, withFinal))))
                        changed.add(pq);
                }
            }
        }
    }

    @Override
    public int computeFixpoint() {
        int i = 0;
        iterateOnce();
        updateCopy();
        updateInnerFrontier();
        wVector.noChanged();
        frontier();
        i++;
        while (!changed.isEmpty()) {
            iterateOnce();
            updateCopy();
            updateInnerFrontier();
            frontier();
            i++;
        }
        allChanged();
        return i;
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
                                if (antichainInsert(pq, Context.compose(c, wVector.getOldInnerFrontier(pPrime, qPrime), r)))
                                    changed.add(pq);
                                /*
                                if (antichainInsert(pq, Context.compose(Set.of(b.contextPair(c)), Context.compose(wVector.getOldInnerFrontier(pPrime, qPrime), Set.of(b.contextPair(r))))))
                                    changed.add(pq);
                                 */
                            } else {
                                if (antichainInsert(pq, Context.compose(c, getOldInnerFrontier(pPrime, qPrime), r)))
                                    changed.add(pq);
                            }
                            /*
                            if (antichainInsert(pq, Context.compose(Set.of(b.contextPair(c)), Context.compose(getOldInnerFrontier(pPrime, qPrime), Set.of(b.contextPair(r))))))
                                changed.add(pq);
                             */
                        }
                    }
                }
            }
            // Phi(X, X')_{p, q}
            for (State qPrime : a.getStates()) {
                if (!getOldInnerFrontier(p, qPrime).isEmpty() || !wVector.getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, Context.compose(getOldInnerFrontier(p, qPrime), wVector.getInnerVectorCopy().get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                    if (antichainInsert(pq, Context.compose(innerVectorCopy.get(Pair.of(p, qPrime)), wVector.getOldInnerFrontier(qPrime, q))))
                        changed.add(pq);
                }
                if (!wVector.getOldInnerFrontier(p, qPrime).isEmpty() || !getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, Context.compose(wVector.getOldInnerFrontier(p, qPrime), innerVectorCopy.get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                    if (antichainInsert(pq, Context.compose(wVector.getInnerVectorCopy().get(Pair.of(p, qPrime)), getOldInnerFrontier(qPrime, q))))
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
