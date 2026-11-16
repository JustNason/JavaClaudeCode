import com.fasterxml.jackson.core.JsonProcessingException;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.*;
import tools.ReadTool;

import static com.openai.core.ObjectMappers.jsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2 || !"-p".equals(args[0])) {
            System.err.println("Usage: program -p <prompt>");
            System.exit(1);
        }

        boolean runLoop = true;

        String prompt = args[1];

        ReadTool fileReader = new ReadTool();

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


        ChatCompletionTool readTool = ChatCompletionTool.builder()
                .function(
                        FunctionDefinition.builder()
                                .name("Read")
                                .description("Read and return the contents of a file")
                                .parameters(
                                        FunctionParameters.builder()
                                                .putAdditionalProperty("type", JsonValue.from("object"))
                                                .putAdditionalProperty("properties", JsonValue.from(Map.of(
                                                        "file_path", Map.of(
                                                                "type", "string",
                                                                "description", "The path to the file to read"
                                                                                )
                                                                )
                                                        )
                                                )
                                                .putAdditionalProperty("required", JsonValue.from(List.of("file_path")))
                                                .build()
                                )
                                .build()
                )
                .build();

        System.err.println("Logs from your program will appear here!");

        List<ChatCompletionMessageParam> messages = new ArrayList<>();
        messages.add(
                ChatCompletionMessageParam.ofUser(
                        ChatCompletionUserMessageParam.builder()
                                .content(prompt)
                                .build()
                )
        );
        while (runLoop) {

            ChatCompletionCreateParams chat = ChatCompletionCreateParams.builder()
                    .model("anthropic/claude-haiku-4.5")
                    .messages(messages)
                    .addTool(readTool)
                    .build();

            ChatCompletion response = client.chat().completions().create(
                    chat
            );




            if (response.choices().isEmpty()) {
                throw new RuntimeException("no choices in response");
            } else {

                ChatCompletionMessage message = response.choices().getLast().message();
                messages.add(Utils.appendResponse(message.toParam()));
                if (message.toolCalls().isPresent()) {
                    for (ChatCompletionMessageToolCall tool : message.toolCalls().get()) {
                        if (tool.function().name().equals("Read")) {
                            try {
                                String path = jsonMapper().readTree(tool.function().arguments()).get("file_path").asText();

                                String contents = ReadTool.readFileContents(path);


                                messages.add(Utils.appendTool(tool.id(), contents));
//                                System.out.println("FILE IS " + messages.toString() +  "END");
                            } catch (JsonProcessingException e) {

                            }
                        }
                    }
                }
                else {
                    System.out.print(message.content().orElse(""));
                    runLoop = false;
                }
            }


            // You can use print statements as follows for debugging, they'll be visible when running tests.

            // TODO: Uncomment the line below to pass the first stage

//            System.out.print(response.choices().get(0).message().content().orElse(""));
        }
    }
}
