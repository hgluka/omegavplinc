package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.FixpointVector;
import omegaVPLinc.fixpoint.common.RVector;
import omegaVPLinc.utility.Pair;

import java.util.HashSet;
import java.util.Set;

public class RStarVector extends FixpointVector {
    private final WStarVector wStarVector;
    private final RVector rVector;

    public RStarVector(VPA a, VPA b, boolean withWords, WStarVector wStarVector, RVector rVector) {
        super(a, b, true, withWords);
        this.wStarVector = wStarVector;
        this.rVector = rVector;
        for (State p : a.getStates()) {
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (State q : p.getCallSuccessors(c)) {
                    if (p.isFinal() || q.isFinal())
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
            // Union of cZ_{p', q} for (p, r, g, p') in callTransitions
            for (Symbol c : p.getCallSuccessors().keySet()) {
                if ((p.isFinal() || q.isFinal()) && p.getCallSuccessors(c).contains(q)) {
                    if (antichainInsert(pq, Set.of(b.context(c, withFinal, withWords))))
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
        wStarVector.noChanged();
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
        allChanged();
        return i;
    }

    @Override
    public void iterateOnce() {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // X_{p, q}
            if (antichainInsert(pq, wStarVector.getOldInnerFrontier(p, q)))
                changed.add(pq);
            // Phi(X, X')_{p, q}
            for (State qPrime : a.getStates()) {
                if (!getOldInnerFrontier(p, qPrime).isEmpty() || !rVector.getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, Context.compose(getOldInnerFrontier(p, qPrime), rVector.getInnerVectorCopy().get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                    if (antichainInsert(pq, Context.compose(innerVectorCopy.get(Pair.of(p, qPrime)), rVector.getOldInnerFrontier(qPrime, q))))
                        changed.add(pq);
                }
                if (!rVector.getOldInnerFrontier(p, qPrime).isEmpty() || !getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, Context.compose(rVector.getOldInnerFrontier(p, qPrime), innerVectorCopy.get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                    if (antichainInsert(pq, Context.compose(rVector.getInnerVectorCopy().get(Pair.of(p, qPrime)), getOldInnerFrontier(qPrime, q))))
                        changed.add(pq);
                }
            }
        }
    }

    @Override
    public void frontier() {
        frontier = new HashSet<>(wStarVector.getChanged());
        for (Pair<State, State> pq : rVector.getChanged()) {
            State p = pq.fst();
            State q = pq.snd();

            for (State pPrime : a.getStates()) {
                if (!getInnerVector().get(Pair.of(q, pPrime)).isEmpty())
                    frontier.add(Pair.of(p, pPrime));
                if (!getInnerVector().get(Pair.of(pPrime, p)).isEmpty())
                    frontier.add(Pair.of(pPrime, q));
            }
        }
        for (Pair<State, State> pq : changed) {
            State p = pq.fst();
            State q = pq.snd();

            for (State pPrime : a.getStates()) {
                if (!rVector.getInnerVector().get(Pair.of(q, pPrime)).isEmpty())
                    frontier.add(Pair.of(p, pPrime));
                if (!rVector.getInnerVector().get(Pair.of(pPrime, p)).isEmpty())
                    frontier.add(Pair.of(pPrime, q));
            }
        }
    }
}
