package parser;

/**
 * Error sintáctico detectado por el {@link Parser}.
 *
 * <p>Se lanza en el primer error encontrado; el análisis se detiene ahí, igual
 * que en el analizador léxico.</p>
 */
public class SyntaxException extends RuntimeException {

    public SyntaxException(String mensaje) {
        super(mensaje);
    }
}
