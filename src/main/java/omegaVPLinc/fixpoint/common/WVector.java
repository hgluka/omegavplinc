package omegaVPLinc.fixpoint.common;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.FixpointVector;
import omegaVPLinc.fixpoint.compare.PartialComparator;
import omegaVPLinc.utility.Pair;

import java.util.*;

public class WVector extends FixpointVector {
    public WVector(VPA a, VPA b, boolean withFinal, boolean withWords) {
        super(a, b, withFinal, withWords);
        for (State p : a.getStates()) {
            frontier.add(Pair.of(p, p));
            for (Symbol s : p.getInternalSuccessors().keySet()) {
                for (State q : p.getInternalSuccessors(s)) {
                    frontier.add(Pair.of(p, q));
                }
            }
        }
    }

    @Override
    public void initial() {
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // Epsilon context if p == q
            if (p.equals(q)) {
                if (antichainInsert(pq, Set.of(b.getEpsilonContext(withFinal, withWords))))
                    changed.add(pq);
            }
            // Union of aX_{p', q} for (p, a, p') in internalTransitions
            for (Symbol s : p.getInternalSuccessors().keySet()) {
                if (p.getInternalSuccessors(s).contains(q)) {
                    if (antichainInsert(pq, Set.of(b.context(s, withFinal, withWords))))
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
            // Union of cX_{p', q'}r for
            // (p, c, p', g) in callTransitions and
            // (q', r, g, q) in returnTransitions
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (State pPrime : p.getCallSuccessors(c)) {
                    for (Symbol r : q.getReturnPredecessors().keySet()) {
                        for (State qPrime : q.getReturnPredecessors(r, p.getName())) {
                            if (antichainInsert(pq, Context.compose(c, getOldInnerFrontier(pPrime, qPrime), r))) {
                                changed.add(pq);
                            }
                        }
                    }
                }
            }
            // Union of X_{p, q'}X_{q', q} for q' in states of A
            for (State qPrime : a.getStates()) {
                if (!getOldInnerFrontier(p, qPrime).isEmpty() || !getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, Context.compose(getOldInnerFrontier(p, qPrime), innerVectorCopy.get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                    if (antichainInsert(pq, Context.compose(innerVectorCopy.get(Pair.of(p, qPrime)), getOldInnerFrontier(qPrime, q))))
                        changed.add(pq);
                }
            }
        }
    }

    @Override
    public void frontier() {
        frontier = new HashSet<>();
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
                if (!getInnerVector().get(Pair.of(q, pPrime)).isEmpty()) {
                    frontier.add(Pair.of(p, pPrime));
                }
                if (!getInnerVector().get(Pair.of(pPrime, p)).isEmpty()) {
                    frontier.add(Pair.of(pPrime, q));
                }
            }
        }
    }
}
