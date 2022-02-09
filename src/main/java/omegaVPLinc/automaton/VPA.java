package omegaVPLinc.automaton;

import omegaVPLinc.utility.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class VPA {

    private final Set<Symbol> callAlphabet;
    private final Set<Symbol> internalAlphabet;
    private final Set<Symbol> returnAlphabet;
    private final Set<String> stackAlphabet;
    private final String emptyStackSymbol;
    private final Set<Symbol> fullAlphabet;

    private final Set<State> states;
    private final State initialState;

    private final Map<State, Set<State>> epsilonContext;
    private final Map<State, Set<State>> finalEpsilonContext;

    public VPA(Set<Symbol> callAlphabet,
               Set<Symbol> internalAlphabet,
               Set<Symbol> returnAlphabet,
               Set<String> stackAlphabet,
               Set<State> states,
               State initialState) {
        this.callAlphabet = callAlphabet;
        this.internalAlphabet = internalAlphabet;
        this.returnAlphabet = returnAlphabet;
        this.fullAlphabet = new HashSet<>(this.callAlphabet);
        this.fullAlphabet.addAll(this.internalAlphabet);
        this.fullAlphabet.addAll(this.returnAlphabet);
        this.stackAlphabet = stackAlphabet;
        this.states = states;
        this.initialState = initialState;

        this.epsilonContext = new HashMap<>();
        for (State p : states) {
            this.epsilonContext.put(p, new HashSet<>(Set.of(p)));
        }

        this.finalEpsilonContext = new HashMap<>();
        for (State p : states) {
            if (p.isFinal())
                this.finalEpsilonContext.put(p, new HashSet<>(Set.of(p)));
        }
        this.emptyStackSymbol = "empty";
    }

    public boolean writeToNPVPA(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write("automaton \"npvpa\";\nalphabet;\n");
        HashMap<Symbol, Integer> symbol_map = new HashMap<>();
        Integer i = 0;
        for (Symbol c : this.callAlphabet) {
            writer.write(i + " < " + c.getSymbol() + ";\n");
            symbol_map.put(c, i);
            i++;
        }
        for (Symbol r : this.returnAlphabet) {
            writer.write(i + " > " + r.getSymbol() + ";\n");
            symbol_map.put(r, i);
            i++;
        }
        for (Symbol s : this.internalAlphabet) {
            writer.write(i + " " + s.getSymbol() + ";\n");
            symbol_map.put(s, i);
            i++;
        }
        HashMap<State, Integer> state_map = new HashMap<>();
        i = 0;
        writer.write("states;\n");
        for (State p : states) {
            if (p.isFinal())
                writer.write(i + " 2 " + p.getName() + ";\n");
            else
                writer.write(i + " 1 " + p.getName() + ";\n");
            state_map.put(p, i);
            i++;
        }
        HashMap<String, Integer> stack_map = new HashMap<>();
        i = 0;
        writer.write("stack;\n");
        for (String s : stackAlphabet) {
            writer.write(i + " " + s + ";\n");
            stack_map.put(s, i);
        }
        writer.write("initial " + state_map.get(initialState) + ";\n");
        writer.write("transitions;\n");
        for (State p : states) {
            for (Symbol c : p.getCallSuccessors().keySet()) {
                for (State q : p.getCallSuccessors(c)) {
                    writer.write(state_map.get(p) + " " + symbol_map.get(c) + " " + "(" + state_map.get(q) +","+state_map.get(q)+");\n");
                }
            }
        }
        for (State p : states) {
            for (Symbol s : p.getInternalSuccessors().keySet()) {
                for (State q : p.getInternalSuccessors(s)) {
                    writer.write(state_map.get(p) + " " + symbol_map.get(s) + " " + state_map.get(q) + ";\n");
                }
            }
        }
        for (State p : states) {
            for (Symbol r : p.getReturnSuccessors().keySet()) {
                for (String s : p.getReturnSuccessors().get(r).keySet()) {
                    for (State q : p.getReturnSuccessors(r, s)) {
                        writer.write(state_map.get(p) + " " + stack_map.get(s) + " " + symbol_map.get(r) + " " + state_map.get(q) + ";\n");
                    }
                }
            }
        }
        writer.close();
        return true;
    }

    public String getEmptyStackSymbol() {
        return emptyStackSymbol;
    }

    public Map<State, Set<State>> getEpsilonContext() {
        return epsilonContext;
    }

    public Map<State, Set<State>> getFinalEpsilonContext() {
        return finalEpsilonContext;
    }

    public Map<State, Set<State>> context(Symbol symbol) throws IllegalArgumentException {
        Map<State, Set<State>> ctx = new HashMap<>();
        switch (symbol.getType()) {
            case CALL -> {
                for (State from : states) {
                    Set<State> ctxFromState = new HashSet<>();
                    for (String stackSymbol : stackAlphabet) {
                        Set<State> ctxOfStackSymbol = from.getCallSuccessors()
                                .getOrDefault(symbol, new HashMap<>())
                                .getOrDefault(stackSymbol, new HashSet<>());
                        ctxFromState.addAll(ctxOfStackSymbol);
                    }
                    if (!ctxFromState.isEmpty()) ctx.put(from, ctxFromState);
                }
            }
            case INTERNAL -> {
                for (State from : states) {
                    Set<State> ctxFromState = from.getInternalSuccessors()
                            .getOrDefault(symbol, new HashSet<>());
                    if (!ctxFromState.isEmpty()) ctx.put(from, ctxFromState);
                }
            }
            case RETURN -> {
                for (State from : states) {
                    Set<State> ctxFromState = from.getReturnSuccessors()
                            .getOrDefault(symbol, new HashMap<>())
                            .getOrDefault(emptyStackSymbol, new HashSet<>());
                    if (!ctxFromState.isEmpty()) ctx.put(from, ctxFromState);
                }
            }
            default -> throw new IllegalArgumentException("Symbol type doesn't exist: " + symbol.getType());
        }
        return ctx;
    }

    public Map<State, Set<State>> finalContext(Symbol symbol) {
        Map<State, Set<State>> ctx = context(symbol);
        Map<State, Set<State>> finalCtx = new HashMap<>();
        for (State p : ctx.keySet()) {
            if (p.isFinal()) {
                finalCtx.put(p, ctx.get(p));
            } else {
                for (State q : ctx.get(p)) {
                    if (q.isFinal()) {
                        finalCtx.computeIfAbsent(p, k -> new HashSet<>()).add(q);
                    }
                }
            }
        }
        return finalCtx;
    }

    public Pair<Map<State, Set<State>>, Map<State, Set<State>>> contextPair(Symbol symbol) {
        return Pair.of(context(symbol), finalContext(symbol));
    }

    public Set<Symbol> getCallAlphabet() {
        return callAlphabet;
    }

    public Set<Symbol> getInternalAlphabet() {
        return internalAlphabet;
    }

    public Set<Symbol> getReturnAlphabet() {
        return returnAlphabet;
    }

    public Set<String> getStackAlphabet() {
        return stackAlphabet;
    }

    public Set<State> getStates() {
        return states;
    }

    public Optional<State> getState(String name) {
        for (State q : states) {
            if (q.getName().equals(name))
                return Optional.of(q);
        }
        return Optional.empty();
    }

    public Set<Pair<State, State>> getAllStatePairs() {
        Set<Pair<State, State>> statePairs = new HashSet<>();
        for (State p : states) {
            for (State q : states) {
                statePairs.add(Pair.of(p, q));
            }
        }
        return statePairs;
    }

    public State getInitialState() {
        return initialState;
    }
}
