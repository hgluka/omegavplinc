package omegaVPLinc;

import omegaVPLinc.algorithm.InclusionChecker;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.parser.Parser;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class ParseAndCheckTest {
    private static final Logger logger = LoggerFactory.getLogger(ParseAndCheckTest.class);

    @Test
    void parseAndCheckTest() throws IOException, Parser.ParseError {
        Parser parserA = new Parser("src/test/resources/McCarthy91.bpl_BuchiCegarLoopAbstraction0.ats");
        Parser parserB = new Parser("src/test/resources/McCarthy91.bpl_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
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
        assertInclusion("src/test/resources/All_factors_of_Sturmian_words_are_recurrent_sub.autfilt.ats",
                "src/test/resources/All_factors_of_Sturmian_words_are_recurrent_sup.autfilt.ats");
    }

    @Test
    void parseAndCheckSturmianPalindromesTest() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/Sturmian_words_start_with_arbitarily_long_palindromes_sub.autfilt.ats",
                "src/test/resources/Sturmian_words_start_with_arbitarily_long_palindromes_sup.autfilt.ats");
    }

    @Test
    void parseAndCheckSturmianPalindromesReversedTest() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/Sturmian_words_start_with_arbitarily_long_palindromes_sup.autfilt.ats",
                "src/test/resources/Sturmian_words_start_with_arbitarily_long_palindromes_sub.autfilt.ats");
    }

    @Test
    void parseAndCheckOstrowskiTest() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/Addition_of_Ostrowski-a_representations_is_a_function_(ie,_there_is_an_output_for_every_input)_sub.autfilt.ats",
                "src/test/resources/Addition_of_Ostrowski-a_representations_is_a_function_(ie,_there_is_an_output_for_every_input)_sup.autfilt.ats");
    }

    @Test
    void parseAndCheckNaturalNumbersTest() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/All_natural_numbers_other_than_0_have_a_predecessor_sub.autfilt.ats",
                "src/test/resources/All_natural_numbers_other_than_0_have_a_predecessor_sup.autfilt.aligned.ats");
    }

    @Test
    void parseAndCheckLazyOstrowskiTest() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/The_lazy_Ostrowski_representation_is_unique_sub.autfilt.ats",
                "src/test/resources/The_lazy_Ostrowski_representation_is_unique_sup.autfilt.ats");
    }

    @Test
    void parseAndCheckLazyOstrowskiReversedTest() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/The_lazy_Ostrowski_representation_is_unique_sup.autfilt.ats",
                "src/test/resources/The_lazy_Ostrowski_representation_is_unique_sub.autfilt.ats");
    }

    @Test
    void parseAndCheckSpecialFactorsReversedTest() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/Specal_factors_are_unique_sup.autfilt.aligned.ats", "src/test/resources/Specal_factors_are_unique_sub.autfilt.ats");
    }

    @Test
    void parseAndCheckBigTest() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/McCarthy91.bpl_BuchiCegarLoopAbstraction0.ats", "src/test/resources/union.ats");
    }

    private void assertInclusion(String a, String b) throws IOException, Parser.ParseError {
        Parser parserA = new Parser(a);
        Parser parserB = new Parser(b);
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        InclusionChecker checker = new InclusionChecker(A, B);
        Instant start = Instant.now();
        boolean isIncluded = checker.checkInclusion();
        Instant end = Instant.now();
        logger.info("The check took " + Duration.between(start, end).toSeconds() + " seconds.");
        assertTrue(isIncluded);
    }

    private void assertNoInclusion(String a, String b) throws IOException, Parser.ParseError {
        Parser parserA = new Parser(a);
        Parser parserB = new Parser(b);
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        InclusionChecker checker = new InclusionChecker(A, B);
        Instant start = Instant.now();
        boolean isIncluded = checker.checkInclusion();
        Instant end = Instant.now();
        logger.info("The check took " + Duration.between(start, end).toSeconds() + " seconds.");
        assertFalse(isIncluded);
    }
}
