package omegaVPLinc.fixpoint.compare;

import omegaVPLinc.automaton.State;

import java.util.Map;
import java.util.Set;

public abstract class PartialComparator<T> {
    public abstract boolean lesserOrEqual(T t1, T t2);
}
