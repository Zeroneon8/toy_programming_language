# Analizador Léxico para el Lenguaje Toy

Implementación en Java de un **analizador léxico** para el lenguaje Toy. El
escáner se construyó a mano, sin herramientas de generación automática, y usa
un **Autómata Finito Determinista (AFD)** representado como una tabla de
transiciones.

El proyecto también deja preparada la carpeta `src/parser/` para la siguiente
etapa: el reconocedor sintáctico descendente recursivo.

---

## 1. Estructura del proyecto

```text
toy_programming_language/
├── src/
│   ├── Main.java                    Punto de entrada
│   ├── input/
│   │   ├── ToyFileReader.java       Lectura del archivo .toy/.txt
│   │   └── prueba_lexer.toy         Archivo de ejemplo
│   ├── lexer/
│   │   ├── Alphabet.java            Alfabeto de entrada del AFD
│   │   ├── Status.java              Estados del AFD
│   │   ├── Automaton.java           Tabla de transiciones y aceptación
│   │   └── Scanner.java             Obtención y clasificación de lexemas
│   ├── token/
│   │   ├── Token.java               Par (tipo, lexema)
│   │   └── TokenType.java           Tipos de token de Toy
│   └── parser/
│       └── .gitkeep                 Reservada para el parser descendente
├── tests/
│   ├── *.toy                        Casos de prueba
│   ├── *.expected.txt               Salidas esperadas
│   └── run_tests.ps1                Ejecutor de pruebas
├── bin/                             Clases compiladas
└── README.md
```

---

## 2. Tokens soportados

### 2.1 Palabras reservadas

| Lexema    | Token         | Lexema    | Token          |
|-----------|---------------|-----------|----------------|
| `if`      | `TOK_IF`      | `then`    | `TOK_THEN`     |
| `else`    | `TOK_ELSE`    | `true`    | `TOK_TRUE`     |
| `false`   | `TOK_FALSE`   | `and`     | `TOK_AND`      |
| `or`      | `TOK_OR`      | `while`   | `TOK_WHILE`    |
| `do`      | `TOK_DO`      | `for`     | `TOK_FOR`      |
| `func`    | `TOK_FUNC`    | `end`     | `TOK_END`      |
| `local`   | `TOK_LOCAL`   | `print`   | `TOK_PRINT`    |
| `println` | `TOK_PRINTLN` | `ret`     | `TOK_RET`      |
| `number`  | `TOK_VNUMBER` | `string`  | `TOK_VSTRING`  |
| `bool`    | `TOK_BOOL`    |           |                |

### 2.2 Operadores y símbolos

| Lexema | Token        | Categoría            |
|--------|--------------|----------------------|
| `+`    | `TOK_PLUS`   | Aritmético           |
| `-`    | `TOK_MINUS`  | Aritmético           |
| `*`    | `TOK_MUL`    | Aritmético           |
| `/`    | `TOK_DIV`    | Aritmético           |
| `%`    | `TOK_MOD`    | Aritmético           |
| `^`    | `TOK_EXP`    | Aritmético           |
| `>`    | `TOK_GT`     | Relacional           |
| `<`    | `TOK_LT`     | Relacional           |
| `>=`   | `TOK_GTE`    | Relacional           |
| `<=`   | `TOK_LTE`    | Relacional           |
| `==`   | `TOK_EQ`     | Relacional           |
| `~=`   | `TOK_NEQ`    | Relacional           |
| `~`    | `TOK_NOT`    | Lógico               |
| `:=`   | `TOK_ASSIGN` | Asignación           |
| `(`    | `TOK_LPAR`   | Símbolo              |
| `)`    | `TOK_RPAR`   | Símbolo              |
| `,`    | `TOK_COMMA`  | Símbolo              |

### 2.3 Identificadores y literales

| Token        | Regla de formación |
|--------------|--------------------|
| `TOK_ID`     | `[A-Za-z_][A-Za-z0-9_]*` |
| `TOK_NUMBER` | `[0-9]+(\.[0-9]+)?([eE][+-]?[0-9]+)?` |
| `TOK_STRING` | Texto entre comillas dobles (`"..."`) o simples (`'...'`) |

