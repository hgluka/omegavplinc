package omegaVPLinc.fixpoint.prefix;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.CVector;
import omegaVPLinc.fixpoint.FixpointVector;
import omegaVPLinc.fixpoint.RVector;
import omegaVPLinc.fixpoint.compare.MapComparator;
import omegaVPLinc.utility.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UVector extends FixpointVector {
    private final CVector cVector;
    private final RVector rVector;

    public UVector(VPA a,
                   VPA b,
                   CVector cVector,
                   RVector rVector) {
        super(a, b, new MapComparator());
        this.cVector = cVector;
        this.rVector = rVector;
        for (State p : a.getStates()) {
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (State q : p.getCallSuccessors(c)) {
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
            // ctx(c) for (p, c, q, s) in callTransitions
            for (Symbol c : p.getCallSuccessors().keySet()) {
                if (p.getCallSuccessors(c).contains(q)) {
                    if (antichainInsert(pq, Set.of(b.context(c))))
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
        cVector.noChanged();
        rVector.noChanged();
        frontier();
        i++;
        while (!changed.isEmpty()) {
            iterateOnce();
            updateCopy();
            updateInnerFrontier();
            frontier();
            i++;
        }
        return i;
    }

    @Override
    public void iterateOnce() {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // Y_{p, p'}T_{p',q'}Z_{q', q}
            for (State pPrime : a.getStates()) {
                if (!cVector.getInnerVectorCopy().get(Pair.of(p, pPrime)).isEmpty()) {
                    for (State qPrime : a.getStates()) {
                        if (!innerVectorCopy.get(Pair.of(pPrime, qPrime)).isEmpty() && !rVector.getInnerVectorCopy().get(Pair.of(qPrime, q)).isEmpty()) {
                            if (!cVector.getOldInnerFrontier(p, pPrime).isEmpty() || !getOldInnerFrontier(pPrime, qPrime).isEmpty() || !rVector.getOldInnerFrontier(qPrime, q).isEmpty()) {
                                if (antichainInsert(pq, Context.compose(cVector.getInnerVectorCopy().get(Pair.of(p, pPrime)), Context.compose(innerVectorCopy.get(Pair.of(pPrime, qPrime)), rVector.getInnerVectorCopy().get(Pair.of(qPrime, q))))))
                                    changed.add(pq);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void frontier() {
        frontier = new HashSet<>();
        for (Pair<State, State> pq : changed) {
            for (State p : a.getStates()) {
                if (!cVector.getInnerVectorCopy().get(Pair.of(p, pq.fst())).isEmpty()) {
                    for (State q : a.getStates()) {
                        if (!rVector.getInnerVectorCopy().get(Pair.of(pq.snd(), q)).isEmpty()) {
                            frontier.add(Pair.of(p, q));
                        }
                    }
                }
            }
        }
        for (Pair<State, State> pq : cVector.getChanged()) {
            State p = pq.fst();
            for (State qPrime : a.getStates()) {
                frontier.add(Pair.of(p, qPrime));
            }
        }
        for (Pair<State, State> pq : rVector.getChanged()) {
            State q = pq.snd();
            for (State qPrime : a.getStates()) {
                frontier.add(Pair.of(qPrime, q));
            }
        }
    }
}
