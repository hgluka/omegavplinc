package omegaVPLinc.automaton;

import omegaVPLinc.algorithm.InclusionChecker;
import omegaVPLinc.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class VPATest {
    private static final Logger logger = LoggerFactory.getLogger(VPATest.class);
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
    void testContexts() {
        Map<State, Set<State>> contextOfR = vpa.context(new Symbol("RETURN", "r"));
        Map<State, Set<State>> expectedContextOfR = Map.of(
                vpa.getState("q0").get(),
                Set.of(vpa.getState("q1").get()),
                vpa.getState("q1").get(),
                Set.of(vpa.getState("q0").get()));
        assertEquals(expectedContextOfR, contextOfR);
    }

    @Test
    void testWriteToNpvpa() throws IOException, Parser.ParseError {
        Parser parserA = new Parser("src/test/resources/svcomp_examples_processed/ddlm2013.i_A.ats");
        Parser parserB = new Parser("src/test/resources/svcomp_examples_processed/ddlm2013.i_Bunion.ats");
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        A.writeToNPVPA("src/test/resources/svcomp_examples_npvpa/ddlm2013.i_A.npvpa");
        B.writeToNPVPA("src/test/resources/svcomp_examples_npvpa/ddlm2013.i_Bunion.npvpa");
    }

    @Test
    void testWriteAllToNpvpa() throws IOException, Parser.ParseError {
        File dir = new File("src/test/resources/svcomp_examples_processed/");
        File[] directoryListing = dir.listFiles();
        VPA A;
        Parser parser;
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (!Files.exists(Path.of("src/test/resources/svcomp_examples_npvpa/" + child.getName().substring(0, child.getName().length() - 3) + "npvpa"))) {
                    parser = new Parser(child.getAbsolutePath());
                    A = parser.parse();
                    logger.info("src/test/resources/svcomp_examples_npvpa/" + child.getName().substring(0, child.getName().length() - 3) + "npvpa");
                    A.writeToNPVPA("src/test/resources/svcomp_examples_npvpa/" + child.getName().substring(0, child.getName().length() - 3) + "npvpa");
                }
            }
        }
    }
}
