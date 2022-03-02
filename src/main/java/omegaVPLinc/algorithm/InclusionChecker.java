package omegaVPLinc.algorithm;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.period.Periods;
import omegaVPLinc.fixpoint.prefix.Prefixes;
import omegaVPLinc.utility.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InclusionChecker {
    private static final Logger logger = LoggerFactory.getLogger(InclusionChecker.class);
    private final VPA a;
    private final VPA b;

    private final Prefixes prefixes;
    private final Periods periods;

    public InclusionChecker(VPA a, VPA b) {
        this.a = a;
        this.b = b;
        this.prefixes = new Prefixes(a, b);
        this.periods = new Periods(a, b);
    }

    private boolean inctx(Map<State, Set<State>> x, Map<State, Set<State>> y1, Map<State, Set<State>> y2) {
        Map<State, Set<State>> y1closure = State.transitiveClosure(y1);
        Map<State, Set<State>> yComposition = State.composeM(y1closure, State.composeM(y2, y1closure));
        if (!x.containsKey(b.getInitialState()))
            return false;
        for (State q : x.get(b.getInitialState())) {
            for (State p : y1closure.getOrDefault(q, new HashSet<>())) {
                if (yComposition.containsKey(p) && yComposition.get(p).contains(p))
                    return true;
            }
        }
        return false;
    }

    public boolean checkInclusion() {
        int prefixIterations = prefixes.iterate();
        int periodIterations = periods.iterate();
        for (State p : a.getStates()) {
            for (Context x : prefixes.getFromC(a.getInitialState(), p)) {
                for (Context y1y2 : periods.getFromCS(p, p)) {
                    if (!inctx(x.getCtx(), y1y2.getCtx(), y1y2.getFinalCtx())) {
                        logger.debug("C, C_star");
                        logger.debug("Prefix: ({} -> {}) {}", a.getInitialState(), p, x.getWord());
                        logger.debug("Period: ({} -> {}) {}", p, p, y1y2.getWord());
                        return false;
                    }
                }
            }
            for (Context x : prefixes.getFromU(a.getInitialState(), p)) {
                for (Context y1y2 : periods.getFromRS(p, p)) {
                    if (!inctx(x.getCtx(), y1y2.getCtx(), y1y2.getFinalCtx())) {
                        logger.debug("U, R_star");
                        logger.debug("Prefix: ({} -> {}) {}", a.getInitialState(), p, x.getWord());
                        logger.debug("Period: ({} -> {}) {}", p, p, y1y2.getWord());
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
