package omegaVPLinc.fixpoint.prefix;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Prefixes {
    private static final Logger logger = LoggerFactory.getLogger(Prefixes.class);

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
        logger.info("Starting Prefix iteration.");
        Set<Pair<State, State>> changedW = new HashSet<>(Set.of(Pair.of(null, null)));
        Set<Pair<State, State>> changedC = new HashSet<>(Set.of(Pair.of(null, null)));
        Set<Pair<State, State>> changedR = new HashSet<>(Set.of(Pair.of(null, null)));
        Set<Pair<State, State>> changedU = new HashSet<>(Set.of(Pair.of(null, null)));

        Set<Pair<State, State>> frontierW = new HashSet<>();
        for (State p : a.getStates()) {
            frontierW.add(Pair.of(p, p));
        }
        Set<Pair<State, State>> frontierC = new HashSet<>();
        Set<Pair<State, State>> frontierR = new HashSet<>();
        Set<Pair<State, State>> frontierU = new HashSet<>();
        for (State p : a.getStates()) {
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (State q : p.getCallSuccessors(c)) {
                    frontierU.add(Pair.of(p, q));
                }
            }
        }
        int i = 0;
        while (!changedW.isEmpty()
                || !changedC.isEmpty()
                || !changedR.isEmpty()
                || !changedU.isEmpty()) {
            changedW = W.iterateOnce(frontierW);
            changedC = C.iterateOnce(frontierC);
            changedR = R.iterateOnce(frontierR);
            changedU = U.iterateOnce(frontierU);
            logger.info("Iteration number {} complete", i);
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

    public Set<Map<State, Set<State>>> getFromC(State p, State q) {
        return C.getInnerVector().get(Pair.of(p, q));
    }

    public Set<Map<State, Set<State>>> getFromU(State p, State q) {
        return U.getInnerVector().get(Pair.of(p, q));
    }

    public PrefixCVector getC() {
        return C;
    }

    public UVector getU() {
        return U;
    }
}
