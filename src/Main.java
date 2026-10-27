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
            return;
        }

        Scanner scanner = new Scanner(buffer);

        try {
            ArrayList<String> lexemas = scanner.getLexemas();
            ArrayList<Token> tokens = scanner.classifyAllTokens(lexemas);

            if (soloTokens) {
                for (Token token : tokens) {
                    System.out.println(token);
                }
                return;
            }

            new Parser(tokens).parse();
            System.out.println("Analisis sintactico exitoso: el programa es valido.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SyntaxException e) {
            System.out.println(e.getMessage());
        }
    }
}
