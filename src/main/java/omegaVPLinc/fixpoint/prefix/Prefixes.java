package omegaVPLinc.fixpoint.prefix;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.common.CVector;
import omegaVPLinc.fixpoint.common.RVector;
import omegaVPLinc.fixpoint.common.WVector;
import omegaVPLinc.utility.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class Prefixes {
    private static final Logger logger = LoggerFactory.getLogger(Prefixes.class);

    private final VPA a;
    private final VPA b;

    private final WVector W;
    private final CVector C;
    private final RVector R;
    private final UVector       U;

    public Prefixes(VPA a, VPA b) {
        this.a = a;
        this.b = b;
        this.W = new WVector(a, b, false);
        this.C = new CVector(a, b, false, W);
        this.R = new RVector(a, b, false, W);
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
        logger.info("W fixpoint complete after {} iterations.", w_iters);
        C.frontier();
        int c_iters = C.computeFixpoint();
        logger.info("C fixpoint complete after {} iterations.", c_iters);
        R.frontier();
        int r_iters = R.computeFixpoint();
        logger.info("R fixpoint complete after {} iterations.", r_iters);
        U.frontier();
        int u_iters = U.computeFixpoint();
        logger.info("U fixpoint complete after {} iterations.", u_iters);

        return w_iters + c_iters + r_iters + u_iters;
    }

    public Set<Context> getFromC(State p, State q) {
        return C.getInnerVector().get(Pair.of(p, q));
    }

    public Set<Context> getFromU(State p, State q) {
        return U.getInnerVector().get(Pair.of(p, q));
    }

    public CVector getC() {
        return C;
    }

    public UVector getU() {
        return U;
    }
}
