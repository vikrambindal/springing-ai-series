package demo.vikram.springai.capabilities;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromptCapability {

    private final List<McpSyncClient> mcpSyncClientList;
    private final ChatClient chatClient;
    private final StaticResourceCapability staticResourceCapability;

    public List<ServerPrompt> loadServerPrompts() {

        McpSyncClient mcpSyncClient = mcpSyncClientList.getFirst();

        return mcpSyncClient.listPrompts()
                .prompts()
                .stream()
                .map(serverPrompt -> new ServerPrompt(serverPrompt.name(),
                        serverPrompt.description(),
                        serverPrompt.arguments())
                )
                .toList();
    }

    public List<String> getParameterValues(final String promptName, String argumentName) {

        McpSyncClient mcpSyncClient = mcpSyncClientList.getFirst();

        McpSchema.PromptReference promptReference = new McpSchema.PromptReference(promptName);
        McpSchema.CompleteRequest.CompleteArgument completeArgument = new McpSchema.CompleteRequest.CompleteArgument(argumentName, "");
        McpSchema.CompleteResult completeResult = mcpSyncClient.completeCompletion(new McpSchema.CompleteRequest(promptReference, completeArgument));
        return completeResult.completion().values();
    }

    public String readPrompt(final String promptName, Map<String, Object> arguments) {

        McpSyncClient mcpSyncClient = mcpSyncClientList.getFirst();

        McpSchema.GetPromptRequest promptRequest = new McpSchema.GetPromptRequest(promptName, arguments);
        McpSchema.GetPromptResult promptResult = mcpSyncClient.getPrompt(promptRequest);

        List<Message> messages = new ArrayList<>();
        for (McpSchema.PromptMessage mcpMsg : promptResult.messages()) {
            // Extract text content from MCP PromptMessage
            String text = switch (mcpMsg.content()) {
                case McpSchema.TextContent textContent:
                    yield textContent.text();
                case McpSchema.ImageContent imageContent:
                    yield imageContent.data();
                case McpSchema.AudioContent audioContent:
                    yield audioContent.data();
                case McpSchema.ResourceLink resourceLink:
                    yield staticResourceCapability.loadResourceContent(resourceLink.uri());
                case McpSchema.EmbeddedResource embeddedResource:
                    yield staticResourceCapability.loadResourceContent(embeddedResource.resource().uri());
                default:
                    yield null;
            };

            // Map roles accordingly
            switch (mcpMsg.role()) {
                case USER -> messages.add(new UserMessage(text));
                case ASSISTANT -> {
                    return text;
                }
                default -> messages.add(new SystemMessage(text));
            };
        }

        // 3. Use the messages with ChatClient
        return chatClient.prompt()
                .messages(messages)
                .call()
                .content();
    }

    public record ServerPrompt(String name, String description, List<McpSchema.PromptArgument> promptArguments) {

        @Override
        public String toString() {
            return String.format("%s - %s", name, description);
        }
    }
}
