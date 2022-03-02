package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.WVector;
import omegaVPLinc.fixpoint.compare.PairComparator;
import omegaVPLinc.utility.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PeriodWVector extends WVector {
    private static final Logger logger = LoggerFactory.getLogger(PeriodWVector.class);
    public PeriodWVector(VPA a, VPA b) {
        super(a, b, new PairComparator());
    }

    @Override
    public void initial() {
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // Epsilon context if p == q
            if (p.equals(q))
                if (antichainInsert(pq, Set.of(b.getEpsilonContext())))
                    changed.add(pq);
            // Union of aX_{p', q} for (p, a, p') in internalTransitions
            for (Symbol s : p.getInternalSuccessors().keySet()) {
                if (p.getInternalSuccessors(s).contains(q)) {
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
            // Union of cX_{p', q'}r for
            // (p, c, p', g) in callTransitions and
            // (q', r, g, q) in returnTransitions
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (State pPrime : p.getCallSuccessors(c)) {
                    for (Symbol r : q.getReturnPredecessors().keySet()) {
                        for (State qPrime : q.getReturnPredecessors(r, p.getName())) {
                            if (antichainInsert(pq, Context.compose(c, getOldInnerFrontier(pPrime, qPrime), r)))
                                changed.add(pq);
                            /*
                            if (antichainInsert(pq, Context.compose(Set.of(b.contextPair(c)), Context.compose(getOldInnerFrontier(pPrime, qPrime), Set.of(b.contextPair(r))))))
                                changed.add(pq);
                             */
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
}
