package omegaVPLinc.automaton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VPABuilderTest {
    private VPABuilder vpaBuilder;
    private Map<String, Symbol> alphabet;

    @BeforeEach
    void setUp() {
        vpaBuilder = new VPABuilder();
        Symbol c = new Symbol("CALL", "c");
        Symbol a = new Symbol("INTERNAL", "a");
        Symbol r = new Symbol("RETURN", "r");
        alphabet = Map.of("c", c, "a", a, "r", r);
    }

    @Test
    void testBuild() {
        Set<Symbol> ca = Set.of(alphabet.get("c"));
        Set<Symbol> ia = Set.of(alphabet.get("a"));
        Set<Symbol> ra = Set.of(alphabet.get("r"));
        Set<String> sa = Set.of("q0", "q1");

        State q0 = new State("q0");
        State q1 = new State("q1");
        Set<State> states = Set.of(q0, q1);

        q1.setFinal(true);
        q0.addCallSuccessor(alphabet.get("c"), "q0", q0);
        q0.addCallPredecessor(alphabet.get("c"), "q0", q0);

        q0.addInternalSuccessor(alphabet.get("a"), q0);
        q0.addInternalPredecessor(alphabet.get("a"), q0);
        q0.addInternalSuccessor(alphabet.get("a"), q1);

        q0.addReturnSuccessor(alphabet.get("r"), "q0", q1);
        q1.addReturnPredecessor(alphabet.get("r"), "q0", q0);

        q1.addReturnSuccessor(alphabet.get("r"), "q0", q0);
        q0.addReturnPredecessor(alphabet.get("r"), "q0", q1);
        q0.addReturnPredecessor(alphabet.get("r"), "q0", q0);

        VPA vpa = vpaBuilder.callAlphabet(ca)
                .internalAlphabet(ia)
                .returnAlphabet(ra)
                .stackAlphabet(sa)
                .states(states)
                .initialState(q0)
                .build();
        assertEquals(ca, vpa.getCallAlphabet());
        assertEquals(ia, vpa.getInternalAlphabet());
        assertEquals(ra, vpa.getReturnAlphabet());
        assertEquals(sa, vpa.getStackAlphabet());
        assertEquals(states, vpa.getStates());
        assertEquals(q0, vpa.getInitialState());
        assertEquals(Set.of(q0, q1), q0.getInternalSuccessors().get(alphabet.get("a")));
        assertEquals(q0.getCallSuccessors(),
                vpa.getInitialState().getCallSuccessors());
        assertEquals(q1.getCallPredecessors(),
                vpa.getState("q1").get().getCallPredecessors());
    }
}