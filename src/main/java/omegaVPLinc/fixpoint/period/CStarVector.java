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

public class CStarVector extends FixpointVector<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> {
    private final WStarVector wStarVector;
    private final PeriodCVector cVector;

    public CStarVector(VPA a, VPA b, WStarVector wStarVector, PeriodCVector cVector) {
        super(a, b, new PairComparator());
        this.wStarVector = wStarVector;
        this.cVector = cVector;
    }

    @Override
    public Set<Pair<State, State>> initial(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // Union of rY_{p', q} for (p, r, |, p') in returnTransitions
            for (Symbol r : p.getReturnSuccessors().keySet()) {
                if ((p.isFinal() || q.isFinal()) && p.getReturnSuccessors(r, a.getEmptyStackSymbol()).contains(q))
                    if (antichainInsert(pq, Set.of(b.contextPair(r))))
                        changed.add(pq);
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
            // X'_{p, q}
            if (antichainInsert(pq, wStarVector.getOldInnerFrontier(p, q)))
                changed.add(pq);
            // Phi(X, X')_{p, q}
            for (State qPrime : a.getStates()) {
                if (!getOldInnerFrontier(p, qPrime).isEmpty() || !cVector.getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, State.composeP(innerVectorCopy.get(Pair.of(p, qPrime)), cVector.getInnerVectorCopy().get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                }
                if (!cVector.getOldInnerFrontier(p, qPrime).isEmpty() || !getOldInnerFrontier(qPrime, q).isEmpty()) {
                    if (antichainInsert(pq, State.composeP(cVector.getInnerVectorCopy().get(Pair.of(p, qPrime)), innerVectorCopy.get(Pair.of(qPrime, q)))))
                        changed.add(pq);
                }
            }
        }
        return new HashSet<>(changed);
    }

    @Override
    public Set<Pair<State, State>> frontier() {
        Set<Pair<State, State>> frontier = new HashSet<>(wStarVector.getChanged());
        for (Pair<State, State> pq : cVector.getChanged()) {
            State p = pq.fst();
            State q = pq.snd();

            for (State pPrime : a.getStates()) {
                frontier.add(Pair.of(p, pPrime));
                frontier.add(Pair.of(pPrime, q));
            }
        }
        for (Pair<State, State> pq : changed) {
            State p = pq.fst();
            State q = pq.snd();

            for (State pPrime : a.getStates()) {
                frontier.add(Pair.of(p, pPrime));
                frontier.add(Pair.of(pPrime, q));
            }
        }
        return frontier;
    }
}