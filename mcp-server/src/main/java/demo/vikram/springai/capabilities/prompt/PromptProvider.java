package demo.vikram.springai.capabilities.prompt;

import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpComplete;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptProvider {

    private static List<String> MIDDLE_EARTH_USERS = List.of("frodo", "samwise", "merry", "pippin");

    @McpPrompt(
            name = "hobbitInc-greeting",
            description = "Provides welcome to Hobbit Inc."
    )
    public List<McpSchema.PromptMessage> welcomeToHobbitInc(final McpSchema.GetPromptRequest request) {
        return List.of(
                new McpSchema.PromptMessage(McpSchema.Role.ASSISTANT,
                        new McpSchema.TextContent("Hello, Welcome to Hobbit Inc." +
                                "I will be your Middle Earth Guide for today where I can tell you about hobbits. " +
                                "Directions from and to certain destinations in Middle Earth. " +
                                "Recipes about some of the favourite hobbit food along the way. " +
                                "How can I help ?")),
                new McpSchema.PromptMessage(McpSchema.Role.USER,
                        new McpSchema.TextContent("I would like to learn more about Hobbit Inc.")),
                new McpSchema.PromptMessage(McpSchema.Role.ASSISTANT,
                        new McpSchema.ResourceLink("Hobbit Inc",
                        "Hobbit Inc Ltd",
                        "company://resources",
                        "Provides information about Hobbit Inc.",
                        null,
                        null,
                        null,
                        null))
        );
    }

    @McpComplete(
            prompt = "hobbit-info"
    )
    public List<String> searchableHobbits(final McpSchema.CompleteRequest.CompleteArgument argument) {
        if ("hobbit".equals(argument.name())) {
            return MIDDLE_EARTH_USERS.stream()
                    .filter(name -> name.toLowerCase().startsWith(argument.value().toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    @McpPrompt(
            name = "hobbit-info",
            description = "Provides aid in getting adequate information about a hobbit"
    )
    public McpSchema.PromptMessage hobbitInfoPrompt(
            @McpArg(required = true, name = "hobbit", description = "Name of the hobbit to search information for") String hobbit) {

        return new McpSchema.PromptMessage(McpSchema.Role.ASSISTANT, new McpSchema.ResourceLink("Hobbit Profile",
                "Hobbit Profile",
                String.format("hobbit-profile://profile/%s".formatted(hobbit)),
                "Provides profile details about the hobbit %s".formatted(hobbit),
                null,
                null,
                null,
                null));
    }
}
