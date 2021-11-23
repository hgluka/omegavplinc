package omegaVPLinc.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    @Test
    void testLex() throws IOException {
        String fileString = Files.readString(Path.of("src/test/resources/McCarthy91.bpl_BuchiCegarLoopAbstraction0.ats"));
        assertNotEquals("", fileString);

        Lexer lexer = new Lexer();
        List<Lexer.Word> words = lexer.lex(fileString);
        assertEquals("[WORD => NestedWordAutomaton, WORD => nwa, EQ => =, LPR => (, WORD => callAlphabet, EQ => =, LBR => {, WORD => c0, WORD => c1, RBR => }, WORD => internalAlphabet, EQ => =, LBR => {, WORD => a0, WORD => a1, WORD => a2, WORD => a3, WORD => a4, WORD => a5, WORD => a6, RBR => }, WORD => returnAlphabet, EQ => =, LBR => {, WORD => r0, WORD => r1, RBR => }, WORD => states, EQ => =, LBR => {, WORD => s0, WORD => s1, WORD => s2, WORD => s3, WORD => s4, WORD => s5, WORD => s6, RBR => }, WORD => initialStates, EQ => =, LBR => {, WORD => s1, RBR => }, WORD => finalStates, EQ => =, LBR => {, WORD => s0, WORD => s1, WORD => s2, WORD => s3, WORD => s4, WORD => s5, WORD => s6, RBR => }, WORD => callTransitions, EQ => =, LBR => {, LPR => (, WORD => s0, WORD => c1, WORD => s1, RPR => ), LPR => (, WORD => s2, WORD => c0, WORD => s1, RPR => ), RBR => }, WORD => internalTransitions, EQ => =, LBR => {, LPR => (, WORD => s1, WORD => a6, WORD => s4, RPR => ), LPR => (, WORD => s1, WORD => a3, WORD => s0, RPR => ), LPR => (, WORD => s4, WORD => a2, WORD => s5, RPR => ), LPR => (, WORD => s4, WORD => a4, WORD => s6, RPR => ), LPR => (, WORD => s6, WORD => a5, WORD => s3, RPR => ), RBR => }, WORD => returnTransitions, EQ => =, LBR => {, LPR => (, WORD => s3, WORD => s0, WORD => r0, WORD => s2, RPR => ), LPR => (, WORD => s3, WORD => s2, WORD => r1, WORD => s4, RPR => ), RBR => }, RPR => ), EOF => EOF]",
                words.toString());
    }
}