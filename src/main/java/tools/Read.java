package tools;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class Read {
    public String readFileContents(String path) {
        Path filePath = Path.of(path);
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
