package input;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lee un archivo de código Toy y lo convierte en un arreglo de caracteres.
 *
 * <p>Si no se indica una ruta, se abre un diálogo nativo para seleccionar el
 * archivo. También se puede llamar {@link #readFile(String)} para ejecutar el
 * analizador desde la línea de comandos, lo que facilita las pruebas.</p>
 */
public class ToyFileReader {

    public char[] readFile() {
        FileDialog fileDialog = new FileDialog(
                (Frame) null,
                "Selecciona el archivo de código Toy (.toy o .txt)",
                FileDialog.LOAD);
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (filename == null) {
            System.out.println("Operacion cancelada por el usuario.");
            return null;
        }

        return readFile(new File(directory, filename).getAbsolutePath());
    }

    public char[] readFile(String filePath) {
        Path path = Path.of(filePath);

        try {
            String text = Files.readString(path);
            // Elimina el BOM UTF-8 si el archivo fue guardado con el.
            if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
                text = text.substring(1);
            }
            return text.toCharArray();
        } catch (IOException e) {
            System.out.println("Ocurrio un error al leer el archivo: " + e.getMessage());
            return null;
        }
    }
}
