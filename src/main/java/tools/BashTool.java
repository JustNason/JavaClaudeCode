package tools;

import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BashTool {
    public static String runBashCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Command timed out";
            }

            int exitCode = process.exitValue();
            return "Exit code: " + exitCode + ", Output: " + output;
        } catch (IOException | InterruptedException e) {
            return e.getMessage();
        }
    }

    public static ChatCompletionTool create() {
        return ChatCompletionTool.builder()
                .function(FunctionDefinition.builder()
                        .name("Bash")
                        .description("Execute a shell command")
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty("type", JsonValue.from("object"))
                                .putAdditionalProperty("properties", JsonValue.from(Map.of(
                                        "command", Map.of(
                                                "type", "string",
                                                "description", "The command to execute"
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
