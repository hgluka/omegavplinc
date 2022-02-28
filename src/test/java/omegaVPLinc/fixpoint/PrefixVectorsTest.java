package omegaVPLinc.fixpoint;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.automaton.VPABuilder;
import omegaVPLinc.fixpoint.prefix.*;
import omegaVPLinc.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PrefixVectorsTest {
    private static final Logger logger = LoggerFactory.getLogger(PrefixVectorsTest.class);

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
    void testPrefixes() {
        Prefixes prefixes = new Prefixes(vpa, vpa);
        int iterations = prefixes.iterate();
        assertEquals(13, iterations);
    }

    @Test
    void testPrefixesBigA() throws IOException, Parser.ParseError {
        Parser parserA = new Parser("src/test/resources/svcomp_examples_notdone/diskperf_simpl1.cil.c_A.ats");
        Parser parserB = new Parser("src/test/resources/svcomp_examples_notdone/diskperf_simpl1.cil.c_Bunion.ats");
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        Prefixes prefixes = new Prefixes(A, B);
        Instant start = Instant.now();
        int iterations = prefixes.iterate();
        Instant end = Instant.now();
        logger.info("Prefix iterations took {} seconds.", Duration.between(start, end).toSeconds());
    }


    @Test
    void testPrefixesBig() throws IOException, Parser.ParseError {
        Parser parserA = new Parser("src/test/resources/McCarthy91.bpl_BuchiCegarLoopAbstraction0.ats");
        Parser parserB = new Parser("src/test/resources/union.ats");
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        Prefixes prefixes = new Prefixes(A, B);
        Instant start = Instant.now();
        int iterations = prefixes.iterate();
        Instant end = Instant.now();
        logger.info("Prefix iterations took {} seconds.", Duration.between(start, end).toSeconds());
        assertEquals(23, iterations);
    }

    @Test
    void testPrefixesPalindromes() throws IOException, Parser.ParseError {
        Parser parserA = new Parser("src/test/resources/Sturmian_words_start_with_arbitarily_long_palindromes_sub.autfilt.ats");
        Parser parserB = new Parser("src/test/resources/Sturmian_words_start_with_arbitarily_long_palindromes_sup.autfilt.ats");
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        Prefixes prefixes = new Prefixes(A, B);
        Instant start = Instant.now();
        int iterations = prefixes.iterate();
        Instant end = Instant.now();
        logger.info("Prefix iterations took {} seconds.", Duration.between(start, end).toSeconds());
        assertEquals(10, iterations);
    }
}