package omegaVPLinc.fixpoint.compare;

import omegaVPLinc.automaton.State;

import java.util.Map;
import java.util.Set;

public abstract class PartialComparator<T> {
    public abstract boolean lesserOrEqual(T t1, T t2);

    public boolean isSubset(Map<State, Set<State>> firstMap, Map<State, Set<State>> secondMap) {
        for (Map.Entry<State, Set<State>> entry : firstMap.entrySet()) {
            if (!secondMap.containsKey(entry.getKey())) return false;
            if (!secondMap.get(entry.getKey()).containsAll(entry.getValue()))
                return false;
        }
        return true;
    }
}
