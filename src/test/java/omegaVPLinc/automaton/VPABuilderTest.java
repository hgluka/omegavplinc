package omegaVPLinc.automaton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VPABuilderTest {
    private VPABuilder vpaBuilder;

    @BeforeEach
    void setUp() {
        vpaBuilder = new VPABuilder();
    }

    @Test
    void build() {
        Set<String> ca = Set.of("c0", "c1");
        Set<String> ia = Set.of("a0");
        Set<String> ra = Set.of("r0", "r1", "r2");
        Set<String> sa = Set.of("q0", "q1");

        State q0 = new State("q0", false);
        State q1 = new State("q1", true);
        Set<State> states = Set.of(q0, q1);

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
    }
}