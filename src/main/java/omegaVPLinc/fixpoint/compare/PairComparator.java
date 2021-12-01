package omegaVPLinc.fixpoint.compare;

import omegaVPLinc.automaton.State;
import omegaVPLinc.utility.Pair;

import java.util.Map;
import java.util.Set;

public class PairComparator extends PartialComparator<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> {
    public boolean lesserOrEqual(Pair<Map<State, Set<State>>, Map<State, Set<State>>> x1,
                                 Pair<Map<State, Set<State>>, Map<State, Set<State>>> x2) {
        return isSubset(x1.fst(), x2.fst()) && isSubset(x1.snd(), x2.snd());
    }
}
