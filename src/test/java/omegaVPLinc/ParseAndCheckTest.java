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
        InclusionChecker checker1 = new InclusionChecker(A, B, true);
        assertFalse(checker1.checkInclusion());
        InclusionChecker checker2 = new InclusionChecker(B, A, false);
        assertTrue(checker2.checkInclusion());
        InclusionChecker checker3 = new InclusionChecker(A, A, false);
        assertTrue(checker3.checkInclusion());
        InclusionChecker checker4 = new InclusionChecker(B, B, false);
        assertTrue(checker4.checkInclusion());
    }

    @Test
    void check_png2ico() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/png2ico1.ats", "src/test/resources/png2ico2.ats");
    }

    @Test
    void check_png2ico_reversed() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/png2ico2.ats", "src/test/resources/png2ico1.ats");
    }

    @Test
    void check_throttleStd() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/throttle-std1.ats", "src/test/resources/throttle-std2.ats");
    }

    @Test
    void check_throttleSep() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/throttle-sep1.ats", "src/test/resources/throttle-sep2.ats");
    }

    // This test takes a long time. To enable, uncomment next line.
    // @Test
    void check_gzip() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/gzip1.ats", "src/test/resources/gzip2.ats");
    }

    // This test takes a long time. To enable, uncomment next line.
    // @Test
    void check_gzip_reduced() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/gzip1_reduced.ats", "src/test/resources/gzip2_reduced.ats");
    }

    @Test
    void check_gzipFixed_reduced() throws IOException, Parser.ParseError {
        // with minimization via ultimate: 24817.55ms
        assertInclusion("src/test/resources/gzip1_reduced.ats", "src/test/resources/gzip3_reduced.ats");
    }

    @Test
    void check_mcCarthy91() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/McCarthy91.bpl_BuchiCegarLoopAbstraction0.ats", "src/test/resources/McCarthy91.bpl_union.ats");
    }

    @Test
    void check_ddlm2013() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/svcomp_examples/ddlm2013.i_A.ats", "src/test/resources/svcomp_examples/ddlm2013.i_Bunion.ats");
    }

    @Test
    void check_cstrncatMixedAlloca() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/svcomp_examples/cstrncat_mixed_alloca.i_A.ats", "src/test/resources/svcomp_examples/cstrncat_mixed_alloca.i_Bunion.ats");
    }

    @Test
    void check_recMallocEx2() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/svcomp_examples/rec_malloc_ex2.i_A.ats", "src/test/resources/svcomp_examples/rec_malloc_ex2.i_Bunion.ats");
    }

    @Test
    void check_minepumpSpec3Product01() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/svcomp_examples/minepump_spec3_product01.cil.c_A.ats", "src/test/resources/svcomp_examples/minepump_spec3_product01.cil.c_Bunion.ats");
    }

    @Test
    void check_cllSearch() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/svcomp_examples/cll_search-alloca-1.i_A.ats", "src/test/resources/svcomp_examples/cll_search-alloca-1.i_Bunion.ats");
    }

    @Test
    void check_ex02() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/svcomp_examples/Ex02.c_A.ats", "src/test/resources/svcomp_examples/Ex02.c_Bunion.ats");
    }

    @Test
    void check_fibonacci02() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/svcomp_examples/Fibonacci02.c_A.ats", "src/test/resources/svcomp_examples/Fibonacci02.c_Bunion.ats");
    }

    @Test
    void check_bubbleSort2() throws IOException, Parser.ParseError {
        assertNoInclusion("src/test/resources/svcomp_examples/bubble_sort-2.i_A.ats", "src/test/resources/svcomp_examples/bubble_sort-2.i_Bunion.ats");
    }

    @Test
    void check_recMallocEx5() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/svcomp_examples/rec_malloc_ex5.i_A.ats", "src/test/resources/svcomp_examples/rec_malloc_ex5.i_Bunion.ats");
    }

    @Test
    void check_diskPerfSimpl1() throws IOException, Parser.ParseError {
        assertInclusion("src/test/resources/svcomp_examples/diskperf_simpl1.cil.c_A.ats", "src/test/resources/svcomp_examples/diskperf_simpl1.cil.c_Bunion.ats");
    }

    private void assertInclusion(String a, String b) throws IOException, Parser.ParseError {
        Parser parserA = new Parser(a);
        Parser parserB = new Parser(b);
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        InclusionChecker checker = new InclusionChecker(A, B, false);
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
        InclusionChecker checker = new InclusionChecker(A, B, true);
        Instant start = Instant.now();
        boolean isIncluded = checker.checkInclusion();
        Instant end = Instant.now();
        logger.info("The check took " + Duration.between(start, end).toMillis() + " milliseconds.");
        assertFalse(isIncluded);
    }
}
