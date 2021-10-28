package omegaVPLinc.automaton;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Symbol {
    public enum SymbolType {
        CALL,
        INTERNAL,
        RETURN;
    }
    private SymbolType type;
    private String symbol;

    public Symbol(String symbol) {
        this.symbol = symbol;
    }

    public Symbol(String type, String symbol) throws IllegalArgumentException {
        this.type = SymbolType.valueOf(type);
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public SymbolType getType() {
        return type;
    }

    public void setType(String type) {
        this.type = SymbolType.valueOf(type);
    }

    public boolean typeEquals(Symbol s) {
        if (type == s.getType()) {
            return true;
        } else {
            return false;
        }
    }

    public static Set<Symbol> createAlphabet(String typeString, Set<String> symbols) {
        Set<Symbol> alphabet = new HashSet<>();
        for (String s : symbols) {
            alphabet.add(new Symbol(typeString, s));
        }
        return alphabet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol1 = (Symbol) o;
        return symbol.equals(symbol1.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, symbol);
    }
}
