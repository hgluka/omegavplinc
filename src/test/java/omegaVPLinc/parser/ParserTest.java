package omegaVPLinc.parser;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testParse() throws IOException, Parser.ParseError {
        Parser parser = new Parser("src/test/resources/McCarthy91.bpl_BuchiCegarLoopAbstraction0.ats");
        VPA vpa = parser.parse();
        assertTrue(vpa.getState("s0").get().getCallSuccessors().containsKey(new Symbol("CALL", "c1")));
        for (State state : vpa.getStates()) {
            assertTrue(state.isFinal());
        }
        assertEquals(vpa.getState("s1").get(), vpa.getInitialState());
        assertEquals(2, vpa.getState("s1").get().getInternalSuccessors().size());
    }
}