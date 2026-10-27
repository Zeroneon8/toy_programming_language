package parser;

import java.util.List;
import token.Token;
import token.TokenType;

/**
 * Reconocedor sintáctico descendente recursivo del lenguaje Toy.
 *
 * <p>Cada no terminal de la gramática tiene un método con el mismo nombre. Encima
 * de cada método se indica el número de las reglas que implementa. Las reglas
 * recursivas que terminan en epsilon (por ejemplo {@code <expSuma'>} o
 * {@code <masArgumentos>}) se implementan como ciclos {@code while}, lo cual es
 * equivalente.</p>
 *
 * <p>La gramática es LL(1): en cada punto de decisión basta con mirar el token
 * actual para escoger la producción.</p>
 */
public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Analiza el programa completo. Si termina sin lanzar
     * {@link SyntaxException}, el programa es sintácticamente válido.
     */
    public void parse() {
        programa();
        if (es(TokenType.TOK_LOCAL)) {
            throw error("'local' solo se puede usar dentro de un bloque (if/while/for/func)");
        }
        if (es(TokenType.TOK_RET)) {
            throw error("'ret' solo se puede usar dentro de un bloque");
        }
        if (!finDeArchivo()) {
            throw error("se esperaba el inicio de una sentencia o una funcion");
        }
    }

    // =====================================================================
    // Utilidades
    // =====================================================================

    private boolean finDeArchivo() {
        return pos >= tokens.size();
    }

    /** Tipo del token actual, o {@code null} si ya no quedan tokens. */
    private TokenType tipoActual() {
        return finDeArchivo() ? null : tokens.get(pos).getTipo();
    }

    private boolean es(TokenType... tipos) {
        TokenType actual = tipoActual();
        for (TokenType t : tipos) {
            if (actual == t) {
                return true;
            }
        }
        return false;
    }

    private void avanzar() {
        pos++;
    }

    private void consumir(TokenType esperado, String descripcion) {
        if (!es(esperado)) {
            throw error("se esperaba " + descripcion);
        }
        avanzar();
    }

    private SyntaxException error(String mensaje) {
        String encontrado = finDeArchivo()
                ? "el final del archivo"
                : "'" + tokens.get(pos).getLexema() + "' (token " + (pos + 1) + ")";
        return new SyntaxException("Error sintactico: " + mensaje + ", pero se encontro " + encontrado);
    }

    // Conjuntos PRIMERO usados para decidir entre producciones

    private boolean inicioSentencia() {
        return es(TokenType.TOK_WHILE, TokenType.TOK_IF, TokenType.TOK_FOR,
                TokenType.TOK_PRINT, TokenType.TOK_PRINTLN, TokenType.TOK_VNUMBER,
                TokenType.TOK_VSTRING, TokenType.TOK_BOOL, TokenType.TOK_ID);
    }

    private boolean inicioTipo() {
        return es(TokenType.TOK_VNUMBER, TokenType.TOK_VSTRING, TokenType.TOK_BOOL);
    }

    private boolean inicioExpresion() {
        return es(TokenType.TOK_LPAR, TokenType.TOK_MINUS, TokenType.TOK_NOT,
                TokenType.TOK_NUMBER, TokenType.TOK_STRING, TokenType.TOK_TRUE,
                TokenType.TOK_FALSE, TokenType.TOK_ID);
    }

    // =====================================================================
    // Programa y funciones
    // =====================================================================

    // 1. <programa> -> <elemento> <programa>
    // 2. <programa> -> ε
    private void programa() {
        while (es(TokenType.TOK_FUNC) || inicioSentencia()) {
            elemento();
        }
    }

    // 3. <elemento> -> <funcion>
    // 4. <elemento> -> <sentencia>
    private void elemento() {
        if (es(TokenType.TOK_FUNC)) {
            funcion();
        } else {
            sentencia();
        }
    }

    // 5. <funcion> -> func id ( <parametros> ) <bloque> end
    private void funcion() {
        consumir(TokenType.TOK_FUNC, "'func'");
        consumir(TokenType.TOK_ID, "el nombre de la funcion");
        consumir(TokenType.TOK_LPAR, "'('");
        parametros();
        consumir(TokenType.TOK_RPAR, "')'");
        bloque();
        consumir(TokenType.TOK_END, "'end' para cerrar la funcion");
    }

    // 6. <parametros> -> <tipo> id <masParametros>
    // 7. <parametros> -> ε
    private void parametros() {
        if (inicioTipo()) {
            tipo();
            consumir(TokenType.TOK_ID, "el nombre del parametro");
            masParametros();
        } else if (es(TokenType.TOK_ID)) {
            throw error("se esperaba el tipo del parametro (number, string o bool)");
        }
    }

    // 8. <masParametros> -> , <tipo> id <masParametros>
    // 9. <masParametros> -> ε
    private void masParametros() {
        while (es(TokenType.TOK_COMMA)) {
            avanzar();
            tipo();
            consumir(TokenType.TOK_ID, "el nombre del parametro");
        }
    }

    // =====================================================================
    // Bloques
    // =====================================================================

    // 10. <bloque> -> <sentenciaBloque> <bloque>
    // 11. <bloque> -> <sentenciaRet>
    // 12. <bloque> -> ε
    private void bloque() {
        while (inicioSentencia() || es(TokenType.TOK_LOCAL)) {
            sentenciaBloque();
        }
        if (es(TokenType.TOK_RET)) {
            sentenciaRet();
        }
        if (es(TokenType.TOK_FUNC)) {
            throw error("las funciones solo se pueden declarar en el ambito global "
                    + "(no anidadas ni dentro de bloques)");
        }
    }

    // 13. <sentenciaBloque> -> <sentencia>
    // 14. <sentenciaBloque> -> <sentenciaLocal>
    private void sentenciaBloque() {
        if (es(TokenType.TOK_LOCAL)) {
            sentenciaLocal();
        } else {
            sentencia();
        }
    }

    // 15. <sentenciaLocal> -> local <declaracion>
    private void sentenciaLocal() {
        consumir(TokenType.TOK_LOCAL, "'local'");
        declaracion();
    }

    // 16. <sentenciaRet> -> ret <valorRet>
    // 17. <valorRet> -> <expresion>
    // 18. <valorRet> -> ε
    private void sentenciaRet() {
        consumir(TokenType.TOK_RET, "'ret'");
        if (inicioExpresion()) {
            expresion();
        }
    }

    // =====================================================================
    // Sentencias
    // =====================================================================

    // 19. <sentencia> -> <sentenciaWhile>
    // 20. <sentencia> -> <sentenciaIf>
    // 21. <sentencia> -> <sentenciaFor>
    // 22. <sentencia> -> <sentenciaPrint>
    // 23. <sentencia> -> <declaracion>
    // 24. <sentencia> -> <sentenciaId>
    private void sentencia() {
        if (es(TokenType.TOK_WHILE)) {
            sentenciaWhile();
        } else if (es(TokenType.TOK_IF)) {
            sentenciaIf();
        } else if (es(TokenType.TOK_FOR)) {
            sentenciaFor();
        } else if (es(TokenType.TOK_PRINT, TokenType.TOK_PRINTLN)) {
            sentenciaPrint();
        } else if (inicioTipo()) {
            declaracion();
        } else if (es(TokenType.TOK_ID)) {
            sentenciaId();
        } else {
            throw error("se esperaba una sentencia");
        }
    }

    // 25. <sentenciaWhile> -> while <expresion> do <bloque> end
    private void sentenciaWhile() {
        consumir(TokenType.TOK_WHILE, "'while'");
        expresion();
        consumir(TokenType.TOK_DO, "'do'");
        bloque();
        consumir(TokenType.TOK_END, "'end' para cerrar el while");
    }

    // 26. <sentenciaIf> -> if <expresion> then <bloque> <sentenciaIf'> end
    // 27. <sentenciaIf'> -> else <bloque>
    // 28. <sentenciaIf'> -> ε
    private void sentenciaIf() {
        consumir(TokenType.TOK_IF, "'if'");
        expresion();
        consumir(TokenType.TOK_THEN, "'then'");
        bloque();
        if (es(TokenType.TOK_ELSE)) {
            avanzar();
            bloque();
        }
        consumir(TokenType.TOK_END, "'end' para cerrar el if");
    }

    // 29. <sentenciaFor> -> for <declaracion> , <expresion> <pasoFor> do <bloque> end
    // 30. <pasoFor> -> , <expresion>
    // 31. <pasoFor> -> ε
    private void sentenciaFor() {
        consumir(TokenType.TOK_FOR, "'for'");
        declaracion();
        consumir(TokenType.TOK_COMMA, "',' antes del valor final del for");
        expresion();
        if (es(TokenType.TOK_COMMA)) {
            avanzar();
            expresion();
        }
        consumir(TokenType.TOK_DO, "'do'");
        bloque();
        consumir(TokenType.TOK_END, "'end' para cerrar el for");
    }

    // 32. <sentenciaPrint> -> print <expresion>
    // 33. <sentenciaPrint> -> println <expresion>
    private void sentenciaPrint() {
        avanzar(); // print o println
        expresion();
    }

    // 34. <declaracion> -> <tipo> id := <expresion>
    private void declaracion() {
        tipo();
        consumir(TokenType.TOK_ID, "un identificador");
        consumir(TokenType.TOK_ASSIGN, "':='");
        expresion();
    }

    // 35. <tipo> -> number
    // 36. <tipo> -> string
    // 37. <tipo> -> bool
    private void tipo() {
        if (!inicioTipo()) {
            throw error("se esperaba un tipo (number, string o bool)");
        }
        avanzar();
    }

    // 38. <sentenciaId> -> id <sentenciaIdResto>
    // 39. <sentenciaIdResto> -> := <expresion>
    // 40. <sentenciaIdResto> -> ( <argumentos> )
    private void sentenciaId() {
        consumir(TokenType.TOK_ID, "un identificador");
        if (es(TokenType.TOK_ASSIGN)) {
            avanzar();
            expresion();
        } else if (es(TokenType.TOK_LPAR)) {
            avanzar();
            argumentos();
            consumir(TokenType.TOK_RPAR, "')'");
        } else {
            throw error("se esperaba ':=' o '(' despues del identificador");
        }
    }

    // 41. <argumentos> -> <expresion> <masArgumentos>
    // 42. <argumentos> -> ε
    // 43. <masArgumentos> -> , <expresion> <masArgumentos>
    // 44. <masArgumentos> -> ε
    private void argumentos() {
        if (inicioExpresion()) {
            expresion();
            while (es(TokenType.TOK_COMMA)) {
                avanzar();
                expresion();
            }
        }
    }

    // =====================================================================
    // Expresiones (de menor a mayor precedencia)
    // =====================================================================

    // 45. <expresion> -> <expOr>
    private void expresion() {
        expOr();
    }

    // 46. <expOr>  -> <expAnd> <expOr'>
    // 47. <expOr'> -> or <expAnd> <expOr'>
    // 48. <expOr'> -> ε
    private void expOr() {
        expAnd();
        while (es(TokenType.TOK_OR)) {
            avanzar();
            expAnd();
        }
    }

    // 49. <expAnd>  -> <expIgualdad> <expAnd'>
    // 50. <expAnd'> -> and <expIgualdad> <expAnd'>
    // 51. <expAnd'> -> ε
    private void expAnd() {
        expIgualdad();
        while (es(TokenType.TOK_AND)) {
            avanzar();
            expIgualdad();
        }
    }

    // 52. <expIgualdad>  -> <expRelacional> <expIgualdad'>
    // 53. <expIgualdad'> -> == <expRelacional> <expIgualdad'>
    // 54. <expIgualdad'> -> ~= <expRelacional> <expIgualdad'>
    // 55. <expIgualdad'> -> ε
    private void expIgualdad() {
        expRelacional();
        while (es(TokenType.TOK_EQ, TokenType.TOK_NEQ)) {
            avanzar();
            expRelacional();
        }
    }

    // 56. <expRelacional>  -> <expSuma> <expRelacional'>
    // 57-60. <expRelacional'> -> (< | <= | > | >=) <expSuma> <expRelacional'>
    // 61. <expRelacional'> -> ε
    private void expRelacional() {
        expSuma();
        while (es(TokenType.TOK_LT, TokenType.TOK_LTE, TokenType.TOK_GT, TokenType.TOK_GTE)) {
            avanzar();
            expSuma();
        }
    }

    // 62. <expSuma>  -> <expMult> <expSuma'>
    // 63. <expSuma'> -> + <expMult> <expSuma'>
    // 64. <expSuma'> -> - <expMult> <expSuma'>
    // 65. <expSuma'> -> ε
    private void expSuma() {
        expMult();
        while (es(TokenType.TOK_PLUS, TokenType.TOK_MINUS)) {
            avanzar();
            expMult();
        }
    }

    // 66. <expMult>  -> <expMod> <expMult'>
    // 67. <expMult'> -> * <expMod> <expMult'>
    // 68. <expMult'> -> / <expMod> <expMult'>
    // 69. <expMult'> -> ε
    private void expMult() {
        expMod();
        while (es(TokenType.TOK_MUL, TokenType.TOK_DIV)) {
            avanzar();
            expMod();
        }
    }

    // 70. <expMod>  -> <expUnaria> <expMod'>
    // 71. <expMod'> -> % <expUnaria> <expMod'>
    // 72. <expMod'> -> ε
    private void expMod() {
        expUnaria();
        while (es(TokenType.TOK_MOD)) {
            avanzar();
            expUnaria();
        }
    }

    // 73. <expUnaria> -> - <expUnaria>
    // 74. <expUnaria> -> ~ <expUnaria>
    // 75. <expUnaria> -> <expPotencia>
    private void expUnaria() {
        if (es(TokenType.TOK_MINUS, TokenType.TOK_NOT)) {
            avanzar();
            expUnaria();
        } else {
            expPotencia();
        }
    }

    // 76. <expPotencia>  -> <primario> <expPotencia'>
    // 77. <expPotencia'> -> ^ <expUnaria>
    // 78. <expPotencia'> -> ε
    private void expPotencia() {
        primario();
        if (es(TokenType.TOK_EXP)) {
            avanzar();
            expUnaria();
        }
    }

    // 79. <primario> -> ( <expresion> )
    // 80. <primario> -> literal_numerico
    // 81. <primario> -> literal_cadena
    // 82. <primario> -> true
    // 83. <primario> -> false
    // 84. <primario> -> id <llamadaOpc>
    // 85. <llamadaOpc> -> ( <argumentos> )
    // 86. <llamadaOpc> -> ε
    private void primario() {
        if (es(TokenType.TOK_LPAR)) {
            avanzar();
            expresion();
            consumir(TokenType.TOK_RPAR, "')'");
        } else if (es(TokenType.TOK_NUMBER, TokenType.TOK_STRING,
                TokenType.TOK_TRUE, TokenType.TOK_FALSE)) {
            avanzar();
        } else if (es(TokenType.TOK_ID)) {
            avanzar();
            if (es(TokenType.TOK_LPAR)) {
                avanzar();
                argumentos();
                consumir(TokenType.TOK_RPAR, "')'");
            }
        } else {
            throw error("se esperaba una expresion");
        }
    }
}
