package token;

/**
 * Representa un token reconocido por el analizador léxico.
 *
 * <p>El lexema se guarda tal como aparece en el archivo fuente. Para los
 * literales de cadena esto incluye las comillas de apertura y cierre.</p>
 */
public class Token {
    private final TokenType tipo;
    private final String lexema;

    public Token(TokenType tipo, String lexema) {
        this.tipo = tipo;
        this.lexema = lexema;
    }

    public TokenType getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    @Override
    public String toString() {
        return "Token: " + tipo + " \"" + lexema + "\"";
    }
}
