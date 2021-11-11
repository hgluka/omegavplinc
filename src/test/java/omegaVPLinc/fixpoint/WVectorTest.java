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

class WVectorTest {
    private VPA vpa;
    private Map<String, Symbol> alphabet;

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
    void testIterateOnce() {
        WVector w1 = new WVector(vpa, vpa);
        WVector w2 = new WVector(vpa, vpa);
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> innerW1copy = w1.deepCopy();
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> oldInnerW1copy = w1.deepCopy();
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> innerW2copy = w2.deepCopy();
        Set<Pair<State, State>> frontier = new HashSet<>();
        for (State p : vpa.getStates()) {
            for (State q : vpa.getStates()) {
                frontier.add(Pair.of(p, q));
            }
        }
        Set<Pair<State, State>> changed1 = w1.iterateOnce(innerW1copy, frontier);
        assertEquals(7, changed1.size());
        Pair<State, State> pq = changed1.stream().findAny().get();
        assertNotEquals(oldInnerW1copy, innerW1copy);
        // System.out.println(w1);
        // System.out.println("==========================");

        Set<Pair<State, State>> changed2 = w2.iterateOnce(innerW2copy, frontier);
        assertEquals(innerW1copy, innerW2copy);

        Map<Pair<State, State>, Set<Map<State, Set<State>>>> oldInnerW1 = w1.deepCopy();
        Set<Pair<State, State>> changed12 = w1.iterateOnce(innerW1copy, frontier);

        System.out.println("CHANGED SIZE: " + changed2.size());
        System.out.println("FRONTIER SIZE: " + w2.frontier(changed2).size());

        Set<Pair<State, State>> changed22 =
                w2.iterateOnce(innerW2copy, w2.frontier(changed2));

        assertEquals(changed12, changed22);
        assertEquals(w1, w2);

        System.out.println("CHANGED SIZE: " + changed22.size());
        System.out.println("FRONTIER SIZE: " + w2.frontier(changed22).size());

        /*
        For each of the pairs pq of states changed in the last iteration, we add
        pairs starting with p and ending with a successor of p and
        pairs ending with q and starting with a predecessor of q.
         */
        // System.out.print(w1);
        assertNotEquals(oldInnerW1, w1.getInnerVector());
    }

    @Test
    void testIterate() {
        WVector W = new WVector(vpa, vpa);

        Map<Pair<State, State>, Set<Map<State, Set<State>>>> Wcopy = W.deepCopy();

        Set<Pair<State, State>> frontier = new HashSet<>();
        for (State p : vpa.getStates()) {
            for (State q : vpa.getStates()) {
                frontier.add(Pair.of(p, q));
            }
        }
        Set<Pair<State, State>> changed = W.iterateOnce(Wcopy, frontier);
        Map<Pair<State, State>, Set<Map<State, Set<State>>>> Wcopy2 = new HashMap<>();

        // TODO: Use a logging library
        System.out.println("======================");
        int i = 1;
        while (!changed.isEmpty()) {
            frontier = W.frontier(changed);
            System.out.println("CHANGED SIZE: " + changed.size());
            System.out.println("FRONTIER SIZE: " + frontier.size());
            Wcopy2 = W.deepCopy();
            changed = W.iterateOnce(Wcopy, frontier);
            i++;
        }

        assertEquals(Wcopy2, Wcopy);

        System.out.println("Number of iterations: " + i);

    }
}