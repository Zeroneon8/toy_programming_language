package lexer;

/**
 * Alfabeto de entrada del AFD de Toy.
 *
 * <p>El orden de las constantes es exactamente el orden de las columnas de la
 * tabla de transiciones:</p>
 *
 * <pre>
 * LETTER | EXPONENT | DIGIT | DOUBLE_QUOTE | SINGLE_QUOTE | SIMPLE_SYMBOL
 * WHITE_SPACE | DOT | PLUS | MINUS | EQUAL | COLON | TILDE
 * GREATER_THAN | LESS_THAN | NEW_LINE | OTHER
 * </pre>
 */
public enum Alphabet {
    /** Letras ASCII y guion bajo, excluyendo {@code e} y {@code E}. */
    LETTER,

    /** {@code e} o {@code E}, usado en la notación exponencial. */
    EXPONENT,

    /** Dígitos {@code 0-9}. */
    DIGIT,

    /** Comilla doble. */
    DOUBLE_QUOTE,

    /** Comilla simple. */
    SINGLE_QUOTE,

    /** Símbolos de un solo carácter: {@code * / % ^ ( ) ,}. */
    SIMPLE_SYMBOL,

    /** Espacio, tabulador y retorno de carro. */
    WHITE_SPACE,

    /** Punto decimal. */
    DOT,

    /** Signo más. */
    PLUS,

    /** Signo menos. */
    MINUS,

    /** Signo igual. */
    EQUAL,

    /** Dos puntos. */
    COLON,

    /** Virgulilla. */
    TILDE,

    /** Mayor que. */
    GREATER_THAN,

    /** Menor que. */
    LESS_THAN,

    /** Salto de línea. */
    NEW_LINE,

    /** Cualquier carácter fuera del alfabeto. */
    OTHER
}
