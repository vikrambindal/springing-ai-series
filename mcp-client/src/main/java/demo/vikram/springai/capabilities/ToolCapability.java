package demo.vikram.springai.capabilities;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpElicitation;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolCapability {

    private final ChatClient chatClient;

    @McpElicitation(clients = "hobbit-mcp-server")
    public McpSchema.ElicitResult handleMissingInfo(McpSchema.ElicitRequest request) {
        String question = request.message();
        log.info("SERVER ASKS {}", question);

        Map<String, Object> stringObjectMap = request.requestedSchema();
        List<String> options = (List<String>) ((Map) ((Map) stringObjectMap.get("properties")).get("travelRoute")).get("enum");
        log.info("Options: {}", options);

        Map<Integer, String> userOptions = new LinkedHashMap<>();
        System.out.println("Please select from below:");
        for (int i = 0; i < options.size(); i++) {
            System.out.println(String.format("%s. %s", i + 1, options.get(i)));
            userOptions.put(i + 1, options.get(i));
        }

        // Replace this with UI / API input in real apps
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your choice: ");
        Integer userOption = Integer.parseInt(scanner.nextLine());
        String userChoice = userOptions.get(userOption);

        // 3. Return the response to the server
        return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT,
                Map.of("travelRoute", userChoice));
    }

    @McpProgress(clients = "hobbit-mcp-server")
    public void handleProgressNotification(McpSchema.ProgressNotification notification) {
        double percentage = notification.progress();
        System.out.println(String.format("Progress: %.2f%% - %s",
                percentage, notification.message()));
    }

    @McpSampling(clients = "hobbit-mcp-server")
    public McpSchema.CreateMessageResult handleSamplingRequest(McpSchema.CreateMessageRequest request) {
        // Process the request and generate a response
        log.info("Sampling request received from MCPServer to MCP Client:\n{}", request);
        String response = """
                    The food is prepared as follows:
                    1. 200g [7 oz] unsalted butter room temperature or softened
                    2. 100g [½ cup] golden caster sugar
                    3. 300g [1¾ cup + 2 tbsp] plain [all-purpose] flour
                    4. Zest of 1 lemon
                    5. Juice of 1 lemon
                    6. 1½ tsp dried or fresh rosemary
                """;

        return McpSchema.CreateMessageResult.builder()
                .role(McpSchema.Role.ASSISTANT)
                .content(new McpSchema.TextContent(response))
                .build();
    }

    public String chat(final String userMessage) {

        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
