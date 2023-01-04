# omegaVPLinc

An algorithm that checks for inclusion between two ω-VPLs generated by the given automata.

## Dependencies

- Java 17

## Building and Running

```
./gradlew build -x test
java -jar build/libs/omegaVPLinc-1.0.jar A.ats B.ats
```

Use `-x test` when building because tests take a while to complete.

## Usage
```
Usage: omegaVPLinc [-hVw] <A> <B>
Checks for inclusion between 2 omega-VPL automata.
      <A>         First automaton file.
      <B>         Second automaton file.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
  -w, --words     Keep track of words to find counterexamples.
```

The two automata `A` and `B` should be given in the `.ats` (Automata Script) format
defined for the [Ultimate Automata Library](https://monteverdi.informatik.uni-freiburg.de/tomcat/Website/?ui=tool&tool=automata_library)

Running with `--words` is slower, and is unnecessary in cases where the inclusion holds.
When inclusion doesn't hold, you can rerun the program with `--words` to find a counterexample:
```
java -jar build/libs/omegaVPLinc-1.0.jar --words A.ats B.ats
```

## Tests and benchmarks

There are unit (or integration) tests defined in `src/test/java/`
To run them, type:
```
./gradlew test
```

There are also benchmarks zipped in `src/test/resources/`
To run them, type:
```
unzip Ultimate.zip
cd src/test/resources
unzip svcomp_examples.zip
cd ..
python3 run_examples.py --run -i resources/svcomp_examples/
```
This will run omegaVPLinc and Ultimate on the whole benchmark suite and output a csv file with run times.
If you want to do something else with the benchmark, check `python3 run_examples.py --help` for more info.

## Authors
- [Kyveli Doveri](https://kyveli.github.io/)
- [Pierre Ganty](https://software.imdea.org/~pierreganty/)
- [Luka Hadzi-Djokic](https://hgluka.net)
