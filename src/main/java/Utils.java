import com.openai.core.JsonField;
import com.openai.models.chat.completions.*;
import com.openai.models.chat.completions.ChatCompletion.Choice;

import java.util.Map;

public class Utils {
    public static ChatCompletionMessageParam appendTool(String toolId, String content) {
        return ChatCompletionMessageParam.ofTool(
                ChatCompletionToolMessageParam.builder()
                        .toolCallId(toolId)
                        .content(content)
                        .build()
        );
    }
    public static ChatCompletionMessageParam appendResponse(ChatCompletionAssistantMessageParam response) {
        return ChatCompletionMessageParam.ofAssistant(
                response
        );
    }
}
