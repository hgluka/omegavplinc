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
        int w_iters = W.computeFixpoint();
        C.frontier();
        int c_iters = C.computeFixpoint();
        R.frontier();
        int r_iters = R.computeFixpoint();
        U.frontier();
        int u_iters = U.computeFixpoint();

        logger.info("W fixpoint complete after {} iterations.", w_iters);
        logger.info("C fixpoint complete after {} iterations.", c_iters);
        logger.info("R fixpoint complete after {} iterations.", r_iters);
        logger.info("U fixpoint complete after {} iterations.", u_iters);
        return w_iters + c_iters + r_iters + u_iters;
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
