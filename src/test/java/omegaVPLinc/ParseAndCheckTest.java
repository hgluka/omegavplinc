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
    void check_test() throws IOException, Parser.ParseError {
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
    void check_sturmianRecurrent() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/All_factors_of_Sturmian_words_are_recurrent_sub.autfilt.ats",
                "src/test/resources/All_factors_of_Sturmian_words_are_recurrent_sup.autfilt.ats");
    }

    @Test
    void check_sturmianPalindromes() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/Sturmian_words_start_with_arbitarily_long_palindromes_sub.autfilt.ats",
                "src/test/resources/Sturmian_words_start_with_arbitarily_long_palindromes_sup.autfilt.ats");
    }

    @Test
    void check_sturmianPalindromes_reversed() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/Sturmian_words_start_with_arbitarily_long_palindromes_sup.autfilt.ats",
                "src/test/resources/Sturmian_words_start_with_arbitarily_long_palindromes_sub.autfilt.ats");
    }

    @Test
    void check_ostrowski() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/Addition_of_Ostrowski-a_representations_is_a_function_(ie,_there_is_an_output_for_every_input)_sub.autfilt.ats",
                "src/test/resources/Addition_of_Ostrowski-a_representations_is_a_function_(ie,_there_is_an_output_for_every_input)_sup.autfilt.ats");
    }

    @Test
    void check_naturalNumbers() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/All_natural_numbers_other_than_0_have_a_predecessor_sub.autfilt.ats",
                "src/test/resources/All_natural_numbers_other_than_0_have_a_predecessor_sup.autfilt.aligned.ats");
    }

    @Test
    void check_lazyOstrowski() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/The_lazy_Ostrowski_representation_is_unique_sub.autfilt.ats",
                "src/test/resources/The_lazy_Ostrowski_representation_is_unique_sup.autfilt.ats");
    }

    @Test
    void check_lazyOstrowski_reversed() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/The_lazy_Ostrowski_representation_is_unique_sup.autfilt.ats",
                "src/test/resources/The_lazy_Ostrowski_representation_is_unique_sub.autfilt.ats");
    }

    @Test
    void check_specialFactors_reversed() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/Specal_factors_are_unique_sup.autfilt.aligned.ats", "src/test/resources/Specal_factors_are_unique_sub.autfilt.ats");
    }

    @Test
    void check_mccarthy91() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/McCarthy91.bpl_BuchiCegarLoopAbstraction0.ats", "src/test/resources/union.ats");
        assertInclusion("src/test/resources/McCarthy91.bpl_BuchiCegarLoopAbstraction0.ats", "src/test/resources/union.ats");
    }

    @Test
    void check_png2ico() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/png2ico1.ats", "src/test/resources/png2ico2.ats");
        assertInclusion("src/test/resources/png2ico1.ats", "src/test/resources/png2ico2.ats");
    }

    @Test
    void check_png2ico_reversed() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/png2ico2.ats", "src/test/resources/png2ico1.ats");
    }

    @Test
    void check_throttleStd() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/throttle-std1.ats", "src/test/resources/throttle-std2.ats");
        assertInclusion("src/test/resources/throttle-std1.ats", "src/test/resources/throttle-std2.ats");
    }

    @Test
    void check_throttleSep() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/throttle-sep1.ats", "src/test/resources/throttle-sep2.ats");
        assertInclusion("src/test/resources/throttle-sep1.ats", "src/test/resources/throttle-sep2.ats");
    }

    @Test
    void check_gzip() throws IOException, Parser.ParseError {
        // without minimization via ultimate: 4h6m
        assertInclusion("src/test/resources/gzip1.ats", "src/test/resources/gzip2.ats");
    }

    @Test
    void check_gzip_reduced() throws IOException, Parser.ParseError {
        // with minimization via ultimate: 6172827.7ms (1h43m)
        assertInclusion("src/test/resources/gzip1_reduced.ats", "src/test/resources/gzip2_reduced.ats");
    }

    @Test
    void check_gzipFixed_reduced() throws IOException, Parser.ParseError {
        // with minimization via ultimate: 24817.55ms
        assertInclusion("src/test/resources/gzip1_reduced.ats", "src/test/resources/gzip3_reduced.ats");
        assertInclusion("src/test/resources/gzip1_reduced.ats", "src/test/resources/gzip3_reduced.ats");
    }

    @Test
    void check_2nested() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/svcomp_examples/2Nested-2.c_BuchiCegarLoopAbstraction0.ats", "src/test/resources/svcomp_examples/2Nested-2.c_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
        assertInclusion("src/test/resources/svcomp_examples/2Nested-2.c_BuchiCegarLoopAbstraction0.ats", "src/test/resources/svcomp_examples/2Nested-2.c_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
    }

    @Test
    void check_4nestedWith3Variables() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/svcomp_examples/4NestedWith3Variables-2.c_BuchiCegarLoopAbstraction0.ats", "src/test/resources/svcomp_examples/4NestedWith3Variables-2.c_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
        assertInclusion("src/test/resources/svcomp_examples/4NestedWith3Variables-2.c_BuchiCegarLoopAbstraction0.ats", "src/test/resources/svcomp_examples/4NestedWith3Variables-2.c_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
    }

    @Test
    void check_brockschmidtCookFuhs() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/svcomp_examples/BrockschmidtCookFuhs-CAV2013-Introduction.c_BuchiCegarLoopAbstraction0.ats", "src/test/resources/svcomp_examples/BrockschmidtCookFuhs-CAV2013-Introduction.c_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
        assertInclusion("src/test/resources/svcomp_examples/BrockschmidtCookFuhs-CAV2013-Introduction.c_BuchiCegarLoopAbstraction0.ats", "src/test/resources/svcomp_examples/BrockschmidtCookFuhs-CAV2013-Introduction.c_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
    }

    @Test
    void check_cairo() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/svcomp_examples/Cairo.c_BuchiCegarLoopAbstraction0.ats", "src/test/resources/svcomp_examples/Cairo.c_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
        assertInclusion("src/test/resources/svcomp_examples/Cairo.c_BuchiCegarLoopAbstraction0.ats", "src/test/resources/svcomp_examples/Cairo.c_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
    }

    @Test
    void check_cairoStep2() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/svcomp_examples/Cairo_step2-1.c_BuchiCegarLoopAbstraction0.ats", "src/test/resources/svcomp_examples/Cairo_step2-1.c_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
        assertInclusion("src/test/resources/svcomp_examples/Cairo_step2-1.c_BuchiCegarLoopAbstraction0.ats", "src/test/resources/svcomp_examples/Cairo_step2-1.c_interpolBuchiNestedWordAutomatonUsedInRefinement1after.ats");
    }

    @Test
    void check_ddlm2013() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/svcomp_examples_processed/ddlm2013.i_A.ats", "src/test/resources/svcomp_examples_processed/ddlm2013.i_Bunion.ats");
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
        logger.info("The check took " + Duration.between(start, end).toMillis() + " milliseconds.");
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
        logger.info("The check took " + Duration.between(start, end).toMillis() + " milliseconds.");
        assertFalse(isIncluded);
    }
}
