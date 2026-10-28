package lexer;

/**
 * Estados del AFD de Toy.
 *
 * <p>El orden de las constantes coincide con el orden de las filas de la tabla
 * de transiciones de {@link Automaton}. {@code STOP} es un estado auxiliar que
 * indica que el lexema actual no puede continuar.</p>
 */
public enum Status {
    /** S0: estado inicial. */
    START,

    /** S1: identificador o palabra reservada (aceptación). */
    IN_IDENTIFIER,

    /** S2: número entero (aceptación). */
    IN_INTEGER,

    /** S3: se leyó el punto decimal. */
    IN_DECIMAL_POINT,

    /** S4: número decimal (aceptación). */
    IN_DECIMAL,

    /** S5: se leyó el indicador de exponente. */
    IN_EXPONENT,

    /** S6: se leyó el signo del exponente. */
    IN_EXPONENT_SIGN,

    /** S7: dígitos del exponente (aceptación). */
    IN_EXPONENT_DIGITS,

    /** S8: cadena abierta con comilla doble. */
    IN_STRING_DOUBLE,

    /** S9: cadena abierta con comilla simple. */
    IN_STRING_SINGLE,

    /** S10: cadena cerrada (aceptación). */
    IN_STRING_CLOSED,

    /** S11: símbolo simple (aceptación). */
    IN_SIMPLE_SYMBOL,

    /** S12: se leyó un menos (aceptación). */
    IN_MINUS,

    /** S13: comentario de línea (aceptación). */
    IN_COMMENT,

    /** S14: {@code <}, {@code >} o {@code ~} (aceptación). */
    IN_ONE_CHAR_COMPARISON,

    /** S15: {@code =} o {@code :} esperando el segundo carácter. */
    IN_EQUAL_OR_COLON,

    /** S16: operador de dos caracteres (aceptación). */
    IN_TWO_CHAR_OPERATOR,

    /** Estado auxiliar de terminación de lexema. */
    STOP
}
