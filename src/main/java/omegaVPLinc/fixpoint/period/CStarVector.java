package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.common.CVector;
import omegaVPLinc.fixpoint.FixpointVector;
import omegaVPLinc.utility.Pair;

import java.util.HashSet;
import java.util.Set;

public class CStarVector extends FixpointVector {
    private final WStarVector wStarVector;
    private final CVector cVector;

    public CStarVector(VPA a, VPA b, WStarVector wStarVector, CVector cVector) {
        super(a, b, true);
        this.wStarVector = wStarVector;
        this.cVector = cVector;
        for (State p : a.getStates()) {
            for (Symbol r : p.getReturnSuccessors().keySet()) {
                for (State q : p.getReturnSuccessors(r, a.getEmptyStackSymbol())) {
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
            // Union of rY_{p', q} for (p, r, |, p') in returnTransitions
            for (Symbol r : p.getReturnSuccessors().keySet()) {
                if ((p.isFinal() || q.isFinal()) && p.getReturnSuccessors(r, a.getEmptyStackSymbol()).contains(q))
                    if (antichainInsert(pq, Set.of(b.context(r, withFinal))))
                        changed.add(pq);
            }
        }
    }

    @Override
    public void iterateOnce() {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // X'_{p, q}
            if (antichainInsert(pq, wStarVector.getOldInnerFrontier(p, q)))
                changed.add(pq);
            // Phi(X, X')_{p, q}
            for (State qPrime : a.getStates()) {
                if (!getOldInnerFrontier(p, qPrime).isEmpty() || !cVector.getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, Context.compose(getOldInnerFrontier(p, qPrime), cVector.getInnerVectorCopy().get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                    if (antichainInsert(pq, Context.compose(innerVectorCopy.get(Pair.of(p, qPrime)), cVector.getOldInnerFrontier(qPrime, q))))
                        changed.add(pq);
                }
                if (!cVector.getOldInnerFrontier(p, qPrime).isEmpty() || !getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, Context.compose(cVector.getOldInnerFrontier(p, qPrime), innerVectorCopy.get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                    if (antichainInsert(pq, Context.compose(cVector.getInnerVectorCopy().get(Pair.of(p, qPrime)), getOldInnerFrontier(qPrime, q))))
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
        cVector.noChanged();
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
        wStarVector.allChanged();
        return i;
    }

    @Override
    public void frontier() {
        frontier = new HashSet<>(wStarVector.getChanged());
        for (Pair<State, State> pq : cVector.getChanged()) {
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
                if (!cVector.getInnerVector().get(Pair.of(q, pPrime)).isEmpty())
                    frontier.add(Pair.of(p, pPrime));
                if (!cVector.getInnerVector().get(Pair.of(pPrime, p)).isEmpty())
                    frontier.add(Pair.of(pPrime, q));
            }
        }
    }
}
