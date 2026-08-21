package ca.uqac.lif.textidote.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageMarkerScanner {

    /** One match: position range in the ORIGINAL text, plus target language. */
    public static class MarkedRange {
        public final int start;
        public final int end;
        public final String language;

        public MarkedRange(int start, int end, String language) {
            this.start = start;
            this.end = end;
            this.language = language;
        }

        @Override
        public String toString() {
            return start + "-" + end + " (" + language + ")";
        }
    }

    /**
     * Scans the given text for all occurrences of the given markers,
     * returning the position ranges (in the original text) of the
     * captured {*} argument for each match, along with its language.
     */
    public static List<MarkedRange> scan(String text, List<LanguageMarker> markers) {
        List<MarkedRange> results = new ArrayList<>();
        for (LanguageMarker marker : markers) {
            results.addAll(scanOne(text, marker));
        }
        return results;
    }

    private static List<MarkedRange> scanOne(String text, LanguageMarker marker) {
        List<MarkedRange> results = new ArrayList<>();
        // Regex ne gere que le prefixe jusqu'au premier jeton CAPTURE ;
        // le contenu du jeton CAPTURE lui-meme est extrait par comptage
        // de profondeur d'accolades, pas par regex (accolades imbriquees).
        StringBuilder prefixRegex = new StringBuilder();
        prefixRegex.append(Pattern.quote(marker.command));
        int captureTokenIndex = -1;
        for (int i = 0; i < marker.tokens.size(); i++) {
            LanguageMarker.Token t = marker.tokens.get(i);
            if (t.type == LanguageMarker.TokenType.CAPTURE) {
                captureTokenIndex = i;
                break;
            }
            switch (t.type) {
                case OPTIONAL:
                    prefixRegex.append("(?:\\[.*?\\])?");
                    break;
                case LITERAL:
                    prefixRegex.append("\\{").append(Pattern.quote(t.literalValue)).append("\\}");
                    break;
                case IGNORED:
                    prefixRegex.append("\\{[^}]*\\}");
                    break;
                default:
                    break;
            }
        }
        if (captureTokenIndex < 0) {
            return results; // pas de jeton CAPTURE, rien a faire (ne devrait pas arriver)
        }
        // Le prefixe doit se terminer juste avant l'accolade ouvrante du jeton CAPTURE
        prefixRegex.append("\\{");

        Pattern p = Pattern.compile(prefixRegex.toString());
        Matcher m = p.matcher(text);
        int searchFrom = 0;
        while (m.find(searchFrom)) {
            int openBracePos = m.end() - 1; // position de l'accolade ouvrante elle-meme
            int contentStart = m.end();     // juste apres l'accolade ouvrante
            int depth = 1;
            int pos = contentStart;
            while (depth > 0 && pos < text.length()) {
                char c = text.charAt(pos);
                if (c == '{') depth++;
                else if (c == '}') depth--;
                pos++;
            }
            if (depth == 0) {
                int contentEnd = pos - 1; // position de l'accolade fermante
                // Verifier les jetons APRES le jeton CAPTURE (s'il y en a), pour
                // valider que le reste de la signature correspond bien (sinon,
                // c'est un faux positif accidentel - meme nom de commande, autre forme)
                results.add(new MarkedRange(contentStart, contentEnd, marker.language));
                searchFrom = pos;
            } else {
                // Accolade non fermee correctement : on abandonne cette occurrence
                searchFrom = m.end();
            }
        }
        return results;
    }
}
