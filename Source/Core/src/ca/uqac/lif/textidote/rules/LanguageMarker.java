package ca.uqac.lif.textidote.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one rule from the lang-markers config file: a LaTeX command
 * name, an ordered list of argument tokens describing its signature, and
 * the target language to apply to the captured text argument.
 */
public class LanguageMarker {

    /** Types of tokens that can appear in a command's argument template. */
    public enum TokenType {
        OPTIONAL,    // []      - optional bracket argument, skipped
        LITERAL,     // {word}  - mandatory argument, must match exactly
        IGNORED,     // {}      - mandatory argument, present but skipped
        CAPTURE      // {*}     - mandatory argument, captured as the text to check
    }

    public static class Token {
        public final TokenType type;
        public final String literalValue; // only used for LITERAL

        public Token(TokenType type, String literalValue) {
            this.type = type;
            this.literalValue = literalValue;
        }

        public static Token parse(String raw) {
            raw = raw.trim();
            if (raw.equals("[]")) {
                return new Token(TokenType.OPTIONAL, null);
            }
            if (raw.equals("{}")) {
                return new Token(TokenType.IGNORED, null);
            }
            if (raw.equals("{*}")) {
                return new Token(TokenType.CAPTURE, null);
            }
            if (raw.startsWith("{") && raw.endsWith("}")) {
                return new Token(TokenType.LITERAL, raw.substring(1, raw.length() - 1));
            }
            throw new IllegalArgumentException("Jeton non reconnu : " + raw);
        }
    }

    public final String command;      // e.g. "\\fblockcquote"
    public final List<Token> tokens;  // ordered argument template
    public final String language;     // target language code

    public LanguageMarker(String command, List<Token> tokens, String language) {
        this.command = command;
        this.tokens = tokens;
        this.language = language;
    }

    /**
     * Parses a single non-comment, non-blank line of the config file.
     * Fields are tab-separated: command, then one field per token, then
     * the language code last.
     */
    public static LanguageMarker parseLine(String line) {
        String[] parts = line.split("\t+");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Ligne malformee (pas assez de champs) : " + line);
        }
        String command = parts[0].trim();
        String language = parts[parts.length - 1].trim();
        List<Token> tokens = new ArrayList<>();
        for (int i = 1; i < parts.length - 1; i++) {
            tokens.add(Token.parse(parts[i]));
        }
        boolean hasCapture = false;
        for (Token t : tokens) {
            if (t.type == TokenType.CAPTURE) {
                hasCapture = true;
                break;
            }
        }
        if (!hasCapture) {
            throw new IllegalArgumentException("Aucun jeton {*} trouve dans la ligne : " + line);
        }
        return new LanguageMarker(command, tokens, language);
    }

    public static List<LanguageMarker> parseFile(List<String> lines) {
        List<LanguageMarker> markers = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            markers.add(parseLine(trimmed));
        }
        return markers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(command);
        for (Token t : tokens) {
            sb.append(" ").append(t.type).append(t.literalValue != null ? "(" + t.literalValue + ")" : "");
        }
        sb.append(" -> ").append(language);
        return sb.toString();
    }
}
