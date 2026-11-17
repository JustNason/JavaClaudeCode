import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.*;
import tools.ReadTool;
import tools.WriteTool;

import static com.openai.core.ObjectMappers.jsonMapper;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2 || !"-p".equals(args[0])) {
            System.err.println("Usage: program -p <prompt>");
            System.exit(1);
        }

        boolean runLoop = true;

        String prompt = args[1];

        String apiKey = System.getenv("OPENROUTER_API_KEY");
        String baseUrl = System.getenv("OPENROUTER_BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://openrouter.ai/api/v1";
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OPENROUTER_API_KEY is not set");
        }

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();


        ChatCompletionTool readTool = ReadTool.create();
        ChatCompletionTool writeTool = WriteTool.create();

        System.err.println("Logs from your program will appear here!");

        MessageArray messages = new MessageArray(prompt);

        while (runLoop) {

            ChatCompletionCreateParams chat = ChatCompletionCreateParams.builder()
                    .model("anthropic/claude-haiku-4.5")
                    .messages(messages.messagesList)
                    .addTool(readTool)
                    .addTool(writeTool)
                    .build();

            ChatCompletion response = client.chat().completions().create(
                    chat
            );




            if (response.choices().isEmpty()) {
                throw new RuntimeException("No choices in response");
            } else {

                ChatCompletionMessage message = response.choices().getLast().message();
                messages.appendResponse(message.toParam());

                if (message.toolCalls().isPresent()) {
                    for (ChatCompletionMessageToolCall tool : message.toolCalls().get()) {
                        if (tool.function().name().equals("Read")) {
                            try {
                                String path = jsonMapper().readTree(tool.function().arguments()).get("file_path").asText();

                                String contents = ReadTool.readFileContents(path);


                                messages.appendTool(tool.id(), contents);
                            } catch (JsonProcessingException e) {
                                System.err.println("Error processing file path Json. Exception: " + e.getMessage());
                            }
                        }
                        else if (tool.function().name().equals("Write")) {
                            try {
                                String path = jsonMapper().readTree(tool.function().arguments()).get("file_path").asText();
                                String contents = jsonMapper().readTree(tool.function().arguments()).get("content").asText();

                                contents = WriteTool.writeFile(path, contents);

                                messages.appendTool(tool.id(), contents);
                            }
                            catch (JsonProcessingException e) {
                                System.err.println("Error processing file path Json. Exception: " + e.getMessage());
                            }
                        }
                    }
                }
                else {
                    System.out.print(message.content().orElse(""));
                    runLoop = false;
                }
            }
        }
    }
}
