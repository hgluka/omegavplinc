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
        W.initial();
        C.initial();
        R.initial();
        U.initial();
        // logger.info("Iteration number 0 complete");
        W.updateCopy();
        C.updateCopy();
        R.updateCopy();
        U.updateCopy();
        W.updateInnerFrontier();
        C.updateInnerFrontier();
        R.updateInnerFrontier();
        U.updateInnerFrontier();
        W.frontier();
        C.frontier();
        R.frontier();
        U.frontier();
        int i = 1;
        while (!W.getChanged().isEmpty()
                || !C.getChanged().isEmpty()
                || !R.getChanged().isEmpty()) {
            W.iterateOnce();
            C.iterateOnce();
            R.iterateOnce();
            // logger.info("Iteration number {} complete", i);
            W.updateCopy();
            W.updateInnerFrontier();
            C.updateCopy();
            C.updateInnerFrontier();
            R.updateCopy();
            R.updateInnerFrontier();
            W.frontier();
            C.frontier();
            R.frontier();
            i++;
        }
        while (!U.getChanged().isEmpty()) {
            U.iterateOnce();
            U.updateCopy();
            U.updateInnerFrontier();
            U.frontier();
        }
        logger.info("Prefix fixpoint complete after {} iterations.", i);
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
