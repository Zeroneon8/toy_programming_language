package lexer;

import java.util.EnumSet;
import java.util.Set;

/**
 * Autómata Finito Determinista (AFD) del lenguaje Toy.
 *
 * <p>La tabla tiene una fila por cada valor de {@link Status} y una columna por
 * cada valor de {@link Alphabet}. La celda indica el siguiente estado; cuando
 * vale {@link Status#STOP}, el lexema actual termina y el carácter que provocó
 * la parada inicia el siguiente lexema.</p>
 */
public class Automaton {

    /**
     * Orden de columnas:
     * LETTER, EXPONENT, DIGIT, DOUBLE_QUOTE, SINGLE_QUOTE, SIMPLE_SYMBOL,
     * WHITE_SPACE, DOT, PLUS, MINUS, EQUAL, COLON, TILDE, GREATER_THAN,
     * LESS_THAN, NEW_LINE, OTHER.
     */
    public final Status[][] table = {
        // S0 - START
        {Status.IN_IDENTIFIER, Status.IN_IDENTIFIER, Status.IN_INTEGER, Status.IN_STRING_DOUBLE, Status.IN_STRING_SINGLE, Status.IN_SIMPLE_SYMBOL, Status.START, Status.STOP, Status.IN_SIMPLE_SYMBOL, Status.IN_MINUS, Status.IN_EQUAL_OR_COLON, Status.IN_EQUAL_OR_COLON, Status.IN_ONE_CHAR_COMPARISON, Status.IN_ONE_CHAR_COMPARISON, Status.IN_ONE_CHAR_COMPARISON, Status.START, Status.STOP},

        // S1 - IN_IDENTIFIER
        {Status.IN_IDENTIFIER, Status.IN_IDENTIFIER, Status.IN_IDENTIFIER, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S2 - IN_INTEGER
        {Status.STOP, Status.IN_EXPONENT, Status.IN_INTEGER, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.IN_DECIMAL_POINT, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S3 - IN_DECIMAL_POINT
        {Status.STOP, Status.STOP, Status.IN_DECIMAL, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S4 - IN_DECIMAL
        {Status.STOP, Status.IN_EXPONENT, Status.IN_DECIMAL, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S5 - IN_EXPONENT
        {Status.STOP, Status.STOP, Status.IN_EXPONENT_DIGITS, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.IN_EXPONENT_SIGN, Status.IN_EXPONENT_SIGN, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S6 - IN_EXPONENT_SIGN
        {Status.STOP, Status.STOP, Status.IN_EXPONENT_DIGITS, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S7 - IN_EXPONENT_DIGITS
        {Status.STOP, Status.STOP, Status.IN_EXPONENT_DIGITS, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S8 - IN_STRING_DOUBLE
        {Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_CLOSED, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.IN_STRING_DOUBLE, Status.STOP, Status.IN_STRING_DOUBLE},

        // S9 - IN_STRING_SINGLE
        {Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_CLOSED, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.IN_STRING_SINGLE, Status.STOP, Status.IN_STRING_SINGLE},

        // S10 - IN_STRING_CLOSED
        {Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S11 - IN_SIMPLE_SYMBOL
        {Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S12 - IN_MINUS
        {Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.IN_COMMENT, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S13 - IN_COMMENT
        {Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.IN_COMMENT, Status.STOP, Status.IN_COMMENT},

        // S14 - IN_ONE_CHAR_COMPARISON
        {Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.IN_TWO_CHAR_OPERATOR, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S15 - IN_EQUAL_OR_COLON
        {Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.IN_TWO_CHAR_OPERATOR, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // S16 - IN_TWO_CHAR_OPERATOR
        {Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP},

        // STOP (fila de relleno; nunca se usa como origen)
        {Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP, Status.STOP}
    };

    public Automaton() {
        if (table.length != Status.values().length) {
            throw new IllegalStateException("La tabla debe tener una fila por cada estado.");
        }
        for (Status[] row : table) {
            if (row.length != Alphabet.values().length) {
                throw new IllegalStateException("La tabla debe tener una columna por cada símbolo del alfabeto.");
            }
        }
    }

    /**
     * Clasifica un carácter en un elemento del alfabeto del AFD.
     *
     * <p>Se usan rangos ASCII explícitos para que caracteres como {@code ñ} o
     * las vocales con tilde caigan en {@link Alphabet#OTHER}.</p>
     */
    public Alphabet classify(char c) {
        if (c == 'e' || c == 'E') {
            return Alphabet.EXPONENT;
        }
        if (c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            return Alphabet.LETTER;
        }
        if (c >= '0' && c <= '9') {
            return Alphabet.DIGIT;
        }
        if (c == '"') {
            return Alphabet.DOUBLE_QUOTE;
        }
        if (c == '\'') {
            return Alphabet.SINGLE_QUOTE;
        }
        if (c == '*' || c == '/' || c == '%' || c == '^'
                || c == '(' || c == ')' || c == ',') {
            return Alphabet.SIMPLE_SYMBOL;
        }
        if (c == ' ' || c == '\t' || c == '\r') {
            return Alphabet.WHITE_SPACE;
        }
        if (c == '.') {
            return Alphabet.DOT;
        }
        if (c == '+') {
            return Alphabet.PLUS;
        }
        if (c == '-') {
            return Alphabet.MINUS;
        }
        if (c == '=') {
            return Alphabet.EQUAL;
        }
        if (c == ':') {
            return Alphabet.COLON;
        }
        if (c == '~') {
            return Alphabet.TILDE;
        }
        if (c == '>') {
            return Alphabet.GREATER_THAN;
        }
        if (c == '<') {
            return Alphabet.LESS_THAN;
        }
        if (c == '\n') {
            return Alphabet.NEW_LINE;
        }
        return Alphabet.OTHER;
    }

    /** Estados de aceptación del AFD: S1, S2, S4, S7, S10, S11, S12, S13, S14 y S16. */
    public final Set<Status> acceptance = EnumSet.of(
            Status.IN_IDENTIFIER,
            Status.IN_INTEGER,
            Status.IN_DECIMAL,
            Status.IN_EXPONENT_DIGITS,
            Status.IN_STRING_CLOSED,
            Status.IN_SIMPLE_SYMBOL,
            Status.IN_MINUS,
            Status.IN_COMMENT,
            Status.IN_ONE_CHAR_COMPARISON,
            Status.IN_TWO_CHAR_OPERATOR
    );

    public boolean isAcceptance(Status estado) {
        return acceptance.contains(estado);
    }
}
