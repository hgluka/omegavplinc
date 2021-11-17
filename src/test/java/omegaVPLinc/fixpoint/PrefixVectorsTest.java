package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.automaton.VPABuilder;
import omegaVPLinc.utility.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PrefixVectorsTest {
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
        WVector<Map<State, Set<State>>> w1 = new PrefixWVector(vpa, vpa);
        WVector<Map<State, Set<State>>> w2 = new PrefixWVector(vpa, vpa);
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> oldInnerW1copy = w1.deepCopy();
        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();
        /*
        for (State p : vpa.getStates()) {
            for (State q : vpa.getStates()) {
                frontier.add(Pair.of(p, q));
            }
        }
        */
        Set<Pair<State, State>> changed1 = w1.iterateOnce(frontier);
        w1.updateCopy();
        assertEquals(7, changed1.size());
        assertNotEquals(oldInnerW1copy, w1.getInnerVector());

        Set<Pair<State, State>> changed2 = w2.iterateOnce(frontier);
        w2.updateCopy();
        assertEquals(w1.getInnerVector(), w2.getInnerVector());

        Map<Pair<State, State>, Set<Map<State, Set<State>>>> oldInnerW1 = w1.deepCopy();
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

        /*
        For each of the pairs pq of states changed in the last iteration, we add
        pairs starting with p and ending with a successor of p and
        pairs ending with q and starting with a predecessor of q.
         */
        // System.out.print(w1);
        assertNotEquals(oldInnerW1, w1.getInnerVector());
    }

    @Test
    void testIterateW() {
        WVector<Map<State, Set<State>>> W = new PrefixWVector(vpa, vpa);

        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changed = W.iterateOnce(frontier);
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> Wcopy2 = new HashMap<>();
        W.updateCopy();

        // TODO: Use a logging library
        System.out.println("======================");
        System.out.println("Iterate W only");
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
        WVector<Map<State, Set<State>>> W = new PrefixWVector(vpa, vpa);
        CVector<Map<State, Set<State>>> C = new PrefixCVector(vpa, vpa, W);
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
        WVector<Map<State, Set<State>>> W = new PrefixWVector(vpa, vpa);
        CVector<Map<State, Set<State>>> C = new PrefixCVector(vpa, vpa, W);

        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> copyW = new HashMap<>();
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> copyC = new HashMap<>();
        W.updateCopy();

        // TODO: Use a logging library
        System.out.println("======================");
        System.out.println("Iterate W and C");
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
        WVector<Map<State, Set<State>>> W = new PrefixWVector(vpa, vpa);
        RVector<Map<State, Set<State>>> R = new PrefixRVector(vpa, vpa, W);
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
        WVector<Map<State, Set<State>>> W = new PrefixWVector(vpa, vpa);
        RVector<Map<State, Set<State>>> R = new PrefixRVector(vpa, vpa, W);

        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> copyW = new HashMap<>();
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> copyR = new HashMap<>();
        W.updateCopy();

        // TODO: Use a logging library
        System.out.println("======================");
        System.out.println("Iterate W and R");
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
    void testIterateOnceU() {
        WVector<Map<State, Set<State>>> W = new PrefixWVector(vpa, vpa);
        CVector<Map<State, Set<State>>> C = new PrefixCVector(vpa, vpa, W);
        RVector<Map<State, Set<State>>> R = new PrefixRVector(vpa, vpa, W);
        UVector U = new UVector(vpa, vpa, C, R);
        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Set<Pair<State, State>> changedC = C.iterateOnce(frontier);
        Set<Pair<State, State>> changedR = R.iterateOnce(frontier);
        Set<Pair<State, State>> changedU = U.iterateOnce(frontier);
        assertNotEquals(0, changedU.size());
        W.updateCopy();
        C.updateCopy();
        R.updateCopy();
        U.updateCopy();

        changedW = W.iterateOnce(W.frontier());
        changedC = C.iterateOnce(frontier);
        changedR = R.iterateOnce(frontier);
        changedU = U.iterateOnce(U.frontier());
        assertEquals(0, changedU.size());
        W.updateCopy();
        C.updateCopy();
        R.updateCopy();
        U.updateCopy();

        assertEquals(U.getInnerVector(), U.getInnerVectorCopy());
    }

    @Test
    void testIterateU() {
        WVector<Map<State, Set<State>>> W = new PrefixWVector(vpa, vpa);
        CVector<Map<State, Set<State>>> C = new PrefixCVector(vpa, vpa, W);
        RVector<Map<State, Set<State>>> R = new PrefixRVector(vpa, vpa, W);
        UVector U = new UVector(vpa, vpa, C, R);

        Set<Pair<State, State>> frontier = vpa.getAllStatePairs();

        Set<Pair<State, State>> changedW = W.iterateOnce(frontier);
        Set<Pair<State, State>> changedU = U.iterateOnce(frontier);
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> copyW = new HashMap<>();
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> copyC = new HashMap<>();
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> copyR = new HashMap<>();
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> copyU = new HashMap<>();

        W.updateCopy();
        U.updateCopy();

        // TODO: Use a logging library
        System.out.println("======================");
        System.out.println("Iterate W, C, R and U");
        System.out.println("======================");
        Set<Pair<State, State>> frontierW = W.frontier();
        Set<Pair<State, State>> frontierC = frontier;
        Set<Pair<State, State>> frontierR = frontier;
        Set<Pair<State, State>> frontierU = U.frontier();
        Set<Pair<State, State>> changedC = new HashSet<>();
        Set<Pair<State, State>> changedR = new HashSet<>();
        int i = 0;
        while (!changedW.isEmpty()
                || !changedC.isEmpty()
                || !changedR.isEmpty()
                || !changedU.isEmpty()) {
            System.out.println("-------------------");
            System.out.println("W CHANGED SIZE: " + changedW.size());
            System.out.println("W FRONTIER SIZE: " + frontierW.size());
            System.out.println("C CHANGED SIZE: " + changedC.size());
            System.out.println("C FRONTIER SIZE: " + frontierC.size());
            System.out.println("R CHANGED SIZE: " + changedR.size());
            System.out.println("R FRONTIER SIZE: " + frontierR.size());
            System.out.println("U CHANGED SIZE: " + changedU.size());
            System.out.println("U FRONTIER SIZE: " + frontierU.size());
            changedW = W.iterateOnce(frontierW);
            changedC = C.iterateOnce(frontierC);
            changedR = R.iterateOnce(frontierR);
            changedU = U.iterateOnce(frontierU);
            W.updateCopy();
            C.updateCopy();
            R.updateCopy();
            U.updateCopy();
            frontierW = W.frontier();
            frontierC = C.frontier();
            frontierR = R.frontier();
            frontierU = U.frontier();
            copyW = W.deepCopy();
            copyC = C.deepCopy();
            copyR = R.deepCopy();
            copyU = U.deepCopy();
            i++;
        }

        assertEquals(copyW, W.getInnerVector());
        assertEquals(copyC, C.getInnerVector());
        assertEquals(copyR, R.getInnerVector());
        assertEquals(copyU, U.getInnerVector());

        System.out.println("Number of iterations: " + i);
        assertEquals(8, i);
    }
}