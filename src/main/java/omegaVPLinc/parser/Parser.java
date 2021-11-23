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

    private List<Lexer.Word> stream;
    private Lexer.Word currentWord;
    private VPABuilder vpaBuilder;
    private boolean flag;

    public Parser(String filePath) throws IOException {
        String stringInput = Files.readString(Path.of(filePath));
        Lexer lexer = new Lexer();
        this.stream = lexer.lex(stringInput);
        this.currentWord = this.stream.get(0);
        this.vpaBuilder = new VPABuilder();
        this.flag = false;
    }

    private void accept(Lexer.Token expected) throws ParseError {
        if (currentWord.getToken().equals(expected)) {
            stream.remove(0);
            currentWord = stream.get(0);
        } else {
            throw new ParseError("Syntax error: " + currentWord + " was not expected.");
        }
    }

    private void acceptWord(String expected) throws ParseError {
        if (currentWord.getToken().equals(Lexer.Token.WORD) && currentWord.getLexeme().equals(expected)) {
            stream.remove(0);
            currentWord = stream.get(0);
        } else {
            throw new ParseError("Syntax error: " + currentWord + " was not expected.");
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
        acceptWord("nwa");
        accept(Lexer.Token.EQ);
        accept(Lexer.Token.LPR);
        parseDefinitions();
        accept(Lexer.Token.RPR);
    }

    private void parseDefinitions() throws ParseError {
        boolean innerFlag = true;
        while (innerFlag) {
            if (peekWord("calAlphabet"))
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

    private void parseReturnTransitions() {

    }

    private void parseInternalTransitions() {

    }

    private void parseCallTransitions() {

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
        while (!peek(Lexer.Token.RBR)) {
            if (peek(Lexer.Token.WORD))
                states.add(new State(currentWord.getLexeme()));
            accept(Lexer.Token.WORD);
        }
        accept(Lexer.Token.RBR);
        vpaBuilder.states(states);
    }

    private void parseReturnAlphabet() throws ParseError {
        acceptWord("returnAlphabet");
        parseAlphabet();
        Set<Symbol> returnAlphabet = parseAlphabet();
        vpaBuilder.returnAlphabet(returnAlphabet);
    }

    private void parseInternalAlphabet() throws ParseError {
        acceptWord("internalAlphabet");
        Set<Symbol> internalAlphabet = parseAlphabet();
        vpaBuilder.internalAlphabet(internalAlphabet);
    }

    private void parseCallAlphabet() throws ParseError {
        acceptWord("callAlphabet");
        Set<Symbol> callAlphabet = parseAlphabet();
        vpaBuilder.callAlphabet(callAlphabet);
    }

    private Set<Symbol> parseAlphabet() throws ParseError {
        accept(Lexer.Token.EQ);
        accept(Lexer.Token.LBR);
        Set<Symbol> alphabet = new HashSet<>();
        while (!peek(Lexer.Token.RBR)) {
            if (peek(Lexer.Token.WORD))
                alphabet.add(new Symbol("CALL", currentWord.getLexeme()));
            accept(Lexer.Token.WORD);
        }
        accept(Lexer.Token.RBR);
        return alphabet;
    }
}
