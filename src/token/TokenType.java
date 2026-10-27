package token;

/**
 * Tipos de token del lenguaje Toy.
 *
 * <p>Cada constante conserva el nombre de la tabla de tokens de la
 * especificación. Por eso los nombres llevan el prefijo {@code TOK_} y la
 * salida del analizador imprime exactamente ese nombre.</p>
 */
public enum TokenType {
    // Palabras reservadas
    TOK_IF,
    TOK_THEN,
    TOK_ELSE,
    TOK_TRUE,
    TOK_FALSE,
    TOK_AND,
    TOK_OR,
    TOK_WHILE,
    TOK_DO,
    TOK_FOR,
    TOK_FUNC,
    TOK_END,
    TOK_LOCAL,
    TOK_PRINT,
    TOK_PRINTLN,
    TOK_RET,
    TOK_VNUMBER,
    TOK_VSTRING,
    TOK_BOOL,

    // Operadores aritméticos
    TOK_PLUS,
    TOK_MINUS,
    TOK_MUL,
    TOK_DIV,
    TOK_MOD,
    TOK_EXP,

    // Operadores relacionales
    TOK_GT,
    TOK_LT,
    TOK_GTE,
    TOK_LTE,
    TOK_EQ,
    TOK_NEQ,

    // Operador lógico
    TOK_NOT,

    // Operador de asignación
    TOK_ASSIGN,

    // Símbolos
    TOK_LPAR,
    TOK_RPAR,
    TOK_COMMA,

    // Identificadores y literales
    TOK_ID,
    TOK_NUMBER,
    TOK_STRING
}
