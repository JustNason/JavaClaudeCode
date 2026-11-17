package tools;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionTool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ReadTool {
    public static String readFileContents(String path) {
        Path filePath = Path.of(path);
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public static ChatCompletionTool create() {
        return ChatCompletionTool.builder()
            .function(FunctionDefinition.builder()
                .name("Read")
                .description("Read and return the contents of a file")
                .parameters(FunctionParameters.builder()
                    .putAdditionalProperty("type", JsonValue.from("object"))
                    .putAdditionalProperty("properties", JsonValue.from(Map.of(
                        "file_path", Map.of(
                            "type", "string",
                            "description", "The path to the file to read"
                        )))
                    )
                    .putAdditionalProperty("required", JsonValue.from(List.of("file_path")))
                    .build()
                )
                .build()
            )
            .build();
    }
}
