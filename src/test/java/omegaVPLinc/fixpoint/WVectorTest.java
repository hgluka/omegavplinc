package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.automaton.VPABuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WVectorTest {
    private VPA vpa;
    private Map<String, Symbol> alphabet;

    @BeforeEach
    void setUp() {
        VPABuilder vpaBuilder = new VPABuilder();
        Symbol c = new Symbol("CALL", "c");
        Symbol a = new Symbol("INTERNAL", "a");
        Symbol r = new Symbol("RETURN", "r");
        alphabet = Map.of("c", c, "a", a, "r", r);

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

        q0.addReturnSuccessor(alphabet.get("r"), "q0", q1);
        q1.addReturnPredecessor(alphabet.get("r"), "q0", q0);

        q1.addReturnSuccessor(alphabet.get("r"), "q0", q0);
        q0.addReturnPredecessor(alphabet.get("r"), "q0", q1);

        this.vpa = vpaBuilder.callAlphabet(ca)
                .internalAlphabet(ia)
                .returnAlphabet(ra)
                .stackAlphabet(sa)
                .states(states)
                .initialState(q0)
                .build();
    }

    @Test
    void testIterateOnce() {
        WVector w = new WVector(vpa, vpa);
        w.iterateOnce();
    }
}