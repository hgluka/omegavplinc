package omegaVPLinc;

import omegaVPLinc.algorithm.InclusionChecker;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.parser.Parser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class ParseAndCheckTest {
    @Test
    void parseAndCheckTest() throws IOException, Parser.ParseError {
        Parser parserA = new Parser("src/test/resources/McCarthy91.bpl_BuchiCegarLoopAbstraction0.ats");
        Parser parserB = new Parser("src/test/resources/McCarthy91.bpl_interpolAutomatonUsedInRefinement3after.ats");
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        InclusionChecker checker1 = new InclusionChecker(A, B);
        assertFalse(checker1.checkInclusion());
        InclusionChecker checker2 = new InclusionChecker(B, A);
        assertTrue(checker2.checkInclusion());
        InclusionChecker checker3 = new InclusionChecker(A, A);
        assertTrue(checker3.checkInclusion());
        InclusionChecker checker4 = new InclusionChecker(B, B);
        assertTrue(checker4.checkInclusion());
    }

    @Test
    void parseAndCheckSturmianRecurrentTest() throws IOException, Parser.ParseError {
        Parser parserA = new Parser("src/test/resources/sturmian_words_recurrent_A.ats");
        Parser parserB = new Parser("src/test/resources/sturmian_words_recurrent_B.ats");
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        InclusionChecker checker = new InclusionChecker(A, B);
        Instant start = Instant.now();
        boolean isIncluded = checker.checkInclusion();
        Instant end = Instant.now();
        System.out.println("The check took " + Duration.between(start, end).toSeconds() + " seconds.");
        assertTrue(isIncluded);
    }

    @Test
    void parseAndCheckSturmianPalindromesTest() throws IOException, Parser.ParseError {
        Parser parserA = new Parser("src/test/resources/sturmian_words_palindromes_A.ats");
        Parser parserB = new Parser("src/test/resources/sturmian_words_palindromes_B.ats");
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        InclusionChecker checker = new InclusionChecker(A, B);
        Instant start = Instant.now();
        boolean isIncluded = checker.checkInclusion();
        Instant end = Instant.now();
        System.out.println("The check took " + Duration.between(start, end).toSeconds() + " seconds.");
        assertTrue(isIncluded);
    }

    @Test
    void parseAndCheckBigTest() throws IOException, Parser.ParseError {
        Parser parserA = new Parser("src/test/resources/McCarthy91.bpl_BuchiCegarLoopAbstraction0.ats");
        Parser parserB = new Parser("src/test/resources/union.ats");
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        InclusionChecker checker = new InclusionChecker(A, B);
        Instant start = Instant.now();
        boolean isIncluded = checker.checkInclusion();
        Instant end = Instant.now();
        System.out.println("The check took " + Duration.between(start, end).toSeconds() + " seconds.");
        assertTrue(isIncluded);
    }
}
