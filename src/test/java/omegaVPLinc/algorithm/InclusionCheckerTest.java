package omegaVPLinc.algorithm;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.automaton.VPABuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InclusionCheckerTest {
    private VPA a;
    private VPA b;

    @BeforeEach
    void setUp() {
        VPABuilder aBuilder = new VPABuilder();
        VPABuilder bBuilder = new VPABuilder();
        Set<String> callStrings = Set.of("c0", "c1");
        Set<String> internalStrings = Set.of("a0", "a1", "a2", "a3", "a4", "a5", "a6");
        Set<String> returnStrings = Set.of("r0", "r1");
        Set<Symbol> ca = Symbol.createAlphabet("CALL", callStrings);
        Set<Symbol> ia = Symbol.createAlphabet("INTERNAL", internalStrings);
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


        Set<String> aSA = Set.of("s0", "s1", "s2", "s3", "s4", "s5", "s6");
        Set<String> bSA = Set.of("s0", "s1", "s2");

        Set<State> aStates = new HashSet<>();
        Map<String, State> aStateMap = new HashMap<>();
        for (String ssymbol : aSA) {
            State state = new State(ssymbol);
            state.setFinal(true);
            aStates.add(state);
            aStateMap.put(ssymbol, state);
        }

        Set<State> bStates = new HashSet<>();
        Map<String, State> bStateMap = new HashMap<>();
        for (String ssymbol : bSA) {
            State state = new State(ssymbol);
            if (ssymbol.equals("s1"))
                state.setFinal(true);
            bStates.add(state);
            bStateMap.put(ssymbol, state);
        }

        State bS1 = bStateMap.get("s1");

        State aS1 = aStateMap.get("s1");

        aStateMap.get("s0").addCallSuccessor(alphabetMap.get("c1"), "s0", aStateMap.get("s1"));
        aStateMap.get("s1").addCallPredecessor(alphabetMap.get("c1"), "s0", aStateMap.get("s0"));
        aStateMap.get("s2").addCallSuccessor(alphabetMap.get("c0"), "s2", aStateMap.get("s1"));
        aStateMap.get("s1").addCallPredecessor(alphabetMap.get("c0"), "s2", aStateMap.get("s2"));

        aStateMap.get("s1").addInternalSuccessor(alphabetMap.get("a6"), aStateMap.get("s4"));
        aStateMap.get("s4").addInternalPredecessor(alphabetMap.get("a6"), aStateMap.get("s1"));
        aStateMap.get("s1").addInternalSuccessor(alphabetMap.get("a3"), aStateMap.get("s0"));
        aStateMap.get("s0").addInternalPredecessor(alphabetMap.get("a3"), aStateMap.get("s1"));
        aStateMap.get("s4").addInternalSuccessor(alphabetMap.get("a2"), aStateMap.get("s5"));
        aStateMap.get("s5").addInternalPredecessor(alphabetMap.get("a2"), aStateMap.get("s4"));
        aStateMap.get("s4").addInternalSuccessor(alphabetMap.get("a4"), aStateMap.get("s6"));
        aStateMap.get("s6").addInternalSuccessor(alphabetMap.get("a4"), aStateMap.get("s4"));
        aStateMap.get("s6").addInternalSuccessor(alphabetMap.get("a5"), aStateMap.get("s3"));
        aStateMap.get("s3").addInternalSuccessor(alphabetMap.get("a5"), aStateMap.get("s6"));

        aStateMap.get("s3").addReturnSuccessor(alphabetMap.get("r0"), "s0", aStateMap.get("s2"));
        aStateMap.get("s2").addReturnPredecessor(alphabetMap.get("r0"), "s0", aStateMap.get("s3"));
        aStateMap.get("s3").addReturnSuccessor(alphabetMap.get("r1"), "s2", aStateMap.get("s4"));
        aStateMap.get("s4").addReturnPredecessor(alphabetMap.get("r1"), "s2", aStateMap.get("s3"));

        bStateMap.get("s2").addCallSuccessor(alphabetMap.get("c1"), "s2", bStateMap.get("s1"));
        bStateMap.get("s1").addCallPredecessor(alphabetMap.get("c1"), "s2", bStateMap.get("s2"));

        bStateMap.get("s0").addInternalSuccessor(alphabetMap.get("a2"), bStateMap.get("s0"));
        bStateMap.get("s0").addInternalPredecessor(alphabetMap.get("a2"), bStateMap.get("s0"));
        bStateMap.get("s0").addInternalSuccessor(alphabetMap.get("a4"), bStateMap.get("s0"));
        bStateMap.get("s0").addInternalPredecessor(alphabetMap.get("a4"), bStateMap.get("s0"));
        bStateMap.get("s0").addInternalSuccessor(alphabetMap.get("a5"), bStateMap.get("s0"));
        bStateMap.get("s0").addInternalPredecessor(alphabetMap.get("a5"), bStateMap.get("s0"));
        bStateMap.get("s1").addInternalSuccessor(alphabetMap.get("a6"), bStateMap.get("s0"));
        bStateMap.get("s0").addInternalPredecessor(alphabetMap.get("a6"), bStateMap.get("s1"));
        bStateMap.get("s1").addInternalSuccessor(alphabetMap.get("a3"), bStateMap.get("s2"));
        bStateMap.get("s2").addInternalPredecessor(alphabetMap.get("a3"), bStateMap.get("s1"));

        this.a = aBuilder.callAlphabet(ca)
                .internalAlphabet(ia)
                .returnAlphabet(ra)
                .stackAlphabet(aSA)
                .states(aStates)
                .initialState(aS1)
                .build();

        this.b = bBuilder.callAlphabet(ca)
                .internalAlphabet(ia)
                .returnAlphabet(ra)
                .stackAlphabet(bSA)
                .states(bStates)
                .initialState(bS1)
                .build();
    }

    @Test
    void checkInclusion() {
        InclusionChecker inclusionChecker1 = new InclusionChecker(a, b);
        InclusionChecker inclusionChecker2 = new InclusionChecker(b, a);
        InclusionChecker inclusionChecker3 = new InclusionChecker(a, a);
        InclusionChecker inclusionChecker4 = new InclusionChecker(b, b);
        assertFalse(inclusionChecker1.checkInclusion());
        assertTrue(inclusionChecker2.checkInclusion());
        assertTrue(inclusionChecker3.checkInclusion());
        assertTrue(inclusionChecker4.checkInclusion());
    }
}