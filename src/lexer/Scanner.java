package lexer;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import token.Token;
import token.TokenType;

/**
 * Analizador léxico del lenguaje Toy.
 *
 * <p>El escaneo se realiza en dos fases, igual que en el analizador de
 * referencia:</p>
 *
 * <ol>
 *   <li>{@link #getLexemas()} recorre el arreglo de caracteres y usa el AFD de
 *       {@link Automaton} para aislar cada lexema. Los comentarios {@code --}
 *       se descartan en esta fase.</li>
 *   <li>{@link #classifyAllTokens(ArrayList)} clasifica cada lexema en un
 *       {@link TokenType} y devuelve la lista de {@link Token}.</li>
 * </ol>
 */
public class Scanner {

    private static final String COMMENT_PREFIX = "--";

    /** Número Toy: entero, decimal opcional y exponente opcional. */
    private static final Pattern NUMBER_PATTERN =
            Pattern.compile("[0-9]+(\\.[0-9]+)?([eE][+-]?[0-9]+)?");

    private Status status;
    private final char[] buffer;
    private Map<String, TokenType> reservedWords;
    private Map<String, TokenType> specialSymbols;
    private ArrayList<String> lastLexemas;

    public Scanner(char[] buffer) {
        this.buffer = buffer;
        this.status = Status.START;
        initializeReservedWords();
        initializeSpecialSymbols();
    }

    /**
     * Convierte una lista de lexemas en tokens, aplicando las tablas de
     * palabras reservadas, símbolos y las reglas de identificadores/números.
     */
    public ArrayList<Token> classifyAllTokens(ArrayList<String> lexemas) {
        ArrayList<Token> tokens = new ArrayList<>();
        for (String lexema : lexemas) {
            tokens.add(classifyToken(lexema));
        }
        return tokens;
    }

    /**
     * Recorre el arreglo de caracteres y devuelve todos los lexemas válidos.
     *
     * <p>Los espacios, tabuladores, retornos de carro y saltos de línea se
     * ignoran cuando el autómata está en {@link Status#START}. Dentro de una
     * cadena o de un comentario sí se procesan con la tabla.</p>
     */
    public ArrayList<String> getLexemas() {
        status = Status.START;
        Automaton automaton = new Automaton();
        StringBuilder lexema = new StringBuilder();
        ArrayList<String> lexemas = new ArrayList<>();
        this.lastLexemas = lexemas;

        int i = 0;
        int lexemaStart = 0;

        while (i < buffer.length) {
            char c = buffer[i];
            Alphabet alphabet = automaton.classify(c);

            // Separadores entre lexemas: no generan tokens.
            if (status == Status.START
                    && (alphabet == Alphabet.WHITE_SPACE || alphabet == Alphabet.NEW_LINE)) {
                i++;
                continue;
            }

            Status nextStatus = automaton.table[status.ordinal()][alphabet.ordinal()];

            if (nextStatus == Status.STOP) {
                // Si el estado actual no es de aceptación, el lexema quedó
                // incompleto: número mal formado, cadena sin cerrar, etc.
                if (!automaton.isAcceptance(status)) {
                    this.lastLexemas = new ArrayList<>(lexemas);
                    throw new IllegalArgumentException(describeError(status, c, i, lexemaStart));
                }

                addLexema(lexemas, lexema);
                lexema.setLength(0);
                status = Status.START;
                // No se avanza i: el carácter actual inicia el siguiente lexema.
                continue;
            }

            if (lexema.length() == 0) {
                lexemaStart = i;
            }
            lexema.append(c);
            status = nextStatus;
            i++;
        }

        if (status != Status.START) {
            if (!automaton.isAcceptance(status)) {
                this.lastLexemas = new ArrayList<>(lexemas);
                throw new IllegalArgumentException(describeEofError(status, lexemaStart));
            }
            addLexema(lexemas, lexema);
        }

        return lexemas;
    }

    public ArrayList<String> getLastLexemas() {
        return lastLexemas != null ? lastLexemas : new ArrayList<>();
    }

