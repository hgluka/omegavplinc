package omegaVPLinc.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public enum Token {
        WORD("([_a-zA-Z][_a-zA-Z0-9]*)|(\"[^\"]+\")"),
        EQ("="),
        LBR("\\{"), RBR("\\}"),
        LPR("\\("), RPR("\\)"),
        COMMENT("\\/\\/.*"),
        SKIP("[ \t\f\r\n,;]"),
        EOF("\\Z");

        private final String pattern;

        Token(String pattern) {
            this.pattern = pattern;
        }
    }

    public class Word {
        private Token token;
        private String lexeme;

        public Word(Token token, String lexeme) {
            this.token = token;
            this.lexeme = lexeme;
        }

        public Token getToken() {
            return token;
        }

        public String getLexeme() {
            return lexeme;
        }

        @Override
        public String toString() {
            return token.name() + " => " + lexeme;
        }
    }

    public List<Word> lex(String input) {
        List<Word> words = new LinkedList<>();

        StringBuilder tokenPatternsBuffer = new StringBuilder();
        for (Token token : Token.values()) {
            tokenPatternsBuffer.append(String.format("|(?<%s>%s)", token.name(), token.pattern));
        }
        Pattern tokenPatterns = Pattern.compile(tokenPatternsBuffer.substring(1));

        Matcher matcher = tokenPatterns.matcher(input);
        while (matcher.find()) {
            for (Token token : Token.values()) {
                if (!token.name().equals("SKIP") && !token.name().equals("COMMENT") && matcher.group(token.name()) != null) {
                    if (token.name().equals("EOF")) {
                        words.add(new Word(token, "EOF"));
                        break;
                    }
                    words.add(new Word(token, matcher.group(token.name())));
                    continue;
                }
            }
        }

        return words;
    }
}
