package omegaVPLinc.fixpoint.compare;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;

import java.util.Map;
import java.util.Set;

public class MapComparator extends PartialComparator<Context> {
    public boolean lesserOrEqual(Context x1, Context x2) {
        return isSubset(x1.getCtx(), x2.getCtx());
    }
}
