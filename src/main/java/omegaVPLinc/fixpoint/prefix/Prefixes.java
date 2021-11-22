package omegaVPLinc.fixpoint.prefix;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;

import java.util.Set;

public class Prefixes {
    private final VPA a;
    private final VPA b;

    private final PrefixWVector W;
    private final PrefixCVector C;
    private final PrefixRVector R;
    private final UVector       U;

    public Prefixes(VPA a, VPA b) {
        this.a = a;
        this.b = b;
        this.W = new PrefixWVector(a, b);
        this.C = new PrefixCVector(a, b, W);
        this.R = new PrefixRVector(a, b, W);
        this.U = new UVector(a, b, C, R);
    }

    public int iterate() {
        Set<Pair<State, State>> frontier = a.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Set<Pair<State, State>> changedC = C.iterateOnce(frontier);
        Set<Pair<State, State>> changedR = R.iterateOnce(frontier);
        Set<Pair<State, State>> changedU = U.iterateOnce(frontier);

        W.updateCopy();
        U.updateCopy();

        Set<Pair<State, State>> frontierW = W.frontier();
        Set<Pair<State, State>> frontierC = C.frontier();
        Set<Pair<State, State>> frontierR = R.frontier();
        Set<Pair<State, State>> frontierU = U.frontier();
        int i = 0;
        while (!changedW.isEmpty()
                || !changedC.isEmpty()
                || !changedR.isEmpty()
                || !changedU.isEmpty()) {
            changedW = W.iterateOnce(frontierW);
            changedC = C.iterateOnce(frontierC);
            changedR = R.iterateOnce(frontierR);
            changedU = U.iterateOnce(frontierU);
            W.updateCopy();
            C.updateCopy();
            R.updateCopy();
            U.updateCopy();
            frontierW = W.frontier();
            frontierC = C.frontier();
            frontierR = R.frontier();
            frontierU = U.frontier();
            i++;
        }

        return i;
    }
}