    /**
     * Clasifica un lexema. El orden importa: las cadenas se revisan primero
     * porque su contenido puede parecerse a una palabra reservada o a un
     * símbolo.
     */
    public Token classifyToken(String lexema) {
        if (isStringLiteral(lexema)) {
            return new Token(TokenType.TOK_STRING, lexema);
        }
        if (reservedWords.containsKey(lexema)) {
            return new Token(reservedWords.get(lexema), lexema);
        }
        if (specialSymbols.containsKey(lexema)) {
            return new Token(specialSymbols.get(lexema), lexema);
        }
        if (isNumber(lexema)) {
            return new Token(TokenType.TOK_NUMBER, lexema);
        }
        if (isIdentifier(lexema)) {
            return new Token(TokenType.TOK_ID, lexema);
        }
        throw new IllegalArgumentException("Lexema no reconocido: " + lexema);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Agrega el lexema a la lista, salvo que sea un comentario. Un comentario
     * de Toy siempre empieza con {@code --}; el token menos es exactamente
     * {@code -}, así que no se confunde.
     */
    private void addLexema(ArrayList<String> lexemas, StringBuilder lexema) {
        if (lexema.length() == 0) {
            return;
        }
        String value = lexema.toString();
        if (value.startsWith(COMMENT_PREFIX)) {
            return;
        }
        lexemas.add(value);
    }

    private String describeError(Status status, char c, int position, int lexemaStart) {
        switch (status) {
            case START:
                if (c == '.') {
                    return "El punto decimal debe ir precedido por un numero en la posicion " + position;
                }
                return "Caracter no reconocido '" + c + "' en la posicion " + position;
            case IN_DECIMAL_POINT:
                return "Se esperaban digitos despues del punto decimal en el numero iniciado en la posicion " + lexemaStart;
            case IN_EXPONENT:
                return "Se esperaba un digito o un signo en el exponente del numero iniciado en la posicion " + lexemaStart;
            case IN_EXPONENT_SIGN:
                return "Se esperaban digitos despues del signo del exponente del numero iniciado en la posicion " + lexemaStart;
            case IN_STRING_DOUBLE:
            case IN_STRING_SINGLE:
                return "Cadena sin cerrar: se encontro un salto de linea en la posicion " + position
                        + " (la cadena inicia en la posicion " + lexemaStart + ")";
            case IN_EQUAL_OR_COLON:
                return "Se esperaba '=' para completar el operador iniciado en la posicion " + lexemaStart;
            default:
                return "Lexema no valido en la posicion " + lexemaStart;
        }
    }

    private String describeEofError(Status status, int lexemaStart) {
        switch (status) {
            case IN_DECIMAL_POINT:
                return "Numero incompleto: se esperaban digitos despues del punto decimal "
                        + "(numero iniciado en la posicion " + lexemaStart + ").";
            case IN_EXPONENT:
                return "Numero incompleto: se esperaba un digito o un signo en el exponente "
                        + "(numero iniciado en la posicion " + lexemaStart + ").";
            case IN_EXPONENT_SIGN:
                return "Numero incompleto: se esperaban digitos despues del signo del exponente "
                        + "(numero iniciado en la posicion " + lexemaStart + ").";
            case IN_STRING_DOUBLE:
            case IN_STRING_SINGLE:
                return "Cadena sin cerrar hasta el final del archivo "
                        + "(la cadena inicia en la posicion " + lexemaStart + ").";
            case IN_EQUAL_OR_COLON:
                return "Se esperaba '=' para completar el operador iniciado en la posicion " + lexemaStart + ".";
            default:
                return "Lexema incompleto al final del archivo "
                        + "(iniciado en la posicion " + lexemaStart + ").";
        }
    }

    private void initializeReservedWords() {
        reservedWords = Map.ofEntries(
                Map.entry("if", TokenType.TOK_IF),
                Map.entry("then", TokenType.TOK_THEN),
                Map.entry("else", TokenType.TOK_ELSE),
                Map.entry("true", TokenType.TOK_TRUE),
                Map.entry("false", TokenType.TOK_FALSE),
                Map.entry("and", TokenType.TOK_AND),
                Map.entry("or", TokenType.TOK_OR),
                Map.entry("while", TokenType.TOK_WHILE),
                Map.entry("do", TokenType.TOK_DO),
                Map.entry("for", TokenType.TOK_FOR),
                Map.entry("func", TokenType.TOK_FUNC),
                Map.entry("end", TokenType.TOK_END),
                Map.entry("local", TokenType.TOK_LOCAL),
                Map.entry("print", TokenType.TOK_PRINT),
                Map.entry("println", TokenType.TOK_PRINTLN),
                Map.entry("ret", TokenType.TOK_RET),
                Map.entry("number", TokenType.TOK_VNUMBER),
                Map.entry("string", TokenType.TOK_VSTRING),
                Map.entry("bool", TokenType.TOK_BOOL)
        );
    }

    private void initializeSpecialSymbols() {
        specialSymbols = Map.ofEntries(
                Map.entry("+", TokenType.TOK_PLUS),
                Map.entry("-", TokenType.TOK_MINUS),
                Map.entry("*", TokenType.TOK_MUL),
                Map.entry("/", TokenType.TOK_DIV),
                Map.entry("%", TokenType.TOK_MOD),
                Map.entry("^", TokenType.TOK_EXP),
                Map.entry(">", TokenType.TOK_GT),
                Map.entry("<", TokenType.TOK_LT),
                Map.entry(">=", TokenType.TOK_GTE),
                Map.entry("<=", TokenType.TOK_LTE),
                Map.entry("==", TokenType.TOK_EQ),
                Map.entry("~=", TokenType.TOK_NEQ),
                Map.entry("~", TokenType.TOK_NOT),
                Map.entry(":=", TokenType.TOK_ASSIGN),
                Map.entry("(", TokenType.TOK_LPAR),
                Map.entry(")", TokenType.TOK_RPAR),
                Map.entry(",", TokenType.TOK_COMMA)
        );
    }

    private boolean isAsciiLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isIdentifier(String lexema) {
        if (lexema.isEmpty()) {
            return false;
        }
        char first = lexema.charAt(0);
        if (first != '_' && !isAsciiLetter(first)) {
            return false;
        }
        for (int i = 1; i < lexema.length(); i++) {
            char c = lexema.charAt(i);
            if (c != '_' && !isAsciiLetter(c) && !isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNumber(String lexema) {
        return NUMBER_PATTERN.matcher(lexema).matches();
    }

    private boolean isStringLiteral(String lexema) {
        if (lexema.length() < 2) {
            return false;
        }
        char first = lexema.charAt(0);
        char last = lexema.charAt(lexema.length() - 1);
        return (first == '"' && last == '"') || (first == '\'' && last == '\'');
    }
}
