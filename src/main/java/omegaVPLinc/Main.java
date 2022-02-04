package omegaVPLinc;

import omegaVPLinc.algorithm.InclusionChecker;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

@CommandLine.Command(name="omegaVPLinc", mixinStandardHelpOptions = true, version = "omegaVPLinc 1.0",
                    description = "Checks for inclusion between 2 omega-VPL automata.")
public class Main implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @CommandLine.Parameters(index = "0", description = "First automaton file.")
    private File Afile;

    @CommandLine.Parameters(index = "1", description = "Second automaton file.")
    private File Bfile;

    @Override
    public Integer call() throws Exception {
        Parser parserA = new Parser(Afile.getPath());
        Parser parserB = new Parser(Bfile.getPath());
        VPA A = parserA.parse();
        VPA B = parserB.parse();
        InclusionChecker checker = new InclusionChecker(A, B);
        Instant start = Instant.now();
        boolean isIncluded = checker.checkInclusion();
        Instant end = Instant.now();
        logger.info("Is " + Afile.getName() + " a subset of " + Bfile.getName() + ": " + isIncluded);
        if (isIncluded) {
            logger.info(Afile.getName() + " is a subset of " + Bfile.getName());
        } else {
            logger.info(Afile.getName() + " is not a subset of " + Bfile.getName());
        }
        logger.info("The check took " + Duration.between(start, end).toMillis() + " milliseconds.");
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
