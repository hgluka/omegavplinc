package omegaVPLinc.fixpoint.prefix;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.CVector;
import omegaVPLinc.fixpoint.FixpointVector;
import omegaVPLinc.fixpoint.RVector;
import omegaVPLinc.fixpoint.compare.MapComparator;
import omegaVPLinc.utility.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UVector extends FixpointVector<Map<State, Set<State>>> {
    private final CVector<Map<State, Set<State>>> cVector;
    private final RVector<Map<State, Set<State>>> rVector;

    public UVector(VPA a,
                   VPA b,
                   CVector<Map<State, Set<State>>> cVector,
                   RVector<Map<State, Set<State>>> rVector) {
        super(a, b, new MapComparator());
        this.cVector = cVector;
        this.rVector = rVector;
    }

    @Override
    public Set<Pair<State, State>> initial(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
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
        return new HashSet<>(changed);
    }

    @Override
    public Set<Pair<State, State>> iterateOnce(Set<Pair<State, State>> frontier) {
        changed = new HashSet<>();
        for (Pair<State, State> pq : frontier) {
            State p = pq.fst();
            State q = pq.snd();
            // Y_{p, p'}T_{p',q'}Z_{q', q}
            for (State pPrime : a.getStates()) {
                if (!cVector.getInnerVectorCopy().get(Pair.of(p, pPrime)).isEmpty()) {
                    for (State qPrime : a.getStates()) {
                        if (!cVector.getOldInnerFrontier(p, pPrime).isEmpty() || !getOldInnerFrontier(pPrime, qPrime).isEmpty() && !rVector.getOldInnerFrontier(qPrime, q).isEmpty()) {
                            if (antichainInsert(pq, State.composeS(cVector.getOldInnerFrontier(p, pPrime), State.composeS(innerVectorCopy.get(Pair.of(pPrime, qPrime)), rVector.getOldInnerFrontier(qPrime, q)))))
                                changed.add(pq);
                        }
                    }
                }
            }
        }
        return new HashSet<>(changed);
    }

    @Override
    public Set<Pair<State, State>> frontier() {
        Set<Pair<State, State>> frontier = new HashSet<>();
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
        return frontier;
    }
}
