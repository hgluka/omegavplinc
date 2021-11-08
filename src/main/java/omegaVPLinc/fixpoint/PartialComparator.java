package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;

import java.util.Map;
import java.util.Set;

public class PartialComparator {
    public boolean lesserOrEqual(Map<State, Set<State>> x1, Map<State, Set<State>> x2) {
        return isSubset(x1, x2);
    }

    public boolean isSubset(Map<State, Set<State>> firstMap, Map<State, Set<State>> secondMap) {
        for (Map.Entry<State, Set<State>> entry : firstMap.entrySet()) {
            if (!secondMap.containsKey(entry.getKey())) return false;
            if (!secondMap.get(entry.getKey()).containsAll(entry.getValue()))
                return false;
        }
        return true;
    }
}
