package omegaVPLinc.parser;

import omegaVPLinc.automaton.State;
import omegaVPLinc.automaton.Symbol;
import omegaVPLinc.automaton.VPA;
import omegaVPLinc.automaton.VPABuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {
    public static class ParseError extends Exception {
        public ParseError(String message) {
            super(message);
        }
    }

    private final List<Lexer.Word> stream;
    private Lexer.Word currentWord;
    private final VPABuilder vpaBuilder;

    public Parser(String filePath) throws IOException {
        String stringInput = Files.readString(Path.of(filePath));
        Lexer lexer = new Lexer();
        this.stream = lexer.lex(stringInput);
        this.currentWord = this.stream.get(0);
        this.vpaBuilder = new VPABuilder();
    }

    private void accept(Lexer.Token expected) throws ParseError {
        if (currentWord.getToken().equals(expected)) {
            stream.remove(0);
            currentWord = stream.get(0);
        } else {
            throw new ParseError("Syntax error: expected " + expected + ", but got " + currentWord + ".");
        }
    }

    private void acceptWord(String expected) throws ParseError {
        if (currentWord.getToken().equals(Lexer.Token.WORD) && currentWord.getLexeme().equals(expected)) {
            stream.remove(0);
            currentWord = stream.get(0);
        } else {
            throw new ParseError("Syntax error: expected " + expected + ", but got " + currentWord + ".");
        }
    }

    private boolean peek(Lexer.Token expected) {
        return currentWord.getToken().equals(expected);
    }

    private boolean peekWord(String expected) {
        return currentWord.getToken().equals(Lexer.Token.WORD) && currentWord.getLexeme().equals(expected);
    }

    public VPA parse() throws IOException, ParseError {
        parseAutomaton();
        return vpaBuilder.build();
    }

    private void parseAutomaton() throws ParseError {
        acceptWord("NestedWordAutomaton");
        if (peek(Lexer.Token.WORD))
            vpaBuilder.name(currentWord.getLexeme());
        accept(Lexer.Token.WORD);
        accept(Lexer.Token.EQ);
        accept(Lexer.Token.LPR);
        parseDefinitions();
        accept(Lexer.Token.RPR);
    }

    private void parseDefinitions() throws ParseError {
        boolean innerFlag = true;
        while (innerFlag) {
            if (peekWord("callAlphabet"))
                parseCallAlphabet();
            else if (peekWord("internalAlphabet"))
                parseInternalAlphabet();
            else if (peekWord("returnAlphabet"))
                parseReturnAlphabet();
            else if (peekWord("states"))
                parseStates();
            else if (peekWord("initialStates"))
                parseInitialStates();
            else if (peekWord("finalStates"))
                parseFinalStates();
            else if (peekWord("callTransitions"))
                parseCallTransitions();
            else if (peekWord("internalTransitions"))
                parseInternalTransitions();
            else if (peekWord("returnTransitions"))
                parseReturnTransitions();
            else
                innerFlag = false;
        }
    }

    private void parseReturnTransitions() throws ParseError {
        acceptWord("returnTransitions");
        accept(Lexer.Token.EQ);
        accept(Lexer.Token.LBR);
        while (peek(Lexer.Token.LPR)) {
            parseReturnTransition();
        }
        accept(Lexer.Token.RBR);
    }

    private void parseReturnTransition() throws ParseError {
        accept(Lexer.Token.LPR);
        State from = null;
        Symbol returnSymbol = null;
        String stackSymbol = null;
        State to = null;
        if (peek(Lexer.Token.WORD))
            from = vpaBuilder.getState(currentWord.getLexeme());
        accept(Lexer.Token.WORD);
        if (peek(Lexer.Token.WORD))
            stackSymbol = currentWord.getLexeme();
        accept(Lexer.Token.WORD);
        if (peek(Lexer.Token.WORD))
            returnSymbol = vpaBuilder.getReturnSymbol(currentWord.getLexeme());
        accept(Lexer.Token.WORD);
        if (peek(Lexer.Token.WORD))
            to = vpaBuilder.getState(currentWord.getLexeme());
        accept(Lexer.Token.WORD);
        if (from == null || stackSymbol == null || returnSymbol == null || to == null) {
            throw new ParseError("Syntax error: part of returnTransition doesn't exist. FROM: " + from + " returnSymbol: " + returnSymbol + " stackSymbol: " + stackSymbol + " TO: " + to + ".");
        }
        from.addReturnSuccessor(returnSymbol, stackSymbol, to);
        to.addReturnPredecessor(returnSymbol, stackSymbol, from);
        accept(Lexer.Token.RPR);
    }

    private void parseInternalTransitions() throws ParseError {
        acceptWord("internalTransitions");
        accept(Lexer.Token.EQ);
        accept(Lexer.Token.LBR);
        while (peek(Lexer.Token.LPR)) {
            parseInternalTransition();
        }
        accept(Lexer.Token.RBR);
    }

    private void parseInternalTransition() throws ParseError {
        accept(Lexer.Token.LPR);
        State from = null;
        Symbol internalSymbol = null;
        State to = null;
        if (peek(Lexer.Token.WORD))
            from = vpaBuilder.getState(currentWord.getLexeme());
        accept(Lexer.Token.WORD);
        if (peek(Lexer.Token.WORD))
            internalSymbol = vpaBuilder.getInternalSymbol(currentWord.getLexeme());
        accept(Lexer.Token.WORD);
        if (peek(Lexer.Token.WORD))
            to = vpaBuilder.getState(currentWord.getLexeme());
        accept(Lexer.Token.WORD);
        if (from == null || internalSymbol == null || to == null) {
            throw new ParseError("Syntax error: part of internalTransition doesn't exist. FROM: " + from + " internalSymbol: " + internalSymbol + " TO: " + to + ".");
        }
        from.addInternalSuccessor(internalSymbol, to);
        to.addInternalPredecessor(internalSymbol, from);
        accept(Lexer.Token.RPR);
    }

    private void parseCallTransitions() throws ParseError {
        acceptWord("callTransitions");
        accept(Lexer.Token.EQ);
        accept(Lexer.Token.LBR);
        while (peek(Lexer.Token.LPR)) {
            parseCallTransition();
        }
        accept(Lexer.Token.RBR);
    }

    private void parseCallTransition() throws ParseError {
        accept(Lexer.Token.LPR);
        State from = null;
        Symbol callSymbol = null;
        State to = null;
        if (peek(Lexer.Token.WORD))
            from = vpaBuilder.getState(currentWord.getLexeme());
        accept(Lexer.Token.WORD);
        if (peek(Lexer.Token.WORD))
            callSymbol = vpaBuilder.getCallSymbol(currentWord.getLexeme());
        accept(Lexer.Token.WORD);
        if (peek(Lexer.Token.WORD))
            to = vpaBuilder.getState(currentWord.getLexeme());
        accept(Lexer.Token.WORD);
        if (from == null || callSymbol == null || to == null) {
            throw new ParseError("Syntax error: part of callTransition doesn't exist. FROM: " + from + " callSymbol: " + callSymbol + " TO: " + to + ".");
        }
        from.addCallSuccessor(callSymbol, from.getName(), to);
        to.addCallPredecessor(callSymbol, from.getName(), from);
        accept(Lexer.Token.RPR);
    }

    private void parseFinalStates() throws ParseError {
        if (vpaBuilder.getStates() == null) {
            throw new ParseError("Syntax error: states must be declared before finalStates");
        }
        acceptWord("finalStates");
        accept(Lexer.Token.EQ);
        accept(Lexer.Token.LBR);
        Set<State> finalStates = new HashSet<>();
        while (!peek(Lexer.Token.RBR)) {
            if (peek(Lexer.Token.WORD))
                finalStates.add(new State(currentWord.getLexeme()));
            accept(Lexer.Token.WORD);
        }
        accept(Lexer.Token.RBR);
        for (State state : vpaBuilder.getStates()) {
            if (finalStates.contains(state))
                state.setFinal(true);
        }
    }

    private void parseInitialStates() throws ParseError {
        if (vpaBuilder.getStates() == null) {
            throw new ParseError("Syntax error: states must be declared before initialStates");
        }
        acceptWord("initialStates");
        accept(Lexer.Token.EQ);
        accept(Lexer.Token.LBR);
        if (peek(Lexer.Token.WORD) && vpaBuilder.getStates().contains(new State(currentWord.getLexeme())))
            for (State state : vpaBuilder.getStates())
                if (state.equals(new State(currentWord.getLexeme())))
                    vpaBuilder.initialState(state);
        accept(Lexer.Token.WORD);
        accept(Lexer.Token.RBR);
    }

    private void parseStates() throws ParseError {
        acceptWord("states");
        accept(Lexer.Token.EQ);
        accept(Lexer.Token.LBR);
        Set<State> states = new HashSet<>();
        Set<String> stackAlphabet = new HashSet<>();
        while (!peek(Lexer.Token.RBR)) {
            if (peek(Lexer.Token.WORD)) {
                states.add(new State(currentWord.getLexeme()));
                stackAlphabet.add(currentWord.getLexeme());
            }
            accept(Lexer.Token.WORD);
        }
        accept(Lexer.Token.RBR);
        vpaBuilder.states(states);
        vpaBuilder.stackAlphabet(stackAlphabet);
    }

    private void parseReturnAlphabet() throws ParseError {
        acceptWord("returnAlphabet");
        Set<Symbol> returnAlphabet = parseAlphabet("RETURN");
        vpaBuilder.returnAlphabet(returnAlphabet);
    }

    private void parseInternalAlphabet() throws ParseError {
        acceptWord("internalAlphabet");
        Set<Symbol> internalAlphabet = parseAlphabet("INTERNAL");
        vpaBuilder.internalAlphabet(internalAlphabet);
    }

    private void parseCallAlphabet() throws ParseError {
        acceptWord("callAlphabet");
        Set<Symbol> callAlphabet = parseAlphabet("CALL");
        vpaBuilder.callAlphabet(callAlphabet);
    }

    private Set<Symbol> parseAlphabet(String alphabetType) throws ParseError {
        accept(Lexer.Token.EQ);
        accept(Lexer.Token.LBR);
        Set<Symbol> alphabet = new HashSet<>();
        while (!peek(Lexer.Token.RBR)) {
            if (peek(Lexer.Token.WORD))
                alphabet.add(new Symbol(alphabetType, currentWord.getLexeme()));
            accept(Lexer.Token.WORD);
        }
        accept(Lexer.Token.RBR);
        return alphabet;
    }
}
