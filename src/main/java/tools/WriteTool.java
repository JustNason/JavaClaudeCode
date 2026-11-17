package tools;

import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionTool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class WriteTool {
    public static String writeFile(String path, String contents) {
        Path filePath = Path.of(path);
        try {
            return Files.writeString(filePath, contents).toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public static ChatCompletionTool create() {
        return ChatCompletionTool.builder()
            .function(FunctionDefinition.builder()
                .name("Write")
                .description("Write content to a file")
                .parameters(FunctionParameters.builder()
                    .putAdditionalProperty("type", JsonValue.from("object"))
                    .putAdditionalProperty("properties", JsonValue.from(Map.of(
                        "file_path", Map.of(
                            "type", "string",
                            "description", "The path of the file to write to"
                        )))
                    )
                    .putAdditionalProperty("properties", JsonValue.from(Map.of(
                        "content", Map.of(
                            "type", "string",
                            "description", "The content to write to the file"
                        )))
                    )
                    .putAdditionalProperty("required", JsonValue.from(List.of("file_path", "content")))
                    .build()
                )
                .build()
            )
            .build();
    }
}
