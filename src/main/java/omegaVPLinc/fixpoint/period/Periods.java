package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Periods {
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
        Set<Pair<State, State>> frontier = a.getAllStatePairs();

        Set<Pair<State, State>> changedW = new HashSet<>(Set.of(Pair.of(null, null)));
        Set<Pair<State, State>> changedWS = new HashSet<>(Set.of(Pair.of(null, null)));
        Set<Pair<State, State>> changedC = new HashSet<>(Set.of(Pair.of(null, null)));
        Set<Pair<State, State>> changedCS = new HashSet<>(Set.of(Pair.of(null, null)));
        Set<Pair<State, State>> changedR = new HashSet<>(Set.of(Pair.of(null, null)));
        Set<Pair<State, State>> changedRS = new HashSet<>(Set.of(Pair.of(null, null)));

        Set<Pair<State, State>> frontierW = new HashSet<>();
        for (State p : a.getStates()) {
            frontierW.add(Pair.of(p, p));
        }
        Set<Pair<State, State>> frontierWS = new HashSet<>();
        Set<Pair<State, State>> frontierC = new HashSet<>();
        Set<Pair<State, State>> frontierCS = new HashSet<>();
        Set<Pair<State, State>> frontierR = new HashSet<>();
        Set<Pair<State, State>> frontierRS = new HashSet<>();
        int i = 0;
        while (!changedW.isEmpty()
                || !changedWS.isEmpty()
                || !changedC.isEmpty()
                || !changedCS.isEmpty()
                || !changedR.isEmpty()
                || !changedRS.isEmpty()) {
            changedW = W.iterateOnce(frontierW);
            changedWS = WS.iterateOnce(frontierWS);
            changedC = C.iterateOnce(frontierC);
            changedCS = CS.iterateOnce(frontierCS);
            changedR = R.iterateOnce(frontierR);
            changedRS = RS.iterateOnce(frontierRS);
            W.updateCopy();
            WS.updateCopy();
            C.updateCopy();
            CS.updateCopy();
            R.updateCopy();
            RS.updateCopy();
            frontierW = W.frontier();
            frontierWS = WS.frontier();
            frontierC = C.frontier();
            frontierCS = CS.frontier();
            frontierR = R.frontier();
            frontierRS = RS.frontier();
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
}