Los literales de cadena conservan sus comillas en el lexema. Por eso, como
`Token.toString()` encierra el lexema entre comillas, una cadena se muestra así:

```text
Token: TOK_STRING ""hola mundo""
```

---

## 3. Autómata Finito Determinista

### 3.1 Alfabeto

El alfabeto tiene 17 columnas. Cada carácter del archivo se clasifica en una de
ellas antes de consultar la tabla:

```text
Letra | Exp | Dig | " | ' | Simp | Blanco | . | + | - | = | : | ~ | > | < | \n | Otro
```

- **Letra:** letras ASCII y `_`, excluyendo `e` y `E`.
- **Exp:** `e` o `E`.
- **Dig:** `0-9`.
- **Simp:** `* / % ^ ( ) ,`.
- **Blanco:** espacio, tabulador y `\r`.
- **Otro:** cualquier carácter fuera del alfabeto.

### 3.2 Estados

| Estado | Nombre | Aceptación |
|---|---|:---:|
| S0 | `START` | No |
| S1 | `IN_IDENTIFIER` | Sí |
| S2 | `IN_INTEGER` | Sí |
| S3 | `IN_DECIMAL_POINT` | No |
| S4 | `IN_DECIMAL` | Sí |
| S5 | `IN_EXPONENT` | No |
| S6 | `IN_EXPONENT_SIGN` | No |
| S7 | `IN_EXPONENT_DIGITS` | Sí |
| S8 | `IN_STRING_DOUBLE` | No |
| S9 | `IN_STRING_SINGLE` | No |
| S10 | `IN_STRING_CLOSED` | Sí |
| S11 | `IN_SIMPLE_SYMBOL` | Sí |
| S12 | `IN_MINUS` | Sí |
| S13 | `IN_COMMENT` | Sí |
| S14 | `IN_ONE_CHAR_COMPARISON` | Sí |
| S15 | `IN_EQUAL_OR_COLON` | No |
| S16 | `IN_TWO_CHAR_OPERATOR` | Sí |
| — | `STOP` | No |

### 3.3 Tabla de transiciones

```text
      Letra Exp Dig  "   '  Simp Blanco  .   +   -   =   :   ~   >   <  \n Otro
S0 :   S1   S1  S2  S8  S9  S11  S0   STOP S11 S12 S15 S15 S14 S14 S14 S0  STOP
S1 :   S1   S1  S1 STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP
S2 :  STOP  S5  S2 STOP STOP STOP STOP  S3 STOP STOP STOP STOP STOP STOP STOP STOP STOP
S3 :  STOP STOP  S4 STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP
S4 :  STOP  S5  S4 STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP
S5 :  STOP STOP  S7 STOP STOP STOP STOP STOP  S6  S6 STOP STOP STOP STOP STOP STOP STOP
S6 :  STOP STOP  S7 STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP
S7 :  STOP STOP  S7 STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP
S8 :   S8   S8  S8 S10  S8  S8   S8   S8   S8  S8  S8  S8  S8  S8  S8 STOP  S8
S9 :   S9   S9  S9  S9 S10  S9   S9   S9   S9  S9  S9  S9  S9  S9  S9 STOP  S9
S10: STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP
S11: STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP
S12: STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP S13 STOP STOP STOP STOP STOP STOP
S13:  S13  S13 S13 S13 S13 S13  S13  S13  S13 S13 S13 S13 S13 S13 S13 STOP S13
S14: STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP S16 STOP STOP STOP STOP STOP STOP
S15: STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP S16 STOP STOP STOP STOP STOP STOP
S16: STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP STOP
```

Toda transición no listada lleva a `STOP`.

---

## 4. Funcionamiento del escáner

### 4.1 Obtención de lexemas: `getLexemas()`

1. Se recorre el arreglo de caracteres de izquierda a derecha.
2. En estado `START`, los espacios, tabuladores, `\r` y `\n` se ignoran.
3. Para cada carácter se consulta `table[estado][símbolo]`.
4. Si el destino es `STOP`:
   - Si el estado actual es de aceptación, se cierra el lexema y el carácter
     actual se reprocesa desde `START`.
   - Si no es de aceptación, el lexema está incompleto y se lanza un error.
