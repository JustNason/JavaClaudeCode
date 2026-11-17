import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;

import java.util.ArrayList;
import java.util.List;

public class MessageArray {
    public List<ChatCompletionMessageParam> messagesList = new ArrayList<>();
    public MessageArray(String prompt) {
        messagesList.add(
            ChatCompletionMessageParam.ofUser(
                ChatCompletionUserMessageParam.builder()
                    .content(prompt)
                    .build()
                )
        );
    }
    public void appendTool(String toolId, String content) {
        messagesList.add(ChatCompletionMessageParam.ofTool(
                ChatCompletionToolMessageParam.builder()
                        .toolCallId(toolId)
                        .content(content)
                        .build()
        ));
    }
    public void appendResponse(ChatCompletionAssistantMessageParam response) {
        messagesList.add(
                ChatCompletionMessageParam.ofAssistant(response)
        );
    }
}