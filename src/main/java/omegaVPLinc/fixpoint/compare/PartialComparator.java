package omegaVPLinc.fixpoint.compare;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;

import java.util.Map;
import java.util.Set;

public class PartialComparator {
    public boolean lesserOrEqual(Context t1, Context t2) {
        if (t1.isWithFinal() && t2.isWithFinal()) {
            return isSubset(t1.getCtx(), t2.getCtx()) && isSubset(t1.getFinalCtx(), t2.getFinalCtx());
        } else {
            return isSubset(t1.getCtx(), t2.getCtx());
        }
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