5. Al llegar al final del archivo se cierra el último lexema o se reporta el
   error correspondiente.

Los comentarios `--` se reconocen mediante `S12 → S13` y se descartan: no
generan tokens. Un `-` aislado sí genera `TOK_MINUS`.

### 4.2 Clasificación: `classifyAllTokens()` y `classifyToken()`

Cada lexema se clasifica en este orden:

1. Cadena (`"..."` o `'...'`) → `TOK_STRING`.
2. Palabra reservada → su `TOK_*`.
3. Operador o símbolo → su `TOK_*`.
4. Número → `TOK_NUMBER`.
5. Identificador → `TOK_ID`.
6. Si no coincide con nada → error.

### 4.3 Errores léxicos

El analizador detiene el análisis en el primer error. Algunos casos:

- Carácter fuera del alfabeto: `Caracter no reconocido '@' en la posicion 7`.
- Punto decimal sin dígitos: `Se esperaban digitos despues del punto decimal...`.
- Exponente incompleto: `Numero incompleto: se esperaba un digito o un signo...`.
- Cadena sin cerrar antes de un salto de línea o del final del archivo.
- `=` o `:` sin el segundo `=` (`==` o `:=`).

Las posiciones son índices de carácter con base 0.

---

## 5. Compilación y ejecución

Requisitos: **JDK 11 o superior**.

### Compilar

```powershell
javac -d bin -sourcepath src src\Main.java
```

### Ejecutar con un archivo

```powershell
java -cp bin Main src\input\prueba_lexer.toy
```

### Ejecutar con el diálogo de selección

```powershell
java -cp bin Main
```

### Ejemplo de salida

```text
Token: TOK_FUNC "func"
Token: TOK_ID "main"
Token: TOK_LPAR "("
Token: TOK_RPAR ")"
Token: TOK_LOCAL "local"
Token: TOK_ID "x"
Token: TOK_ASSIGN ":="
Token: TOK_NUMBER "10"
```

---

## 6. Pruebas

Cada archivo `.toy` de `tests/` tiene su salida esperada en `.expected.txt`.
Para ejecutar todos los casos:

```powershell
powershell -ExecutionPolicy Bypass -File tests\run_tests.ps1
```

| Caso | Qué prueba |
|------|------------|
| `01_program` | Programa Toy de ejemplo |
| `02_keywords` | Todas las palabras reservadas |
| `03_operators` | Todos los operadores y símbolos |
| `04_numbers` | Enteros, decimales y exponentes |
| `05_strings` | Cadenas con comillas dobles y simples |
| `06_comments` | Comentarios `--` ignorados |
| `07_invalid_number` | Número decimal incompleto |
| `08_unterminated_string` | Cadena sin cerrar antes de un salto de línea |
| `09_invalid_char` | Carácter fuera del alfabeto |
| `10_lone_assign` | `=` sin el segundo `=` |
| `11_incomplete_exponent` | Exponente incompleto |
| `12_unterminated_string_eof` | Cadena sin cerrar al final del archivo |

---

## 7. Notas y limitaciones

- Los comentarios son de línea: empiezan con `--` y terminan en `\n`.
- Las cadenas no pueden ocupar varias líneas.
- No se procesan secuencias de escape: una barra invertida es un carácter
  normal dentro de la cadena y la comilla del mismo tipo la cierra.
- Los literales de cadena conservan sus comillas en el lexema.
- `=` y `:` por sí solos no son tokens válidos; solo `==` y `:=`.
- No hay directivas de preprocesamiento ni comentarios de bloque.
- La carpeta `src/parser/` está reservada para el reconocedor descendente
  recursivo; todavía no contiene código.

---

## 8. Siguiente etapa

El reconocedor sintáctico descendente recursivo podrá consumir la lista de
`Token` generada por `Scanner` y ubicarse en el paquete `parser`, sin modificar
el analizador léxico.
