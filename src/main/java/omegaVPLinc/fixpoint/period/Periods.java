package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Periods {
    private static final Logger logger = LoggerFactory.getLogger(Periods.class);
    private final VPA a;
    private final VPA b;

    private final PeriodWVector W;
    private final WStarVector WS;
    private final PeriodCVector C;
    private final CStarVector CS;
    private final PeriodRVector R;
    private final RStarVector RS;

    public Periods(VPA a, VPA b) {
        this.a = a;
        this.b = b;
        this.W = new PeriodWVector(a, b);
        this.WS = new WStarVector(a, b, W);
        this.C = new PeriodCVector(a, b, W);
        this.CS = new CStarVector(a, b, WS, C);
        this.R = new PeriodRVector(a, b, W);
        this.RS = new RStarVector(a, b, WS, R);
    }

    public int iterate() {
        logger.info("Starting Period iteration.");
        W.initial();
        WS.initial();
        C.initial();
        CS.initial();
        R.initial();
        RS.initial();
        logger.info("Iteration number 0 complete");
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
        WS.frontier();
        C.frontier();
        CS.frontier();
        R.frontier();
        RS.frontier();
        int i = 1;
        while (!W.getChanged().isEmpty()
                || !WS.getChanged().isEmpty()
                || !C.getChanged().isEmpty()
                || !CS.getChanged().isEmpty()
                || !R.getChanged().isEmpty()
                || !RS.getChanged().isEmpty()) {
            W.iterateOnce();
            WS.iterateOnce();
            C.iterateOnce();
            CS.iterateOnce();
            R.iterateOnce();
            RS.iterateOnce();
            logger.info("Iteration number {} complete.", i);
            W.updateCopy();
            W.updateInnerFrontier();
            WS.updateCopy();
            WS.updateInnerFrontier();
            C.updateCopy();
            C.updateInnerFrontier();
            CS.updateCopy();
            CS.updateInnerFrontier();
            R.updateCopy();
            R.updateInnerFrontier();
            RS.updateCopy();
            RS.updateInnerFrontier();
            W.frontier();
            WS.frontier();
            C.frontier();
            CS.frontier();
            R.frontier();
            RS.frontier();
            i++;
        }
        return i;
    }

    public Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> getFromCS(State p, State q) {
        return CS.getInnerVector().get(Pair.of(p, q));
    }

    public Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>> getFromRS(State p, State q) {
        return RS.getInnerVector().get(Pair.of(p, q));
    }

    public CStarVector getCS() {
        return CS;
    }

    public RStarVector getRS() {
        return RS;
    }
}
