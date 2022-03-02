package omegaVPLinc.fixpoint.compare;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.utility.Pair;

import java.util.Map;
import java.util.Set;

public class PairComparator extends PartialComparator<Context> {
    public boolean lesserOrEqual(Context x1,
                                 Context x2) {
        return isSubset(x1.getCtx(), x2.getCtx()) && isSubset(x1.getFinalCtx(), x2.getFinalCtx());
    }
}
