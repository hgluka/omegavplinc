package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.automaton.VPABuilder;
import omegaVPLinc.fixpoint.period.*;
import omegaVPLinc.utility.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PeriodVectorsTest {
    private VPA vpa;

    @BeforeEach
    void setUp() {
        VPABuilder vpaBuilder = new VPABuilder();
        Set<String> callStrings = Set.of("c0", "c1");
        Set<String> internalStrings = Set.of("a0", "a1", "a2", "a3", "a4", "a5", "a6");
        Set<String> returnStrings = Set.of("r0", "r1");
        Set<Symbol> ca = Symbol.createAlphabet("CALL", callStrings);
        Set<Symbol> ia = Symbol.createAlphabet(
                "INTERNAL",
                internalStrings
        );
        Set<Symbol> ra = Symbol.createAlphabet("RETURN", returnStrings);

        Map<String, Symbol> alphabetMap = new HashMap<>();
        for (String c : callStrings) {
            alphabetMap.put(c, ca.stream().filter(s -> s.getSymbol().equals(c)).findFirst().get());
        }
        for (String a : internalStrings) {
            alphabetMap.put(a, ia.stream().filter(s -> s.getSymbol().equals(a)).findFirst().get());
        }
        for (String r : returnStrings) {
            alphabetMap.put(r, ra.stream().filter(s -> s.getSymbol().equals(r)).findFirst().get());
        }


        Set<String> sa = Set.of("s0", "s1", "s2", "s3", "s4", "s5", "s6");

        Set<State> states = new HashSet<>();
        Map<String, State> stateMap = new HashMap<>();
        for (String ssymbol : sa) {
            State state = new State(ssymbol);
            state.setFinal(true);
            states.add(state);
            stateMap.put(ssymbol, state);
        }

        State s1 = stateMap.get("s1");

        stateMap.get("s0").addCallSuccessor(alphabetMap.get("c1"), "s0", stateMap.get("s1"));
        stateMap.get("s1").addCallPredecessor(alphabetMap.get("c1"), "s0", stateMap.get("s0"));
        stateMap.get("s2").addCallSuccessor(alphabetMap.get("c0"), "s2", stateMap.get("s1"));
        stateMap.get("s1").addCallPredecessor(alphabetMap.get("c0"), "s2", stateMap.get("s2"));

        stateMap.get("s1").addInternalSuccessor(alphabetMap.get("a6"), stateMap.get("s4"));
        stateMap.get("s4").addInternalPredecessor(alphabetMap.get("a6"), stateMap.get("s1"));
        stateMap.get("s1").addInternalSuccessor(alphabetMap.get("a3"), stateMap.get("s0"));
        stateMap.get("s0").addInternalPredecessor(alphabetMap.get("a3"), stateMap.get("s1"));
        stateMap.get("s4").addInternalSuccessor(alphabetMap.get("a2"), stateMap.get("s5"));
        stateMap.get("s5").addInternalPredecessor(alphabetMap.get("a2"), stateMap.get("s4"));
        stateMap.get("s4").addInternalSuccessor(alphabetMap.get("a4"), stateMap.get("s6"));
        stateMap.get("s6").addInternalSuccessor(alphabetMap.get("a4"), stateMap.get("s4"));
        stateMap.get("s6").addInternalSuccessor(alphabetMap.get("a5"), stateMap.get("s3"));
        stateMap.get("s3").addInternalSuccessor(alphabetMap.get("a5"), stateMap.get("s6"));

        stateMap.get("s3").addReturnSuccessor(alphabetMap.get("r0"), "s0", stateMap.get("s2"));
        stateMap.get("s2").addReturnPredecessor(alphabetMap.get("r0"), "s0", stateMap.get("s3"));
        stateMap.get("s3").addReturnSuccessor(alphabetMap.get("r1"), "s2", stateMap.get("s4"));
        stateMap.get("s4").addReturnPredecessor(alphabetMap.get("r1"), "s2", stateMap.get("s3"));

        this.vpa = vpaBuilder.callAlphabet(ca)
                .internalAlphabet(ia)
                .returnAlphabet(ra)
                .stackAlphabet(sa)
                .states(states)
                .initialState(s1)
                .build();
    }

    @Test
    void testIterateOnceW() {
        PeriodWVector w1 = new PeriodWVector(vpa, vpa);
        PeriodWVector w2 = new PeriodWVector(vpa, vpa);
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> oldInnerW1 = w1.deepCopy();
        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changed1 = w1.iterateOnce(frontier);
        w1.updateCopy();
        assertEquals(7, changed1.size());
        assertNotEquals(oldInnerW1, w1.getInnerVector());

        Set<Pair<State, State>> changed2 = w2.iterateOnce(frontier);
        w2.updateCopy();
        assertEquals(w1.getInnerVector(), w2.getInnerVector());

        oldInnerW1 = w1.deepCopy();
        Set<Pair<State, State>> changed12 = w1.iterateOnce(frontier);
        w1.updateCopy();

        System.out.println("CHANGED SIZE: " + changed2.size());
        System.out.println("FRONTIER SIZE: " + w2.frontier().size());

        Set<Pair<State, State>> changed22 =
                w2.iterateOnce(w2.frontier());
        w2.updateCopy();

        assertEquals(changed12, changed22);
        assertEquals(w1, w2);

        System.out.println("CHANGED SIZE: " + changed22.size());
        System.out.println("FRONTIER SIZE: " + w2.frontier().size());

        assertNotEquals(oldInnerW1, w1.getInnerVector());
    }

    @Test
    void testIterateW() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);

        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changed = W.iterateOnce(frontier);
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> Wcopy2 = new HashMap<>();
        W.updateCopy();

        // TODO: Use a logging library
        System.out.println("======================");
        System.out.println("Iterate Period W only");
        System.out.println("======================");
        int i = 0;
        while (!changed.isEmpty()) {
            frontier = W.frontier();
            //frontier = vpa.getAllStatePairs();
            System.out.println("CHANGED SIZE: " + changed.size());
            System.out.println("FRONTIER SIZE: " + frontier.size());
            Wcopy2 = W.deepCopy();
            changed = W.iterateOnce(frontier);
            W.updateCopy();
            i++;
        }

        assertEquals(Wcopy2, W.getInnerVector());

        System.out.println("Number of iterations: " + i);
        assertEquals(7, i);
    }

    @Test
    void testIterateOnceC() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);
        PeriodCVector C = new PeriodCVector(vpa, vpa, W);
        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Set<Pair<State, State>> changedC = C.iterateOnce(frontier);
        assertEquals(0, changedC.size());
        W.updateCopy();
        C.updateCopy();

        changedW = W.iterateOnce(W.frontier());
        changedC = C.iterateOnce(frontier);
        assertNotEquals(0, changedC.size());
        W.updateCopy();
        C.updateCopy();

        // System.out.println(C);
        assertEquals(C.getInnerVector(), C.getInnerVectorCopy());
    }

    @Test
    void testIterateC() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);
        PeriodCVector C = new PeriodCVector(vpa, vpa, W);

        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyW = new HashMap<>();
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyC = new HashMap<>();
        W.updateCopy();

        // TODO: Use a logging library
        System.out.println("======================");
        System.out.println("Iterate Period W and C");
        System.out.println("======================");
        Set<Pair<State, State>> frontierW = W.frontier();
        Set<Pair<State, State>> frontierC = frontier;
        Set<Pair<State, State>> changedC = new HashSet<>();
        int i = 0;
        while (!changedW.isEmpty() || (!changedC.isEmpty() || i == 0)) {
            System.out.println("W CHANGED SIZE: " + changedW.size());
            System.out.println("W FRONTIER SIZE: " + frontierW.size());
            System.out.println("C CHANGED SIZE: " + changedC.size());
            System.out.println("C FRONTIER SIZE: " + frontierC.size());
            changedW = W.iterateOnce(frontierW);
            changedC = C.iterateOnce(frontierC);
            W.updateCopy();
            C.updateCopy();
            frontierW = W.frontier();
            frontierC = C.frontier();
            copyW = W.deepCopy();
            copyC = C.deepCopy();
            i++;
        }

        assertEquals(copyW, W.getInnerVector());
        assertEquals(copyC, C.getInnerVector());

        System.out.println("Number of iterations: " + i);
        assertEquals(8, i);
    }

    @Test
    void testIterateOnceR() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);
        PeriodRVector R = new PeriodRVector(vpa, vpa, W);
        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Set<Pair<State, State>> changedR = R.iterateOnce(frontier);
        assertEquals(0, changedR.size());
        W.updateCopy();
        R.updateCopy();

        changedW = W.iterateOnce(W.frontier());
        changedR = R.iterateOnce(frontier);
        assertNotEquals(0, changedR.size());
        W.updateCopy();
        R.updateCopy();

        assertEquals(R.getInnerVector(), R.getInnerVectorCopy());
    }

    @Test
    void testIterateR() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);
        PeriodRVector R = new PeriodRVector(vpa, vpa, W);

        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyW = new HashMap<>();
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyR = new HashMap<>();
        W.updateCopy();

        // TODO: Use a logging library
        System.out.println("======================");
        System.out.println("Iterate Period W and R");
        System.out.println("======================");
        Set<Pair<State, State>> frontierW = W.frontier();
        Set<Pair<State, State>> frontierR = frontier;
        Set<Pair<State, State>> changedR = new HashSet<>();
        int i = 0;
        while (!changedW.isEmpty() || (!changedR.isEmpty() || i == 0)) {
            System.out.println("W CHANGED SIZE: " + changedW.size());
            System.out.println("W FRONTIER SIZE: " + frontierW.size());
            System.out.println("R CHANGED SIZE: " + changedR.size());
            System.out.println("R FRONTIER SIZE: " + frontierR.size());
            changedW = W.iterateOnce(frontierW);
            changedR = R.iterateOnce(frontierR);
            W.updateCopy();
            R.updateCopy();
            frontierW = W.frontier();
            frontierR = R.frontier();
            copyW = W.deepCopy();
            copyR = R.deepCopy();
            i++;
        }

        assertEquals(copyW, W.getInnerVector());
        assertEquals(copyR, R.getInnerVector());

        System.out.println("Number of iterations: " + i);
        assertEquals(7, i);
    }

    @Test
    void testIterateOnceFinalW() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);
        WStarVector WS = new WStarVector(vpa, vpa, W);
        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Set<Pair<State, State>> changedR = WS.iterateOnce(frontier);
        assertEquals(0, changedR.size());
        W.updateCopy();
        WS.updateCopy();

        changedW = W.iterateOnce(W.frontier());
        changedR = WS.iterateOnce(frontier);
        assertNotEquals(0, changedR.size());
        W.updateCopy();
        WS.updateCopy();

        assertEquals(WS.getInnerVector(), WS.getInnerVectorCopy());
    }

    @Test
    void testIterateFinalW() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);
        WStarVector WS = new WStarVector(vpa, vpa, W);

        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyW = new HashMap<>();
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyWS = new HashMap<>();
        W.updateCopy();

        // TODO: Use a logging library
        System.out.println("======================");
        System.out.println("Iterate Period W and WS");
        System.out.println("======================");
        Set<Pair<State, State>> frontierW = W.frontier();
        Set<Pair<State, State>> frontierWS = frontier;
        Set<Pair<State, State>> changedWS = new HashSet<>();
        int i = 0;
        while (!changedW.isEmpty() || !changedWS.isEmpty()) {
            System.out.println("W CHANGED SIZE: " + changedW.size());
            System.out.println("W FRONTIER SIZE: " + frontierW.size());
            System.out.println("WS CHANGED SIZE: " + changedWS.size());
            System.out.println("WS FRONTIER SIZE: " + frontierWS.size());
            changedW = W.iterateOnce(frontierW);
            changedWS = WS.iterateOnce(frontierWS);
            W.updateCopy();
            WS.updateCopy();
            frontierW = W.frontier();
            frontierWS = WS.frontier();
            copyW = W.deepCopy();
            copyWS = WS.deepCopy();
            i++;
        }

        assertEquals(copyW, W.getInnerVector());
        assertEquals(copyWS, WS.getInnerVector());

        assertNotEquals(WS, W);
        System.out.println("Number of iterations: " + i);
        assertEquals(7, i);
    }

    @Test
    void testIterateOnceFinalC() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);
        WStarVector WS = new WStarVector(vpa, vpa, W);
        PeriodCVector C = new PeriodCVector(vpa, vpa, W);
        CStarVector CS = new CStarVector(vpa, vpa, WS, C);
        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Set<Pair<State, State>> changedWS = WS.iterateOnce(frontier);
        Set<Pair<State, State>> changedC = C.iterateOnce(frontier);
        Set<Pair<State, State>> changedCS = CS.iterateOnce(frontier);
        assertEquals(0, changedCS.size());
        W.updateCopy();
        WS.updateCopy();
        C.updateCopy();
        CS.updateCopy();

        changedW = W.iterateOnce(W.frontier());
        changedWS = WS.iterateOnce(frontier);
        changedC = C.iterateOnce(frontier);
        changedCS = CS.iterateOnce(CS.frontier());
        assertEquals(0, changedCS.size());
        W.updateCopy();
        WS.updateCopy();
        C.updateCopy();
        CS.updateCopy();

        assertEquals(CS.getInnerVector(), CS.getInnerVectorCopy());
    }

    @Test
    void testIterateFinalC() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);
        WStarVector WS = new WStarVector(vpa, vpa, W);
        PeriodCVector C = new PeriodCVector(vpa, vpa, W);
        CStarVector CS = new CStarVector(vpa, vpa, WS, C);

        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyW = new HashMap<>();
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyWS = new HashMap<>();
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyC = new HashMap<>();
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyCS = new HashMap<>();

        W.updateCopy();

        // TODO: Use a logging library
        System.out.println("======================");
        System.out.println("Iterate W, WS, C and CS");
        System.out.println("======================");
        Set<Pair<State, State>> frontierW = W.frontier();
        Set<Pair<State, State>> frontierWS = frontier;
        Set<Pair<State, State>> frontierC = frontier;
        Set<Pair<State, State>> frontierCS = frontier;
        Set<Pair<State, State>> changedWS = new HashSet<>();
        Set<Pair<State, State>> changedC = new HashSet<>();
        Set<Pair<State, State>> changedCS = new HashSet<>();
        int i = 0;
        while (!changedW.isEmpty()
                || !changedWS.isEmpty()
                || !changedC.isEmpty()
                || !changedCS.isEmpty()) {
            System.out.println("-------------------");
            System.out.println("W CHANGED SIZE: " + changedW.size());
            System.out.println("W FRONTIER SIZE: " + frontierW.size());
            System.out.println("WS CHANGED SIZE: " + changedWS.size());
            System.out.println("WS FRONTIER SIZE: " + frontierWS.size());
            System.out.println("C CHANGED SIZE: " + changedC.size());
            System.out.println("C FRONTIER SIZE: " + frontierC.size());
            System.out.println("CS CHANGED SIZE: " + changedCS.size());
            System.out.println("CS FRONTIER SIZE: " + frontierCS.size());
            changedW = W.iterateOnce(frontierW);
            changedWS = WS.iterateOnce(frontierWS);
            changedC = C.iterateOnce(frontierC);
            changedCS = CS.iterateOnce(frontierCS);
            W.updateCopy();
            WS.updateCopy();
            C.updateCopy();
            CS.updateCopy();
            frontierW = W.frontier();
            frontierWS = WS.frontier();
            frontierC = C.frontier();
            frontierCS = CS.frontier();
            copyW = W.deepCopy();
            copyWS = WS.deepCopy();
            copyC = C.deepCopy();
            copyCS = CS.deepCopy();
            i++;
        }

        assertEquals(copyW, W.getInnerVector());
        assertEquals(copyWS, WS.getInnerVector());
        assertEquals(copyC, C.getInnerVector());
        assertEquals(copyCS, CS.getInnerVector());

        System.out.println("Number of iterations: " + i);
        assertEquals(8, i);
    }

    @Test
    void testIterateOnceFinalR() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);
        WStarVector WS = new WStarVector(vpa, vpa, W);
        PeriodRVector R = new PeriodRVector(vpa, vpa, W);
        RStarVector RS = new RStarVector(vpa, vpa, WS, R);
        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Set<Pair<State, State>> changedWS = WS.iterateOnce(frontier);
        Set<Pair<State, State>> changedC = R.iterateOnce(frontier);
        Set<Pair<State, State>> changedRS = RS.iterateOnce(frontier);
        assertEquals(0, changedRS.size());
        W.updateCopy();
        WS.updateCopy();
        R.updateCopy();
        RS.updateCopy();

        changedW = W.iterateOnce(W.frontier());
        changedWS = WS.iterateOnce(frontier);
        changedC = R.iterateOnce(frontier);
        changedRS = RS.iterateOnce(RS.frontier());
        assertEquals(0, changedRS.size());
        W.updateCopy();
        WS.updateCopy();
        R.updateCopy();
        RS.updateCopy();

        assertEquals(RS.getInnerVector(), RS.getInnerVectorCopy());
    }

    @Test
    void testIterateFinalR() {
        PeriodWVector W = new PeriodWVector(vpa, vpa);
        WStarVector WS = new WStarVector(vpa, vpa, W);
        PeriodRVector R = new PeriodRVector(vpa, vpa, W);
        RStarVector RS = new RStarVector(vpa, vpa, WS, R);

        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyW = new HashMap<>();
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyWS = new HashMap<>();
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyR = new HashMap<>();
        Map<Pair<State, State>, Set<Pair<Map<State, Set<State>>, Map<State, Set<State>>>>> copyRS = new HashMap<>();

        W.updateCopy();

        // TODO: Use a logging library
        System.out.println("======================");
        System.out.println("Iterate W, WS, R and RS");
        System.out.println("======================");
        Set<Pair<State, State>> frontierW = W.frontier();
        Set<Pair<State, State>> frontierWS = WS.frontier();
        Set<Pair<State, State>> frontierR = R.frontier();
        Set<Pair<State, State>> frontierRS = RS.frontier();
        Set<Pair<State, State>> changedWS = new HashSet<>();
        Set<Pair<State, State>> changedR = new HashSet<>();
        Set<Pair<State, State>> changedRS = new HashSet<>();
        int i = 0;
        while (!changedW.isEmpty()
                || !changedWS.isEmpty()
                || !changedR.isEmpty()
                || !changedRS.isEmpty()) {
            System.out.println("-------------------");
            System.out.println("W CHANGED SIZE: " + changedW.size());
            System.out.println("W FRONTIER SIZE: " + frontierW.size());
            System.out.println("WS CHANGED SIZE: " + changedWS.size());
            System.out.println("WS FRONTIER SIZE: " + frontierWS.size());
            System.out.println("R CHANGED SIZE: " + changedR.size());
            System.out.println("R FRONTIER SIZE: " + frontierR.size());
            System.out.println("RS CHANGED SIZE: " + changedRS.size());
            System.out.println("RS FRONTIER SIZE: " + frontierRS.size());
            changedW = W.iterateOnce(frontierW);
            changedWS = WS.iterateOnce(frontierWS);
            changedR = R.iterateOnce(frontierR);
            changedRS = RS.iterateOnce(frontierRS);
            W.updateCopy();
            WS.updateCopy();
            R.updateCopy();
            RS.updateCopy();
            frontierW = W.frontier();
            frontierWS = WS.frontier();
            frontierR = R.frontier();
            frontierRS = RS.frontier();
            copyW = W.deepCopy();
            copyWS = WS.deepCopy();
            copyR = R.deepCopy();
            copyRS = RS.deepCopy();
            i++;
        }

        assertEquals(copyW, W.getInnerVector());
        assertEquals(copyWS, WS.getInnerVector());
        assertEquals(copyR, R.getInnerVector());
        assertEquals(copyRS, RS.getInnerVector());

        System.out.println("Number of iterations: " + i);
        assertEquals(7, i);
    }
}