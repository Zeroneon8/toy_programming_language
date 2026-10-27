import input.ToyFileReader;
import java.util.ArrayList;
import lexer.Scanner;
import token.Token;

/**
 * Punto de entrada del analizador léxico de Toy.
 *
 * <p>Uso:</p>
 *
 * <pre>
 * java -cp bin Main [ruta/al/archivo.toy]
 * </pre>
 *
 * <p>Si se omite la ruta, se abre un diálogo para seleccionar el archivo.</p>
 */
public class Main {

    public static void main(String[] args) {
        ToyFileReader reader = new ToyFileReader();
        char[] buffer = (args.length > 0) ? reader.readFile(args[0]) : reader.readFile();

        if (buffer == null) {
            System.out.println("Error al leer el archivo.");
            return;
        }

        Scanner scanner = new Scanner(buffer);

        try {
            ArrayList<String> lexemas = scanner.getLexemas();
            ArrayList<Token> tokens = scanner.classifyAllTokens(lexemas);
            for (Token token : tokens) {
                System.out.println(token);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
