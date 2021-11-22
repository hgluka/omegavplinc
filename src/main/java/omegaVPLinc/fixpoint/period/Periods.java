package omegaVPLinc.fixpoint.period;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.utility.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Periods {
    private final VPA a;
    private final VPA b;

    private final PeriodWVector W;
    private final FinalWVector FW;
    private final PeriodCVector C;
    private final FinalCVector FC;
    private final PeriodRVector R;
    private final FinalRVector FR;

    public Periods(VPA a, VPA b) {
        this.a = a;
        this.b = b;
        this.W = new PeriodWVector(a, b);
        this.FW = new FinalWVector(a, b, W);
        this.C = new PeriodCVector(a, b, W);
        this.FC = new FinalCVector(a, b, FW, C);
        this.R = new PeriodRVector(a, b, W);
        this.FR = new FinalRVector(a, b, FW, R);
    }

    public int iterate() {
        Set<Pair<State, State>> frontier = a.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Set<Pair<State, State>> changedFW = FW.iterateOnce(frontier);
        Set<Pair<State, State>> changedC = C.iterateOnce(frontier);
        Set<Pair<State, State>> changedFC = FC.iterateOnce(frontier);
        Set<Pair<State, State>> changedR = R.iterateOnce(frontier);
        Set<Pair<State, State>> changedFR = FR.iterateOnce(frontier);

        W.updateCopy();
        FW.updateCopy();
        C.updateCopy();
        FC.updateCopy();
        R.updateCopy();
        FR.updateCopy();

        Set<Pair<State, State>> frontierW = W.frontier();
        Set<Pair<State, State>> frontierFW = FW.frontier();
        Set<Pair<State, State>> frontierC = C.frontier();
        Set<Pair<State, State>> frontierFC = FC.frontier();
        Set<Pair<State, State>> frontierR = R.frontier();
        Set<Pair<State, State>> frontierFR = FR.frontier();
        int i = 0;
        while (!changedW.isEmpty()
                || !changedFW.isEmpty()
                || !changedC.isEmpty()
                || !changedFC.isEmpty()
                || !changedR.isEmpty()
                || !changedFR.isEmpty()) {
            changedW = W.iterateOnce(frontierW);
            changedFW = FW.iterateOnce(frontierFW);
            changedC = C.iterateOnce(frontierC);
            changedFC = FC.iterateOnce(frontierFC);
            changedR = R.iterateOnce(frontierR);
            changedFR = FR.iterateOnce(frontierFR);
            W.updateCopy();
            FW.updateCopy();
            C.updateCopy();
            FC.updateCopy();
            R.updateCopy();
            FR.updateCopy();
            frontierW = W.frontier();
            FW.frontier();
            frontierC = C.frontier();
            frontierFC = FC.frontier();
            frontierR = R.frontier();
            frontierFR = FR.frontier();
            i++;
        }
        return i;
    }
}
