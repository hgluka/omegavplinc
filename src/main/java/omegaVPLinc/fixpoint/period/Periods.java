package omegaVPLinc.fixpoint.period;

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

public class Periods {
    private static final Logger logger = LoggerFactory.getLogger(Periods.class);
    private final VPA a;
    private final VPA b;

    private final WVector W;
    private final WStarVector WS;
    private final CVector C;
    private final CStarVector CS;
    private final RVector R;
    private final RStarVector RS;

    public Periods(VPA a, VPA b, boolean withWords) {
        this.a = a;
        this.b = b;
        this.W = new WVector(a, b, true, withWords);
        this.WS = new WStarVector(a, b, withWords, W);
        this.C = new CVector(a, b, true, withWords, W);
        this.CS = new CStarVector(a, b, withWords, WS, C);
        this.R = new RVector(a, b, true, withWords, W);
        this.RS = new RStarVector(a, b, withWords, WS, R);
    }

    public int iterate() {
        logger.info("Starting Period iteration.");
        W.initial();
        WS.initial();
        C.initial();
        CS.initial();
        R.initial();
        RS.initial();
        W.updateCopy();
        WS.updateCopy();
        C.updateCopy();
        CS.updateCopy();
        R.updateCopy();
        RS.updateCopy();
        W.updateInnerFrontier();
        WS.updateInnerFrontier();
        C.updateInnerFrontier();
        CS.updateInnerFrontier();
        R.updateInnerFrontier();
        RS.updateInnerFrontier();

        W.frontier();
        int w_iters = W.computeFixpoint();
        logger.info("W fixpoint complete after {} iterations.", w_iters);
        WS.frontier();
        int ws_iters = WS.computeFixpoint();
        logger.info("WS fixpoint complete after {} iterations.", ws_iters);
        C.frontier();
        int c_iters = C.computeFixpoint();
        logger.info("C fixpoint complete after {} iterations.", c_iters);
        CS.frontier();
        int cs_iters = CS.computeFixpoint();
        logger.info("CS fixpoint complete after {} iterations.", cs_iters);
        R.frontier();
        int r_iters = R.computeFixpoint();
        logger.info("R fixpoint complete after {} iterations.", r_iters);
        RS.frontier();
        int rs_iters = RS.computeFixpoint();
        logger.info("RS fixpoint complete after {} iterations.", rs_iters);
        return w_iters + ws_iters + c_iters + cs_iters + r_iters + rs_iters;
    }

    public Set<Context> getFromCS(State p, State q) {
        return CS.getInnerVector().get(Pair.of(p, q));
    }

    public Set<Context> getFromRS(State p, State q) {
        return RS.getInnerVector().get(Pair.of(p, q));
    }

    public CStarVector getCS() {
        return CS;
    }

    public RStarVector getRS() {
        return RS;
    }
}
