import input.ToyFileReader;
import java.util.ArrayList;
import lexer.Scanner;
import parser.Parser;
import parser.SyntaxException;
import token.Token;

/**
 * Punto de entrada del analizador de Toy.
 *
 * <p>Uso:</p>
 *
 * <pre>
 * java -cp bin Main [--tokens] [ruta/al/archivo.toy]
 * </pre>
 *
 * <p>Sin opciones, ejecuta el analizador léxico y luego el sintáctico. Con
 * {@code --tokens}, solo ejecuta el léxico e imprime la lista de tokens.</p>
 *
 * <p>Si se omite la ruta, se abre un diálogo para seleccionar el archivo.</p>
 */
public class Main {

    public static void main(String[] args) {
        boolean soloTokens = args.length > 0 && args[0].equals("--tokens");
        int indiceArchivo = soloTokens ? 1 : 0;

        ToyFileReader reader = new ToyFileReader();
        char[] buffer = (args.length > indiceArchivo)
                ? reader.readFile(args[indiceArchivo])
                : reader.readFile();

        if (buffer == null) {
            System.out.println("Error al leer el archivo.");
            System.exit(0);
        }

        Scanner scanner = new Scanner(buffer);

        boolean errorLexico = false;
        String mensajeLexico = null;
        ArrayList<Token> tokens = new ArrayList<>();

        // FASE 1: ANÁLISIS LÉXICO
        System.out.println("=== ANÁLISIS LÉXICO ===");
        try {
            ArrayList<String> lexemas = scanner.getLexemas();
            tokens = scanner.classifyAllTokens(lexemas);
            int index = 1;
            for (Token token : tokens) {
                System.out.println(index + ": " + token);
                index++;
            }
            System.out.println("Análisis léxico exitoso: " + tokens.size() + " tokens obtenidos.");
        } catch (IllegalArgumentException e) {
            errorLexico = true;
            mensajeLexico = e.getMessage();
            ArrayList<String> parciales = scanner.getLastLexemas();
            if (parciales != null && !parciales.isEmpty()) {
                System.out.println("Tokens obtenidos hasta el error:");
                int idx = 1;
                for (String lexema : parciales) {
                    try {
                        Token partialToken = scanner.classifyToken(lexema);
                        System.out.println(idx + ": " + partialToken);
                        idx++;
                    } catch (IllegalArgumentException ex) {
                        System.out.println(idx + ": Lexema parcial no clasificado: \"" + lexema + "\"");
                        idx++;
                    }
                }
            }
            System.out.println("Error léxico: " + mensajeLexico);
        }

        if (soloTokens) {
            System.exit(0);
        }

        // FASE 2: ANÁLISIS SINTÁCTICO
        System.out.println();
        System.out.println("=== ANÁLISIS SINTÁCTICO ===");
        if (errorLexico) {
            System.out.println("Omitido por error léxico previo.");
            System.exit(0);
        }

        try {
            new Parser(tokens).parse();
            System.out.println("Análisis sintáctico exitoso: el programa es válido.");
        } catch (SyntaxException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }
}
