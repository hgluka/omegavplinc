package omegaVPLinc.algorithm;

import omegaVPLinc.automaton.Context;
import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.fixpoint.period.Periods;
import omegaVPLinc.fixpoint.prefix.Prefixes;
import omegaVPLinc.utility.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class InclusionChecker {
    private static final Logger logger = LoggerFactory.getLogger(InclusionChecker.class);
    private final VPA a;
    private final VPA b;

    private final Prefixes prefixes;
    private final Periods periods;

    private boolean withWords;

    public InclusionChecker(VPA a, VPA b, boolean withWords) {
        this.a = a;
        this.b = b;
        this.prefixes = new Prefixes(a, b, withWords);
        this.periods = new Periods(a, b, withWords);
        this.withWords = withWords;

    }

    private boolean inctx(Map<State, Set<State>> x, Map<State, Set<State>> y1, Map<State, Set<State>> y2) {
        Map<State, Set<State>> y1closure = Context.transitiveClosure(y1);
        Map<State, Set<State>> yComposition = Context.composeM(y1closure, Context.composeM(y2, y1closure));
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

    private String getLassoWord(LinkedList<Symbol> stem, LinkedList<Symbol> loop) {
        return "[" +
                wordToString(stem) +
                ", " +
                wordToString(loop) +
                "]";
    }

    private String wordToString(LinkedList<Symbol> loop) {
        StringBuilder sb = new StringBuilder();
        for (Symbol s : loop) {
            switch (s.getType()) {
                case CALL -> {
                    sb.append(s);
                    sb.append("<");
                }
                case INTERNAL -> sb.append(s);
                case RETURN -> {
                    sb.append(">");
                    sb.append(s);
                }
            }
            sb.append(" ");
        }
        sb.deleteCharAt(sb.lastIndexOf(" "));
        return sb.toString();
    }

    public boolean checkInclusion() {
        int prefixIterations = prefixes.iterate();
        int periodIterations = periods.iterate();
        for (State p : a.getStates()) {
            for (Context x : prefixes.getFromC(a.getInitialState(), p)) {
                for (Context y1y2 : periods.getFromCS(p, p)) {
                    if (!inctx(x.getCtx(), y1y2.getCtx(), y1y2.getFinalCtx())) {
                        logger.debug("C, C_star");
                        if (withWords) {
                            // logger.debug("Prefix word: ({} -> {}) {}", a.getInitialState(), p, x.getWord());
                            // logger.debug("Period word: ({} -> {}) {}", p, p, y1y2.getWord());
                            logger.debug("Lasso word: {}", getLassoWord(x.getWord(), y1y2.getWord()));
                        } else {
                            logger.debug("Prefix context: ({} -> {}) {}", a.getInitialState(), p, x.getCtx());
                            logger.debug("Period context: ({} -> {}) {}", p, p, y1y2.getCtx());
                            logger.debug("Period final context: ({} -> {}) {}", p, p, y1y2.getFinalCtx());
                        }
                        return false;
                    }
                }
            }
            for (Context x : prefixes.getFromU(a.getInitialState(), p)) {
                for (Context y1y2 : periods.getFromRS(p, p)) {
                    if (!inctx(x.getCtx(), y1y2.getCtx(), y1y2.getFinalCtx())) {
                        logger.debug("U, R_star");
                        if (withWords) {
                            // logger.debug("Prefix word: ({} -> {}) {}", a.getInitialState(), p, x.getWord());
                            // logger.debug("Period word: ({} -> {}) {}", p, p, y1y2.getWord());
                            logger.debug("Lasso word: {}", getLassoWord(x.getWord(), y1y2.getWord()));
                        } else {
                            logger.debug("Prefix context: ({} -> {}) {}", a.getInitialState(), p, x.getCtx());
                            logger.debug("Period context: ({} -> {}) {}", p, p, y1y2.getCtx());
                            logger.debug("Period final context: ({} -> {}) {}", p, p, y1y2.getFinalCtx());
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
